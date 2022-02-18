package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.pid.PID
import us.brainstormz.rataTony.RataTonyHardware
import us.brainstormz.telemetryWizard.TelemetryConsole

class DepositorLK(private val hardware: RataTonyHardware, private val console: TelemetryConsole) {
    enum class Axis { X, Y }
    data class MovementConstraints(val allowedYHeights: MutableList<ClosedFloatingPointRange<Double>> = mutableListOf(),
                                   val allowedXLengths: MutableList<ClosedFloatingPointRange<Double>> = mutableListOf(),
                                   val allowedDropperPositions: MutableList<DropperPos> = mutableListOf()) {
        fun inConstraints(hardware: RataTonyHardware, exclude: Axis): Boolean {
            val inDropper = allowedDropperPositions.fold(false) { acc, it -> hardware.dropperServo.position == it.posValue || acc }
            var otherAxisMotor = hardware.liftMotor
            val otherAxisConstraints = when (exclude) {
                Axis.X -> {
                    otherAxisMotor = hardware.liftMotor
                    allowedYHeights
                }
                Axis.Y -> {
                    otherAxisMotor = hardware.horiMotor
                    allowedXLengths
                }
            }
            val inOther = otherAxisConstraints.fold(false) { acc, it -> otherAxisMotor.currentPosition.toDouble() in it || acc }
            return inDropper && inOther
        }
    }
    data class SlideConversions(private val countsPerMotorRev: Double = 28.0,
                                private val gearReduction: Int = 1 / 1,
                                private val spoolCircumferenceMM: Double = 112.0,
                                val countsPerInch: Double = countsPerMotorRev * gearReduction / (spoolCircumferenceMM / 25.4)) {
        fun countsToIn(counts: Int) = counts * countsPerInch
        fun inToCounts(In: Double) = In / countsPerInch
    }

//    Dropper Variables
    enum class DropperPos(val posValue: Double) {
        Open(0.0),
        Closed(0.7)
    }

//    X Variables
    private val xPrecision = 1
    val xConversion = SlideConversions(countsPerMotorRev = 28.0)
    val xConstraints = MovementConstraints(allowedYHeights = mutableListOf(),
                                           allowedDropperPositions = mutableListOf(DropperPos.Closed))
    val xPID = PID()

//    Y Variables
    val yConversion = SlideConversions(countsPerMotorRev = 28.0)
    val yConstraints = MovementConstraints(allowedXLengths = mutableListOf(),
                                           allowedDropperPositions = mutableListOf(DropperPos.Closed, DropperPos.Open))

    fun moveToPosition(yIn: Double, xIn: Double) {
        var atPosition = false

        while (!atPosition && opmode.opModeIsActive()) {
            atPosition = moveTowardPosition(yIn, xIn)
        }
    }

    var yAtTarget = false
    var xAtTarget = false
    fun moveTowardPosition(yIn: Double, xIn: Double): Boolean {
        if (yConstraints.inConstraints(hardware, Axis.Y))
            yAtTarget = yTowardPosition(yIn)

        if (xConstraints.inConstraints(hardware, Axis.X))
            xAtTarget = xTowardPosition(xIn)

        return yAtTarget && xAtTarget
    }

    fun moveToPositionRelative(yIn: Double, xIn: Double) {
        moveToPosition(yConversion.countsToIn(hardware.liftMotor.currentPosition) + yIn,
                       xConversion.countsToIn(hardware.horiMotor.currentPosition) + xIn)
    }

    fun moveTowardPositionRelative(yIn: Double, xIn: Double): Boolean {
        return moveTowardPosition(yConversion.countsToIn(hardware.liftMotor.currentPosition) + yIn,
                                  xConversion.countsToIn(hardware.horiMotor.currentPosition) + xIn)
    }

    private fun xTowardPosition(inches: Double): Boolean {
        val targetCounts = (inches * xConversion.countsPerInch).toInt()

        val error = targetCounts - hardware.horiMotor.currentPosition
        hardware.horiMotor.power = xPID.calcPID(error.toDouble())

        return if (error in (-xPrecision..xPrecision)) {
            hardware.horiMotor.power = 0.0
            true
        } else {
            false
        }
    }

    private fun yTowardPosition(inches: Double): Boolean {
        val targetCounts = (inches * yConversion.countsPerInch).toInt()

        hardware.liftMotor.power = if (targetCounts - hardware.liftMotor.currentPosition > 0)
            1.0
        else
            0.5

        hardware.liftMotor.targetPosition = targetCounts
        hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

        return hardware.liftMotor.isBusy
    }

    /**
     * run at the beginning of the program
     * */
    private lateinit var opmode: LinearOpMode
    fun runInLinearOpmode(opmode: LinearOpMode) {
        this.opmode = opmode
    }
}