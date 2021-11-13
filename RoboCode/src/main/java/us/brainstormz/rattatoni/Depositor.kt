package us.brainstormz.rattatoni

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.pid.PID
import java.lang.Thread.sleep

class Depositor(private val hardware: MinibotHardware, private val telemetry: Telemetry) {
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

    private val xPID = PID(kp = 0.0015, ki = 0.001)
    private val xPower = 1.0
    var xAbsPos = XPosition.Retract
    private val xExtendTime: Long = 755
    private val xRetractTime: Long = 755

    private val dropperOpen = 0.0
    private val dropperClosed = 0.5

    fun xToPosition(newPos: XPosition?) {
        if (hardware.liftMotor.currentPosition > extendableHeight) {
            when {
                (newPos == XPosition.Extend) && (xAbsPos == XPosition.Retract) -> {
                    hardware.horiServo.power = xPower
                    sleep(xExtendTime)
                    hardware.horiServo.power = 0.0
                    xAbsPos = XPosition.Extend
                }
                (newPos == XPosition.Retract) && (xAbsPos == XPosition.Extend) -> {
                    hardware.horiServo.power = -xPower
                    sleep(xRetractTime)
                    hardware.horiServo.power = 0.0
                    xAbsPos = XPosition.Retract
                }
                else -> hardware.horiServo.power = 0.0
            }
        }

    }

    fun yInDirection(yDirection: Int) {
        hardware.liftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER

        val direction = posOrNeg(-yDirection)
        hardware.liftMotor.power =
        when (hardware.liftMotor.currentPosition) {
            in yLimits -> {
                if (yDirection != 0) {
                    val target = when (direction) {
                        1 -> yLimits.last
                        -1 -> yLimits.first
                        else -> hardware.liftMotor.currentPosition
                    }
                    val pidPower = yPID.calcPID(
                        target.toDouble(),
                        hardware.liftMotor.currentPosition.toDouble()
                    )
                    pidPower
                } else
                    0.0
            }
            else -> 0.0
        }

    }

    fun yToPosition(targetPos: Int) {
        val adjustedTarget = targetPos.coerceIn(yLimits)

        hardware.liftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        val direction = posOrNeg(targetPos)

        hardware.liftMotor.power = yPID.calcPID(adjustedTarget.toDouble(), hardware.liftMotor.currentPosition.toDouble())

        hardware.liftMotor.targetPosition = 0
        hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

        hardware.liftMotor.targetPosition = adjustedTarget

        while (hardware.liftMotor.isBusy) {
            hardware.liftMotor.power = yPID.calcPID(adjustedTarget.toDouble(), hardware.liftMotor.currentPosition.toDouble())
        }

        hardware.liftMotor.power = 0.0
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

    val hardware = MinibotHardware()
    val depositor = Depositor(hardware, telemetry)

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