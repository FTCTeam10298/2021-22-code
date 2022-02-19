package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.pid.PID
import us.brainstormz.rataTony.RataTonyHardware
import us.brainstormz.telemetryWizard.TelemetryConsole

class DepositorLK(private val hardware: RataTonyHardware, private val console: TelemetryConsole) {
    enum class Axis { X, Y }
    data class SlideConversions(private val countsPerMotorRev: Double = 28.0,
                                private val gearReduction: Int = 1 / 1,
                                private val spoolCircumferenceMM: Double = 112.0) {
        val countsPerInch: Double = countsPerMotorRev * gearReduction / (spoolCircumferenceMM / 25.4)
        fun countsToIn(counts: Int) = counts * countsPerInch
        fun inToCounts(In: Double) = In / countsPerInch
    }
    data class MovementConstraints(val allowedYHeights: MutableList<ClosedFloatingPointRange<Double>>? = null,
                                   val allowedXLengths: MutableList<ClosedFloatingPointRange<Double>>? = null,
                                   val allowedDropperPositions: MutableList<DropperPos> = mutableListOf(),
                                   private val depo: DepositorLK) {
        private val hardware = depo.hardware
        var problem: Axis? = null
        fun withinConstraints(): Boolean {
            val inDropper = allowedDropperPositions.fold(false) { acc, it -> hardware.dropperServo.position == it.posValue || acc }

            val inYConstraints = if (allowedYHeights != null) {
                val isWithin =
                    allowedYHeights.fold(false) { acc, it -> depo.currentYIn in it || acc }

                if (!isWithin)
                    problem = Axis.Y

                isWithin
            } else false

            val inXConstraints = if (allowedXLengths != null) {
                val isWithin =
                    allowedXLengths.fold(false) { acc, it -> depo.currentXIn in it || acc }

                if (!isWithin)
                    problem = Axis.X

                isWithin
            } else false

            return inDropper && inYConstraints && inXConstraints
        }
    }

//    Dropper Variables
    enum class DropperPos(val posValue: Double) {
        Open(0.0),
        Closed(0.7)
    }
    private val dropperConstraints = MovementConstraints(allowedYHeights = mutableListOf(),
                                                         allowedXLengths = mutableListOf(),
                                                         allowedDropperPositions = mutableListOf(DropperPos.Closed),
                                                         this)

//    X Variables
    private val xMotor = hardware.horiMotor
    private val currentXIn: Double get() = xConversion.countsToIn(xMotor.currentPosition)
    private val xPrecision = 1
    private val xConversion = SlideConversions(countsPerMotorRev = 28.0)
    private val xConstraints = MovementConstraints(allowedYHeights = mutableListOf(),
                                                   allowedDropperPositions = mutableListOf(DropperPos.Closed),
                                                   depo = this)
    private val xPID = PID()

//    Y Variables
    private val yMotor = hardware.liftMotor
    private val currentYIn: Double get() = yConversion.countsToIn(yMotor.currentPosition)
    private val yConversion = SlideConversions(countsPerMotorRev = 28.0)
    private val yConstraints = MovementConstraints(allowedXLengths = mutableListOf(),
                                                   allowedDropperPositions = mutableListOf(DropperPos.Closed, DropperPos.Open),
                                                   depo = this)

    fun moveToPosition(yIn: Double = currentYIn, xIn: Double = currentXIn) {
        var atPosition = false

        while (!atPosition && opmode.opModeIsActive()) {
            atPosition = moveTowardPosition(yIn, xIn)
        }
    }


    fun moveTowardPosition(yIn: Double = currentYIn, xIn: Double = currentXIn): Boolean {
        val yAtTarget = if (yConstraints.withinConstraints())
                            yTowardPosition(yIn)
                        else
                            false

        val xAtTarget = if (xConstraints.withinConstraints())
                            xTowardPosition(xIn)
                        else
                            false

        return yAtTarget && xAtTarget
    }

    fun moveToPositionRelative(yIn: Double = 0.0, xIn: Double = 0.0) {
        moveToPosition(currentYIn + yIn,
                       currentXIn + xIn)
    }

    fun moveTowardPositionRelative(yIn: Double, xIn: Double): Boolean =
        moveTowardPosition(currentYIn + yIn,
                           currentXIn + xIn)

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

    fun dropper(position: DropperPos, autoResolve: Boolean): Boolean =
        when {
            dropperConstraints.withinConstraints() -> {
                hardware.dropperServo.position = position.posValue
                true
            }
            autoResolve -> {
                when (dropperConstraints.problem) {
                    Axis.Y -> {
                        moveTowardPosition(yIn = dropperConstraints.allowedYHeights!!.minOf { it.start })
                        if (dropperConstraints.withinConstraints()) {
                            hardware.dropperServo.position = position.posValue
                            true
                        } else
                            false
                    }
                    Axis.X -> {
                        moveTowardPosition(xIn = dropperConstraints.allowedXLengths!!.minOf { it.start })
                        if (dropperConstraints.withinConstraints()) {
                            hardware.dropperServo.position = position.posValue
                            true
                        } else
                            false
                    }
                    else -> false
                }
            }
            else -> false
        }

    fun dropperBlocking(position: DropperPos, autoResolve: Boolean) {
        while (opmode.opModeIsActive()) {
            val dropped = dropper(position, autoResolve)
            if (dropped)
                break
        }
    }

    /**
     * run at the beginning of the program
     * */
    private lateinit var opmode: LinearOpMode
    fun runInLinearOpmode(opmode: LinearOpMode) {
        this.opmode = opmode
    }
}