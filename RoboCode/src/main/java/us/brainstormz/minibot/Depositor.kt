package us.brainstormz.minibot

import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.choivico.pid.PID
import java.lang.Thread.sleep

class Depositor(private val hardware: MinibotHardware) {
    enum class XPosition {
        Extend,
        Retract
    }

    private val yPID = PID()
    private val yPower = 0.5
    private val yLimits: IntRange = 1..8

    private val xPID = PID()
    private val xPower = 1.0

    private val dropperOpen = 0.1
    private val dropperClosed = 0.5

    fun move(x: XPosition?, y: Int) {
//        y Axis
//        hardware.liftMotor.power = yPID.calcPID(y.toDouble(), hardware.liftMotor.currentPosition.toDouble())
        hardware.liftMotor.power = yPower
        setLiftRunToPosition()

        val adjutedY = y.coerceIn(yLimits)
        hardware.liftMotor.targetPosition = adjutedY

//        x Axis
        hardware.horiServo.power = xPower

        var atPosition = false
        while (atPosition) {
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