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
    data class MovementConstraints(val limits: ClosedFloatingPointRange<Double>, val conditions: List<(target: Double)->Boolean>, private val depo: DepositorLK) {
        private val hardware = depo.hardware
        var problem: Axis? = null
        fun withinConstraints(target: Double): Boolean {
            return conditions.fold(true) { acc, it ->
                val condition = it(target)
                if (!condition)
                    false
                else 
                    acc
            }
        }
    }

//    Dropper Variables
    enum class DropperPos(val posValue: Double) {
        Open(0.0),
        Closed(0.7)
    }
    private val dropperConstraints = MovementConstraints(0.0..0.7, listOf(),this)
//    X Variables
    private val xMotor = hardware.horiMotor
    private val currentXIn: Double get() = xConversion.countsToIn(xMotor.currentPosition)
    private val xPrecision = 1
    private val xConversion = SlideConversions(countsPerMotorRev = 28.0)
    private val xPID = PID()
    private val xConstraints = MovementConstraints(0.0..300.0, listOf(), this)
//    Y Variables
    private val yMotor = hardware.liftMotor
    private val currentYIn: Double get() = yConversion.countsToIn(yMotor.currentPosition)
    private val yConversion = SlideConversions(countsPerMotorRev = 28.0)
    private val yConstraints = MovementConstraints(0.0..400.0, listOf(), depo = this)

    fun moveToPosition(yIn: Double = currentYIn, xIn: Double = currentXIn) {
        var atPosition = false

        while (!atPosition && opmode.opModeIsActive()) {
            atPosition = moveTowardPosition(yIn, xIn)
        }
    }


    fun moveTowardPosition(yIn: Double = currentYIn, xIn: Double = currentXIn): Boolean {
        val yAtTarget = if (yConstraints.withinConstraints(yIn))
                            yTowardPosition(yIn)
                        else
                            false

        val xAtTarget = if (xConstraints.withinConstraints(xIn))
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
            dropperConstraints.withinConstraints(position.posValue) -> {
                hardware.dropperServo.position = position.posValue
                true
            }
//            autoResolve -> {
//                when (dropperConstraints.problem) {
//                    Axis.Y -> {
//                        moveTowardPosition(yIn = dropperConstraints.allowedYHeights!!.minOf { it.start })
//                        if (dropperConstraints.withinConstraints()) {
//                            hardware.dropperServo.position = position.posValue
//                            true
//                        } else
//                            false
//                    }
//                    Axis.X -> {
//                        moveTowardPosition(xIn = dropperConstraints.allowedXLengths!!.minOf { it.start })
//                        if (dropperConstraints.withinConstraints()) {
//                            hardware.dropperServo.position = position.posValue
//                            true
//                        } else
//                            false
//                    }
//                    else -> false
//                }
//            }
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