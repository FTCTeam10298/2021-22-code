package us.brainstormz.brian

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.choivico.robotCode.hardwareClasses.EncoderDriveMovement
import us.brainstormz.choivico.telemetryWizard.TelemetryConsole

@Autonomous
class BrianAuto: LinearOpMode() {
    val hardware = BrianHardware()
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))
    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        movement.driveRobotStrafe(power = 1.0, inches = 35.0, smartAccel = true)
        movement.driveRobotPosition(power = 1.0, inches = 180.0, smartAccel = true)
        //movement.driveRobotTurn(power = 1.0, degree = 120.0, smartAccel = true)
    }

}