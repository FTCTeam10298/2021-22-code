package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.rataTony.AutoTeleopTransition
import java.lang.Thread.sleep
import kotlin.math.abs

@TeleOp(name= "Lanky Kong Teleop", group= "B")
class LankyKongTeleop: OpMode() {

    val hardware = LankyKongHardware() /** Change Depending on robot */
    val movement = MecanumDriveTrain(hardware)

    var isStickButtonDown = false
    var driveReversed = if (AutoTeleopTransition.alliance == AutoTeleopTransition.Alliance.Red) 1 else -1

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
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



//        COLLECTOR
        val forwardPower = 1.0
        val reversePower = 0.5

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


    }


}