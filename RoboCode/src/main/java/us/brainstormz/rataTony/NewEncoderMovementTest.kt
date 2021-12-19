package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.localizer.EncoderLocalizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement

@Autonomous(name= "NewEncoderMovementTest", group= "Tests")
class NewEncoderMovementTest: LinearOpMode() {
    val hardware = RataTonyHardware()
    val localizer = EncoderLocalizer(hardware)
    val movement = MecanumMovement(localizer, hardware)

    override fun runOpMode() {
        hardware.init(hardwareMap)

        waitForStart()

        while (opModeIsActive()) {
            localizer.recalculatePositionAndRotation()
            movement.move(PositionAndRotation(0.0, 10.0, 0.0), 0.0..1.0)
        }

    }

}