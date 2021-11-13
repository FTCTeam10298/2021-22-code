package us.brainstormz.rattatoni

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import kotlin.math.abs
import us.brainstormz.telemetryWizard.TelemetryConsole

@TeleOp(name="Minibot TeleOp", group="Minibot")
class MinibotTeleOp: OpMode() {

    val console = TelemetryConsole(telemetry)
    val hardware = MinibotHardware()

    val robot = MecanumDriveTrain(hardware)
    val depositor = Depositor(hardware, telemetry)

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
        val r = -(rInput *.5) * abs(rInput *.5)

        robot.driveSetPower(
                (y + x - r),
                (y - x + r),
                (y - x - r),
                (y + x + r)
        )

//        Depositor
        val xTarget = when {
            gamepad2.right_stick_x > 0.0f -> Depositor.XPosition.Extend
            gamepad2.right_stick_x < 0.0f -> Depositor.XPosition.Retract
            else -> null
        }
        depositor.xToPosition(xTarget)



        hardware.liftMotor.zeroPowerBehavior = if (gamepad2.right_stick_y == 0.0f)
            DcMotor.ZeroPowerBehavior.BRAKE
        else
            DcMotor.ZeroPowerBehavior.FLOAT

        val yTarget = gamepad2.right_stick_y.toInt()
        depositor.yInDirection(yTarget)


        if (gamepad2.right_trigger != 0.0f || gamepad2.left_trigger != 0.0f)
            depositor.drop()
        else
            depositor.close()


        if (gamepad2.a) {
            depositor.home()
        }


        if (gamepad2.b) {
            depositor.yToPosition(depositor.lowGoalHeight)
            depositor.xToPosition(Depositor.XPosition.Extend)
        }


        console.display(2, "y pos: ${hardware.liftMotor.currentPosition}")
        console.display(3, "x pos: ${depositor.xAbsPos}ed")
        console.display(4, "dropper: ${hardware.dropperServo.position}")


//        collector
        if (gamepad1.left_bumper || gamepad1.right_bumper || gamepad2.left_bumper || gamepad2.right_bumper) {
            hardware.collector.power = 0.6
        } else {
            hardware.collector.power = 0.0
        }


//        Ducc Spinner
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