package us.brainstormz.brian

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.choivico.robotCode.hardwareClasses.EncoderDriveMovement
import us.brainstormz.choivico.telemetryWizard.TelemetryConsole
import kotlin.math.PI

@Autonomous
class BrianAuto: LinearOpMode() {
    val hardware = BrianHardware()
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))
    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
        movement.driveRobotStrafe(power = 1.0, inches = 4.0, smartAccel = true)
        movement.driveRobotTurn(power = 1.0, degree = 120.0, smartAccel = true)
//        movement.driveRobotPosition(power = 1.0, inches = 36.0, smartAccel = true)
//        movement.driveRobotStrafe(power = 1.0, inches = 0.0, smartAccel = true)
//        movement.driveRobotTurn(power = 1.0, degree = 120.0, smartAccel = true)
    }

}