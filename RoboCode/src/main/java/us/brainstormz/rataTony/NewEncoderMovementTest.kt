package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.localizer.EncoderLocalizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.telemetryWizard.TelemetryConsole

@Autonomous(name= "NewEncoderMovementTest", group= "Tests")
class NewEncoderMovementTest: LinearOpMode() {
    val hardware = RataTonyHardware()
    val console = TelemetryConsole(telemetry)
    val movement = MecanumMovement(EncoderLocalizer(hardware, console), hardware, console)

    override fun runOpMode() {
        hardware.init(hardwareMap)
        movement.linearOpMode = this

        waitForStart()

        movement.goToPosition(PositionAndRotation(10.0, 10.0, 0.0))
        movement.goToPosition(PositionAndRotation(-10.0, 0.0, 0.0))
        movement.goToPosition(PositionAndRotation(0.0, -10.0, 0.0))
        movement.goToPosition(PositionAndRotation(r= 90.0))


//        movement.setSpeedAll(0.0, 1.0, 1.0, 0.0, 0.3)
//        val startTime = time
//        while (time - startTime < 20.0) {
//            movement.localizer.recalculatePositionAndRotation()
//            val currentPos = movement.localizer.currentPositionAndRotation()
//            console.display(1, "Current pos: $currentPos")
//        }
    }

}