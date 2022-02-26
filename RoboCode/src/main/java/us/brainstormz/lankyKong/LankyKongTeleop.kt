package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.rataTony.AutoTeleopTransition
import us.brainstormz.telemetryWizard.TelemetryConsole
import java.lang.Thread.sleep

@TeleOp(name= "Lanky Kong Teleop", group= "B")
class LankyKongTeleop: OpMode() {

    val console = TelemetryConsole(telemetry)

    val hardware = LankyKongHardware() /** Change Depending on robot */
    val movement = MecanumDriveTrain(hardware)

    var isStickButtonDown = false
    var driveReversed = if (AutoTeleopTransition.alliance == AutoTeleopTransition.Alliance.Red) 1 else -1

    lateinit var depo: DepositorLK

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        depo = DepositorLK(hardware, console)
    }

    override fun start() {
//        movement.driveSetPower(1.0, 0.0, 0.0, 0.0)
//        sleep(1000)
//        movement.driveSetPower(0.0, 1.0, 0.0, 0.0)
//        sleep(1000)
//        movement.driveSetPower(0.0, 0.0, 1.0, 0.0)
//        sleep(1000)
//        movement.driveSetPower(0.0, 0.0, 0.0, 1.0)
//        sleep(1000)
    }

    override fun loop() {
        /** TELE-OP PHASE */
        hardware.clearHubCache()

//        DRONE DRIVE

        fun isPressed(v:Float):Boolean = v > 0
        val wallRiding = if (isPressed(gamepad1.right_trigger)) 0.5 else 0.0

        if (gamepad1.left_stick_button || gamepad1.right_stick_button && !isStickButtonDown) {
            isStickButtonDown = true
            driveReversed = -driveReversed
        } else if (!gamepad1.left_stick_button && !gamepad1.right_stick_button) {
            isStickButtonDown = false
        }

        val yInput = -gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = -gamepad1.right_stick_x.toDouble()

        val y = (yInput * driveReversed)
        val x = (xInput * driveReversed) + wallRiding
        val r = rInput * .9

        movement.driveSetPower((y + x - r),
                               (y - x + r),
                               (y - x - r),
                               (y + x + r))

        console.display(1, "Drive Encoders: \n ${hardware.lFDrive.currentPosition} \n ${hardware.rFDrive.currentPosition} \n ${hardware.lBDrive.currentPosition} \n ${hardware.rBDrive.currentPosition}")
        console.display(2, "Range: ${hardware.frontDistance.getDistance(DistanceUnit.INCH)}")
        console.display(3, "Horizontal Motor currPos: ${hardware.horiMotor.currentPosition}")
        console.display(4, "Lift Motor currPos: ${hardware.liftMotor.currentPosition}")
        console.display(5, "Lift Motor targetPos: ${hardware.liftMotor.targetPosition}")


//        COLLECTOR
        val forwardPower = 1.0
        val reversePower = 0.7

        when {
            gamepad1.right_bumper -> {
                hardware.collector.power = forwardPower
                hardware.collector2.power = -reversePower
            }
            gamepad1.left_bumper -> {
                hardware.collector.power = -reversePower
                hardware.collector2.power = forwardPower
            }
            else -> {
                hardware.collector.power = 0.0
                hardware.collector2.power = 0.0
            }
        }

//        DEPOSITOR
        depo.moveWithJoystick(gamepad2.left_stick_y.toDouble(), gamepad2.right_stick_x.toDouble())

        if (gamepad2.right_bumper)
            hardware.dropperServo.position = 1.0
        else
            hardware.dropperServo.position = 0.0

    }


}