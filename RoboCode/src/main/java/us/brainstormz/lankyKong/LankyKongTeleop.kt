package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import kotlin.math.abs

@TeleOp(name= "Lanky Kong Teleop", group= "B")
class LankyKongTeleop: OpMode() {

    val hardware = LankyKongHardware() /** Change Depending on robot */
    val movement = MecanumDriveTrain(hardware)

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
    }

    override fun loop() {
        /** TELE-OP PHASE */

//        DRONE DRIVE
        val yInput = gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()

        val y = -yInput
        val x = xInput
        val r = -rInput * abs(rInput)
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