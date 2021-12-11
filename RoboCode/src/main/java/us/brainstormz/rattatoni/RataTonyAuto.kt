package us.brainstormz.rattatoni

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.hardwareClasses.JamesEncoderMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.rattatoni.TeamScoringElementDetector.TSEPosition

@Autonomous(name="Minibot Auto", group="Minibot")
class RataTonyAuto: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = RataTonyHardware()
    val movement = EncoderDriveMovement(hardware, console)
    val jovement = JamesEncoderMovement(hardware,console)
    val depositor = Depositor(hardware)


    val opencv = OpenCvAbstraction(this)
    val tseDetector = TeamScoringElementDetector(console)
    var tsePosition = TSEPosition.One

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        console.display(1, "Initialization Complete")
        waitForStart()
        /** AUTONOMOUS  PHASE */

//        deliver
        movement.driveRobotStrafe(1.0, 5.0, true)
        movement.driveRobotTurn(1.0,40.0,true)
        depositor.yToPosition(depositor.midGoalHeight)
        movement.driveRobotStrafe(1.0, 11.0, true)
        movement.driveRobotPosition(1.0, 5.0, true)
        movement.driveRobotStrafe(1.0, 15.0, true)
        movement.driveRobotPosition(1.0, -9.0, true)
        movement.driveRobotStrafe(1.0, 10.5, true)
        hardware.horiServo.power = 1.0
        sleep(2000)
        hardware.horiServo.power = 0.0
        depositor.drop()
        hardware.horiServo.power = -1.0
        sleep(2000)
        hardware.horiServo.power = 0.0

//        go to warehouse
        
    }
}
