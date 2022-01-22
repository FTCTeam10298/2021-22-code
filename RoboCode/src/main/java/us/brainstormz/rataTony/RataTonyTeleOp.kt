package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.rataTony.Depositor.LiftPos

/*
    ~~lift wiring (hw)~~
    ~~lift not moving~~
    ~~collector slower~~
    ~~freight slowers (hw)~~
    lift auto up
 */

@TeleOp(name="RataTony TeleOp", group="A")
class RataTonyTeleOp: OpMode() {

    val console = TelemetryConsole(telemetry)
    val hardware = RataTonyHardware()

    val robot = MecanumDriveTrain(hardware)
    val depositor = Depositor(hardware, console)
    var isStickButtonDown = false
    var driveReversed = 1

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun loop() {
        // Drive Polarity Switcher
        if (gamepad1.left_stick_button || gamepad1.right_stick_button && !isStickButtonDown) {
            isStickButtonDown = true
            driveReversed = -driveReversed
        } else if (!gamepad1.left_stick_button && !gamepad1.right_stick_button) {
            isStickButtonDown = false
        }

        // DRONE DRIVE
        fun isPressed(v:Float):Boolean = v > 0

        val yInput = gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()


        fun isNotCentered(v:Float) = v != 0.0f
        fun isNotCentered(v:Double) = v != 0.0
        val wallRiding = if (isPressed(gamepad1.right_trigger)) 0.3 else 0.0

        val y = -yInput * driveReversed
        val x = xInput * driveReversed + wallRiding
        val r = -rInput * .8

        robot.driveSetPower(
                (y + x - r),
                (y - x + r),
                (y - x - r),
                (y + x + r)
        )

        // Depositor
        when {
            gamepad2.left_stick_y != 0.0f ->
                depositor.yAtPower(-gamepad2.left_stick_y.toDouble())
            gamepad2.a ->
                depositor.home()
            gamepad2.y ->
                depositor.yTowardPosition(LiftPos.HighGoal.counts)
            else ->
                depositor.yAtPower(0.0)
        }

        if (gamepad2.right_trigger != 0.0f || gamepad2.left_trigger != 0.0f)
            depositor.drop()
        else
            depositor.close()

        depositor.xAtPower(gamepad2.right_stick_x.toDouble() * .9)

        depositor.updateYPosition()

        console.display(2, "y pos: ${hardware.liftMotor.currentPosition}")
        console.display(3, "x pos: ${hardware.horiMotor.currentPosition}")
        console.display(4, "dropper: ${hardware.dropperServo.position}")

        // Collector
        val forwardPower = 1.0
        val reversePower = 0.5

        when {
            gamepad1.right_bumper -> {
                if (driveReversed > 0) {
                    hardware.collector.power = forwardPower
                    hardware.collector2.power = -reversePower
                } else {
                    hardware.collector.power = -reversePower
                    hardware.collector2.power = forwardPower
                }
            }
            gamepad1.left_bumper -> {
                if (driveReversed < 0) {
                    hardware.collector.power = forwardPower
                    hardware.collector2.power = -reversePower
                } else {
                    hardware.collector.power = -reversePower
                    hardware.collector2.power = forwardPower
                }
            }
            else -> {
                hardware.collector.power = 0.0
                hardware.collector2.power = 0.0
            }
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