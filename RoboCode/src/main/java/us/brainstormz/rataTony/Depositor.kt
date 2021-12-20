package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.pid.PID
import java.lang.Thread.sleep
import kotlinx.coroutines.*


class Depositor2(private val hardware: RataTonyHardware) {
    private enum class Bucket(val position: Double) {
        Open(0.7),
        Closed(0.0)
    }

    enum class Lift(val position: Int) {
        LowGoal(300),
        MidGoal(820),
        HighGoal(1430)
    }

    private val yPID = PID(kp = 0.002, ki = 0.002)
    private val yLimits: IntRange = 0..1430

    private val extendableHeight = 190

    private val xPID = PID(kp = 0.0015, ki = 0.001)
    private val xPower = 1.0

    fun xToPositionBlocking(targetPos: Int) {

    }

    fun xToPosition(targetPos: Int): Unit = runBlocking { async {
        xToPositionBlocking(targetPos)
    } }

    fun yToPositionBlocking(targetPos: Int) {

    }

    fun yToPosition(targetPos: Int): Unit = runBlocking { async {
        yToPositionBlocking(targetPos)
    } }

    fun drop() {
        hardware.dropperServo.position = Bucket.Open.position
    }

    fun close() {
        hardware.dropperServo.position = Bucket.Closed.position
    }

    fun home() {
        hardware.dropperServo.position = Bucket.Closed.position
        xToPosition(0)
        yToPosition(0)
    }

    private fun liftManager() {

    }

    fun init() = runBlocking { async {
        liftManager()
    } }


    private fun posOrNeg(num: Int): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }

}

class Depositor(private val hardware: RataTonyHardware) {
    enum class XPosition {
        Extend,
        Retract
    }

    private val yPID = PID(kp = 0.002, ki = 0.002)
    private val yLimits: IntRange = 0..1430
    private val extendableHeight = 190
    val highGoalHeight = 1430
    val midGoalHeight = 820
    val lowGoalHeight = 300
    var state = 0

    private val xPID = PID(kp = 0.0015, ki = 0.001)
    private val xPower = 1.0
    var xAbsPos = XPosition.Retract
    private val xExtendTime: Long = 755
    private val xRetractTime: Long = 765

    private val dropperOpen = 0.0
    val dropperClosed = 0.7

    fun xToPosition(newPos: XPosition?) {
        if (hardware.liftMotor.currentPosition > extendableHeight) {
            when {
                (newPos == XPosition.Extend) && (xAbsPos == XPosition.Retract) -> {
                    hardware.horiServo.power = xPower
                    sleep(xExtendTime)
                    xAbsPos = XPosition.Extend
                    hardware.horiServo.power = 0.0
                }
                (newPos == XPosition.Retract) && (xAbsPos == XPosition.Extend) -> {
                    hardware.horiServo.power = -xPower
                    sleep(xRetractTime)
                    xAbsPos = XPosition.Retract
                    hardware.horiServo.power = 0.0
                }
                else -> hardware.horiServo.power = 0.0
            }
        }

    }

    fun yInDirection(yDirection: Int) {
//        hardware.liftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER

        val direction = posOrNeg(-yDirection)
        val target = when (direction) {
                1 -> yLimits.last
                -1 -> yLimits.first
                else -> hardware.liftMotor.currentPosition
            }
        if (yDirection != 0)
            yToPosition(target)
        else
            hardware.liftMotor.power = 0.0

//        hardware.liftMotor.power = if (yDirection != 0) {
//            val target = when (direction) {
//                1 -> yLimits.last
//                -1 -> yLimits.first
//                else -> hardware.liftMotor.currentPosition
//            }
//            yPID.calcPID(
//                target.toDouble(),
//                hardware.liftMotor.currentPosition.toDouble()
//            )
//        } else {
//            0.0
//        }

//        if (hardware.liftMotor.currentPosition > yLimits.last) {
//            yToPosition(yLimits.last)
//        } else if (hardware.liftMotor.currentPosition < yLimits.first) {
//            yToPosition(yLimits.first)
//        }
//
//        when (hardware.liftMotor.currentPosition) {
//            in yLimits -> {
//                if (yDirection != 0) {
//                    val target = when (direction) {
//                        1 -> yLimits.last
//                        -1 -> yLimits.first
//                        else -> hardware.liftMotor.currentPosition
//                    }
//                    val pidPower = yPID.calcPID(
//                        target.toDouble(),
//                        hardware.liftMotor.currentPosition.toDouble()
//                    )
//                    pidPower
//                }
//            }
//            else -> 0.0
//        }

    }

    fun yToPosition(targetPos: Int) {
        val adjustedTarget = targetPos.coerceIn(yLimits)

//        val direction = posOrNeg(targetPos)

//        hardware.liftMotor.power = yPID.calcPID(adjustedTarget.toDouble(), hardware.liftMotor.currentPosition.toDouble())
        hardware.liftMotor.power = 1.0

        hardware.liftMotor.targetPosition = adjustedTarget
        hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION


//        while (hardware.liftMotor.isBusy) {
//            hardware.liftMotor.power = yPID.calcPID(adjustedTarget.toDouble(), hardware.liftMotor.currentPosition.toDouble())
//        }
//
//        hardware.liftMotor.power = 0.0
    }

    fun yToPositionBlocking(targetPos: Int) {
        val adjustedTarget = targetPos.coerceIn(yLimits)

//        val direction = posOrNeg(targetPos)

//        hardware.liftMotor.power = yPID.calcPID(adjustedTarget.toDouble(), hardware.liftMotor.currentPosition.toDouble())
        hardware.liftMotor.power = 1.0

        hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

        hardware.liftMotor.targetPosition = adjustedTarget

        while (hardware.liftMotor.isBusy) {
            hardware.liftMotor.power = 1.0
        }
//
//        hardware.liftMotor.power = 0.0
    }

    fun drop() {
        hardware.dropperServo.position = dropperOpen
    }

    fun close() {
        hardware.dropperServo.position = dropperClosed
    }

    fun home() {
        hardware.dropperServo.position = dropperClosed
        xToPosition(XPosition.Retract)
        yToPosition(yLimits.first)
    }

    private fun posOrNeg(num: Int): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }

}

//@TeleOp(name="Depositor Tuner", group="Minibot")
class DepositorTuner: OpMode() {

    val hardware = RataTonyHardware()
    val depositor = Depositor(hardware)

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun loop() {
        telemetry.addLine("")
        telemetry.addLine("y position: ${hardware.liftMotor.currentPosition}")
//        val xPosition = when {
//            hardware.horiExtendLimit.isPressed -> Depositor.XPosition.Extend
//            hardware.horiRetractLimit.isPressed -> Depositor.XPosition.Retract
//            else -> null
//        }
        val xPosition = "not working atm"
        telemetry.addLine("x position: $xPosition")

        telemetry.update()
    }

}
