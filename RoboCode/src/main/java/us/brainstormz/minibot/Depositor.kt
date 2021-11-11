package us.brainstormz.minibot

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.choivico.pid.PID
import java.lang.Thread.sleep

class Depositor(private val hardware: MinibotHardware, private val telemetry: Telemetry) {
    enum class XPosition {
        Extend,
        Retract
    }

    private val yPID = PID()
    private val yPower = 0.8
    private val yLimits: IntRange = 10..1425

    private val xPID = PID()
    private val xPower = 0.1

    private val dropperOpen = 0.1
    private val dropperClosed = 0.5

    fun move(x: XPosition?, y: Int) {
//        y Axis
        if (y != 0) {
//            hardware.liftMotor.power = yPID.calcPID(y.toDouble(), hardware.liftMotor.currentPosition.toDouble())
            hardware.liftMotor.power = yPower
            setLiftRunToPosition()

            telemetry.addLine("target Y: $y")
            val adjustedY = y.coerceIn(yLimits)
            telemetry.addLine("adjusted Y: $adjustedY")
            hardware.liftMotor.targetPosition = adjustedY
            telemetry.addLine("target Pos: ${hardware.liftMotor.targetPosition}")
            telemetry.addLine("current Pos: ${hardware.liftMotor.currentPosition}")
        }
//        x Axis
        hardware.horiServo.power = xPower

        var atPosition = false
        while (atPosition) {
            telemetry.addLine("current Pos: ${hardware.liftMotor.currentPosition}")
            val xAtPosition = when (x) {
                XPosition.Extend -> hardware.horiExtendLimit.isPressed
                XPosition.Retract -> hardware.horiRetractLimit.isPressed
                else -> true
            }
            val yAtPosition = hardware.liftMotor.isBusy
            atPosition = xAtPosition && yAtPosition

//            hardware.liftMotor.power = yPID.calcPID(y.toDouble(), hardware.liftMotor.currentPosition.toDouble())
//            hardware.horiServo.power = xPID.calcPID()
        }
        hardware.liftMotor.power = 0.0
        hardware.horiServo.power = 0.0
    }

    fun drop() {
        hardware.dropperServo.position = dropperOpen
        sleep(400)
        hardware.dropperServo.position = dropperClosed
    }

    fun deposit(x: XPosition, y: Int) {
        move(x, y)
        drop()
    }

    fun home() {
        hardware.dropperServo.position = dropperClosed
        move(XPosition.Retract, yLimits.first)
    }

    private fun setLiftRunToPosition() {
        if (hardware.liftMotor.mode != DcMotor.RunMode.RUN_TO_POSITION) {
            hardware.liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            // When the encoder is reset, also reset the target position, so it doesn't add an old
            // target position when using driveAddTargetPosition()
            hardware.liftMotor.targetPosition = 0
            hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION
        }
    }
}

@TeleOp(name="Depositor Tuner", group="Minibot")
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
