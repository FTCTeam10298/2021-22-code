package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.telemetryWizard.TelemetryConsole

@TeleOp(name="Minibot TeleOp", group="Minibot")
class RataTonyTeleOp: OpMode() {

    val console = TelemetryConsole(telemetry)
    val hardware = RataTonyHardware()

    val robot = MecanumDriveTrain(hardware)
    val depositor = Depositor(hardware)

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun loop() {
        // DRONE DRIVE
        val yInput = gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()

        val y = -yInput
        val x = xInput
        val r = -rInput *.8

        robot.driveSetPower(
                (y + x - r),
                (y - x + r),
                (y - x - r),
                (y + x + r)
        )

        // Depositor
        if (gamepad2.right_stick_x > 0.1f || gamepad2.right_stick_x < -0.1f)
            depositor.state = 0

        hardware.horiServo.power = gamepad2.right_stick_x.toDouble()

        val yTarget = gamepad2.right_stick_y.toInt()
        if (yTarget != 0) {
            depositor.state = 0
        }
        if (depositor.state == 0)
            depositor.yInDirection(yTarget)

        if (gamepad2.right_trigger != 0.0f || gamepad2.left_trigger != 0.0f)
            depositor.drop()
        else
            depositor.close()
//        if (depositor.state == 1 && !hardware.liftMotor.isBusy) {
//            depositor.xToPosition(Depositor.XPosition.Extend)
//            depositor.state = 0
//        }

        console.display(2, "y pos: ${hardware.liftMotor.currentPosition}")
        console.display(3, "x pos: ${depositor.xAbsPos}ed")
        console.display(4, "dropper: ${hardware.dropperServo.position}")

        // Collector
        if (gamepad1.right_bumper || gamepad2.right_bumper) {
            hardware.collector.power = 1.0
        } else if (gamepad1.left_bumper || gamepad2.left_bumper) {
            hardware.collector.power = -1.0
        } else {
            hardware.collector.power = 0.0
        }

        // Ducc Spinner
        when {
            gamepad2.dpad_left -> {
                hardware.carouselSpinner.power = 1.0
            }
            gamepad2.dpad_right -> {
                hardware.carouselSpinner.power = -1.0
            }
            else -> {
                hardware.carouselSpinner.power = 0.0
            }
        }
    }
}