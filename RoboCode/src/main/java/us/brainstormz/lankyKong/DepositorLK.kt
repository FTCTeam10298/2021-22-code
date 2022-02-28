package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.pid.PID
import us.brainstormz.rataTony.RataTonyHardware
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.utils.MathHelps

class DepositorLK(private val hardware: LankyKongHardware, private val console: TelemetryConsole) {
    enum class Axis { X, Y }
    data class SlideConversions(private val countsPerMotorRev: Double = 28.0,
                                private val gearReduction: Int = 1 / 1,
                                private val spoolCircumferenceMM: Double = 112.0) {
        val countsPerInch: Double = countsPerMotorRev * gearReduction / (spoolCircumferenceMM / 25.4)
        fun countsToIn(counts: Int): Double = counts.toDouble() /** countsPerInch*/
//        fun inToCounts(In: Double) = In / countsPerInch
    }

    data class Constraint(val constraint: (target: Double)->Boolean, val name: String)
    data class MovementConstraints(val limits: ClosedRange<Double>, val conditions: List<Constraint>) {
        var problem: Constraint? = null
        private val completeConditions = conditions + Constraint({ target-> target in limits }, "limits")
        fun withinConstraints(target: Double): Boolean {
            return completeConditions.fold(true) { acc, it ->
                val condition = it.constraint(target)
                if (!condition) {
                    problem = it
                    false
                } else
                    acc
                true
            }
        }
    }
//    General
    private val inRobot = 3.0

//    Dropper Variables
    enum class DropperPos(val posValue: Double) {
        Open(0.0),
        Closed(0.7)
    }
    private val dropperConstraints = MovementConstraints(DropperPos.Open.posValue..DropperPos.Closed.posValue,
                                                               listOf(Constraint({target-> target > inRobot}, ""),
                                                                      Constraint({currentXIn > inRobot}, "")))

//    X Variables
    private val xMotor = hardware.horiMotor
    private val currentXIn: Double get() = xConversion.countsToIn(xMotor.currentPosition)
    private val xPrecision = 1
    private val xPID = PID()
    private val xConversion = SlideConversions(countsPerMotorRev = 28.0)
    private val xConstraints = MovementConstraints(0.0..300.0, listOf(Constraint({target-> !(target < inRobot && currentXIn > inRobot)}, ""),
                                                                            /*Constraint({}, "")*/))

//    Y Variables
    private val yMotor = hardware.liftMotor
    private val currentYIn: Double get() = yConversion.countsToIn(yMotor.currentPosition)
    private val yConversion = SlideConversions(countsPerMotorRev = 28.0)
    private val yConstraints = MovementConstraints(0.0..400.0, listOf())

    fun moveToPosition(yIn: Double = currentYIn, xIn: Double = currentXIn) {
        var atPosition = false

        while (!atPosition && opmode.opModeIsActive()) {
            atPosition = moveTowardPosition(yIn, xIn)
        }
    }


    fun moveTowardPosition(yIn: Double = currentYIn, xIn: Double = currentXIn): Boolean {
        val yAtTarget = if (yConstraints.withinConstraints(yIn))
                            yTowardPositionInches(yIn)
                        else
                            false

        val xAtTarget = if (xConstraints.withinConstraints(xIn))
                            xTowardPosition(xIn)
                        else
                            false

        return yAtTarget && xAtTarget
    }

    private var lastYPos = 0
    private var lastXPos = 0
    fun moveWithJoystick(yStick: Double, xStick: Double) {
        if (yStick == 0.0)
            yTowardPositionCounts(lastYPos)
        else {
            // Ensure proper motor run mode
            if (hardware.liftMotor.mode != DcMotor.RunMode.RUN_USING_ENCODER)
                hardware.liftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER

            // Set motor power, using reduced speed when going down
            hardware.liftMotor.power = yStick

            // Update last used position
            lastYPos = hardware.liftMotor.currentPosition
        }

        if (false)//(xStick == 0.0)
            //xTowardPosition(lastXPos)
        else {
            // Ensure proper motor run mode
            if (hardware.horiMotor.mode != DcMotor.RunMode.RUN_USING_ENCODER)
                hardware.horiMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER

            // Set motor power
            hardware.horiMotor.power = xStick

            // Update last used position
            lastXPos = hardware.horiMotor.currentPosition
        }
//        val yIn = if (yStick > 0)
//            MathHelps().scaleBetween(yStick, 0.0..1.0, currentYIn..yConstraints.limits.endInclusive)
//        else
//            MathHelps().scaleBetween(yStick, -1.0..0.0, yConstraints.limits.start..currentYIn)
//
//        val xIn = if (xStick > 0)
//            MathHelps().scaleBetween(xStick, 0.0..1.0, currentXIn..xConstraints.limits.endInclusive)
//        else
//            MathHelps().scaleBetween(xStick, -1.0..0.0, xConstraints.limits.start..currentXIn)
//        moveTowardPosition(yIn, xIn)
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

    private fun yTowardPositionInches(inches: Double): Boolean {
        val targetCounts = (inches * yConversion.countsPerInch).toInt()

        hardware.liftMotor.power = 1.0/*if (targetCounts - hardware.liftMotor.currentPosition > 0)
            1.0
        else
            0.5*/

        hardware.liftMotor.targetPosition = targetCounts
        if (hardware.liftMotor.mode != DcMotor.RunMode.RUN_TO_POSITION)
            hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

        return hardware.liftMotor.isBusy
    }

    private fun yTowardPositionCounts(counts: Int): Boolean {

        hardware.liftMotor.power = 0.1 //FIXME: power should be function parameter
        /*if (targetCounts - hardware.liftMotor.currentPosition > 0)
            1.0
        else
            0.5*/

        hardware.liftMotor.targetPosition = counts
        if (hardware.liftMotor.mode != DcMotor.RunMode.RUN_TO_POSITION)
            hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

        return hardware.liftMotor.isBusy
    }

    fun moveToPositionRelative(yIn: Double = 0.0, xIn: Double = 0.0) {
        moveToPosition(currentYIn + yIn,
            currentXIn + xIn)
    }

    fun moveTowardPositionRelative(yIn: Double, xIn: Double): Boolean =
        moveTowardPosition(currentYIn + yIn,
            currentXIn + xIn)

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