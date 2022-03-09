package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.utils.MathHelps

class DepositorLK(private val hardware: LankyKongHardware, private val console: TelemetryConsole) {

    enum class Axis { X, Y }
    data class SlideConversions(private val countsPerMotorRev: Double = 28.0,
                                private val gearReduction: Int = 1 / 1,
                                private val spoolCircumferenceMM: Double = 112.0) {
        val countsPerInch: Double = 1.0 /*countsPerMotorRev * gearReduction / (spoolCircumferenceMM / 25.4)*/
        fun countsToIn(counts: Int): Double = counts.toDouble() /** countsPerInch*/
        fun inToCounts(In: Double) = In /*/ countsPerInch*/
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
            }
        }
    }
//    General
    private val inRobot = 3.0

//    Dropper Variables
    enum class DropperPos(val posValue: Double) {
        Open(0.7),
        Closed(0.0)
    }
    private val dropperConstraints = MovementConstraints(DropperPos.Open.posValue..DropperPos.Closed.posValue,
                                                               listOf(/*Constraint({target-> target > inRobot}, ""),
                                                                      Constraint({currentXIn > inRobot}, "")*/))
    enum class LiftPos(val counts: Int) {
        LowGoal(573),
        MidGoal(1328),
        HighGoal(2500)
    }

//    X Variables
    private val xMotor = hardware.horiMotor
    private val currentXIn: Double get() = xConversion.countsToIn(xMotor.currentPosition)
    private val xPrecision = 15
    private val xPIDPosition = PID(kp= 0.0001, ki= 0.000001, kd= 0.0)
    private val xPIDJoystick = PID(kp= 0.002)
    private var xPID = xPIDPosition
    private val xConversion = SlideConversions(countsPerMotorRev = 28.0)
    private val xConstraints = MovementConstraints(10.0..6500.0, listOf(Constraint({target-> !(target < inRobot && currentXIn > inRobot)}, ""),
                                                                            /*Constraint({}, "")*/))

//    Y Variables
    private val yMotor = hardware.liftMotor
    private val currentYIn: Double get() = yConversion.countsToIn(yMotor.currentPosition)
    private val yPrecision = 15
    private val yConversion = SlideConversions(countsPerMotorRev = 28.0)
    private val yConstraints = MovementConstraints(0.0..3300.0, listOf())

    fun moveToPosition(yIn: Double = currentYIn, xIn: Double = currentXIn) {
        xPID = xPIDPosition
        var atPosition = false

        while (!atPosition && opmode.opModeIsActive()) {
            atPosition = moveTowardPosition(yIn, xIn)
        }
    }


    fun moveTowardPosition(yIn: Double = currentYIn, xIn: Double = currentXIn): Boolean {
        val yAtTarget = if (yConstraints.withinConstraints(yIn))
                            yTowardPosition(yIn)
                        else
                            true

        val xAtTarget = if (xConstraints.withinConstraints(xIn))
                            xTowardPosition(xIn)
                        else
                            false
        console.display(8, "X at target: $xAtTarget \nY at target: $yAtTarget")
        console.display(9, "X current: $currentXIn \nY current: $currentYIn")

        return yAtTarget && xAtTarget
    }

    private var lastYPos = 0
    private var xMoving = 0.0
    private var lastXPos = 0.0
    fun moveWithJoystick(yStick: Double, xStick: Double) {
        xPID = xPIDJoystick

        val yIn = if (yStick > 0)
            MathHelps().scaleBetween(yStick, 0.0..1.0, currentYIn..yConstraints.limits.endInclusive)
        else
            MathHelps().scaleBetween(yStick, -1.0..0.0, yConstraints.limits.start..currentYIn)

        val xIn = when {
            xStick > 0 -> {xMoving = 0.0; MathHelps().scaleBetween(xStick, 0.0..1.0, currentXIn..xConstraints.limits.endInclusive)}
            xStick < 0 -> {xMoving = 0.0; MathHelps().scaleBetween(xStick, -1.0..0.0, xConstraints.limits.start..currentXIn)}
            else -> {
                if (xMoving < 10) {
                    lastXPos = currentXIn
                    xMoving += 1
                }
                lastXPos
            }
        }

        console.display(11, "X moving: $xMoving")
        moveTowardPosition(yIn, xIn)
    }

    private fun xTowardPosition(inches: Double): Boolean {
        val targetCounts = (xConversion.inToCounts(inches)).toInt()

        val error = targetCounts - hardware.horiMotor.currentPosition
        console.display(10, "X error: $error")
        hardware.horiMotor.power = xPID.calcPID(error.toDouble())

        return if (error in (-xPrecision..xPrecision)) {
            hardware.horiMotor.power = 0.0
            true
        } else {
            false
        }
    }

    private fun yTowardPosition(inches: Double): Boolean {
        val targetCounts = (yConversion.inToCounts(inches)).toInt()

        hardware.liftMotor.power = 1.0/*if (targetCounts - hardware.liftMotor.currentPosition > 0)
            1.0
        else
            0.5*/

        hardware.liftMotor.targetPosition = targetCounts
        hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

        val error = hardware.liftMotor.currentPosition - hardware.liftMotor.targetPosition
        console.display(12, "y error: $error")
        return error in (-yPrecision..yPrecision)
    }

    fun moveToPositionRelative(yIn: Double = 0.0, xIn: Double = 0.0) {
        moveToPosition(currentYIn + yIn,
            currentXIn + xIn)
    }

//    fun moveTowardPositionRelative(yIn: Double, xIn: Double): Boolean =
//        moveTowardPosition(currentYIn + yIn,
//            currentXIn + xIn)

    fun dropper(position: DropperPos, autoResolve: Boolean = false): Boolean =
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
