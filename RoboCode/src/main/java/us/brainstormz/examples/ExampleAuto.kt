package us.brainstormz.examples

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.choivico.robotCode.hardwareClasses.EncoderDriveMovement
import us.brainstormz.choivico.telemetryWizard.TelemetryConsole

@Autonomous
class ExampleAuto: LinearOpMode() {
    val hardware = ExampleHardware()
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))
    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        movement.driveRobotPosition(power = 1.0, inches = 20.0, smartAccel = true)
        movement.driveRobotStrafe(power = 1.0, inches = 20.0, smartAccel = true)
        movement.driveRobotTurn(power = 1.0, degree = 20.0, smartAccel = true)
    }

}