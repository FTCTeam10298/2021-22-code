package us.brainstormz.minibot

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.choivico.robotCode.hardwareClasses.MecanumDriveTrain
import kotlin.math.abs
import us.brainstormz.choivico.telemetryWizard.TelemetryConsole

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
//        val y = yInput.pow(5)
//        val x = xInput.pow(5)
//        val r = rInput.pow(5) * 0.5 + 0.5 * rInput

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
//        Ducc Spinner
        if (gamepad1.y) {
            hardware.carouselSpinner.power = 1.0
        }else{
            hardware.carouselSpinner.power = 0.0
        }
    }
}