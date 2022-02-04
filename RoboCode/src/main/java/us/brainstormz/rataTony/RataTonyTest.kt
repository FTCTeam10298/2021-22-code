package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.rataTony.Depositor.LiftPos

@Autonomous(name="RataTony Test", group="A")
class RataTonyTest: LinearOpMode() {

    val console = TelemetryConsole(telemetry)

    val hardware = RataTonyHardware()
    val movement = EncoderDriveMovement(hardware, console)

    val depositor = Depositor(hardware, console)

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)


        console.display(1, "Initialization Complete")
        waitForStart()
        /** AUTONOMOUS  PHASE */
        hardware.carouselSpinner.power = 1.0
        movement.driveRobotStrafe(0.1, -1.0, true)
        sleep(2200)
        hardware.carouselSpinner.power = 0.08
        sleep(3500)
        hardware.carouselSpinner.power = 0.0
//                        collect
        hardware.collector.power = 1.0
        movement.driveRobotStrafe(1.0, 7.0, true)
        movement.driveRobotTurn(1.0, 90.0, true)
        movement.driveRobotPosition(1.0, 3.0, false)
        movement.driveRobotStrafe(1.0, 2.0, false)
        movement.driveRobotPosition(1.0, 3.0, false)
        movement.driveRobotStrafe(1.0, -5.0, true)
        movement.driveRobotPosition(1.0, 2.0, false)
        movement.driveRobotTurn(1.0, 16.0, true)
        movement.driveRobotPosition(1.0, 1.0, false)
        movement.driveRobotStrafe(1.0, -15.0, true)
        movement.driveRobotPosition(1.0, 1.0, false)
        sleep(200)
        hardware.collector.power = 0.0
        movement.driveRobotPosition(1.0, 5.0, false)

    }
}