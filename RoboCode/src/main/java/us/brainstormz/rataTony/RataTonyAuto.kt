package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.hardwareClasses.JamesEncoderMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.rataTony.TeamScoringElementDetector.TSEPosition

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

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        opencv.init(hardwareMap)
        opencv.cameraName = hardware.cameraName
        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_RIGHT
        opencv.start()
        opencv.onNewFrame(tseDetector::processFrame)

        wizard.newMenu("Alliance", "Which alliance are we on?", listOf("Blue", "Red"), "Starting Position", firstMenu = true)
        wizard.newMenu("Starting Position", "Which side are we starting on?", listOf("Warehouse", "Ducc"))
        wizard.summonWizard(gamepad1)

        console.display(1, "Initialization Complete")
        waitForStart()
        /** AUTONOMOUS  PHASE */

        val tsePosition = tseDetector.position
        opencv.stop()

        val level = when (tsePosition) {
            TSEPosition.One -> depositor.lowGoalHeight
            else -> depositor.midGoalHeight
        }

        if (wizard.wasItemChosen("Alliance", "Blue")) {
//        deliver
            movement.driveRobotStrafe(1.0, 5.0, true)
            movement.driveRobotTurn(1.0,40.0,true)
            depositor.yToPosition(level)
            movement.driveRobotStrafe(1.0, 11.0, true)
            movement.driveRobotPosition(1.0, 5.0, true)
            movement.driveRobotStrafe(1.0, 15.0, true)
            movement.driveRobotPosition(1.0, -9.0, true)
            movement.driveRobotStrafe(1.0, 9.0, true)
            hardware.horiServo.power = 1.0
            sleep(2000)
            hardware.horiServo.power = 0.0
            sleep(1000)
            depositor.drop()
            sleep(1000)
            depositor.yToPositionBlocking(level+60)
            hardware.horiServo.power = -1.0
            sleep(1000)
            depositor.close()
            hardware.horiServo.power = -1.0
            sleep(1000)
            hardware.horiServo.power = 0.0
            depositor.yToPosition(depositor.state)
//        go to warehouse
            movement.driveRobotStrafe(1.0, -10.0, true)
            movement.driveRobotTurn(1.0,-40.0,true)
            movement.driveRobotStrafe(1.0, -37.0, true)
            movement.driveRobotPosition(1.0,-49.0,true)
        }
        if (wizard.wasItemChosen("Alliance", "Red")) {
//          deliver
            movement.driveRobotStrafe(1.0, 5.0, true)
            movement.driveRobotTurn(1.0,-33.0,true)
            depositor.yToPosition(level)
            movement.driveRobotStrafe(1.0, 11.0, true)
            movement.driveRobotPosition(1.0, -5.0, true)
            movement.driveRobotStrafe(1.0, 15.0, true)
            movement.driveRobotPosition(1.0, 6.5, true)
            movement.driveRobotStrafe(1.0, 8.0, true)
            hardware.horiServo.power = 1.0
            sleep(2000)
            hardware.horiServo.power = 0.0
            sleep(1000)
            depositor.drop()
            sleep(1000)
            depositor.yToPositionBlocking(level + 100)
            hardware.horiServo.power = -1.0
            sleep(1000)
            depositor.close()
            hardware.horiServo.power = -1.0
            sleep(1000)
            hardware.horiServo.power = 0.0
            depositor.yToPosition(depositor.state)
//        go to warehouse
            movement.driveRobotStrafe(1.0, -10.0, true)
            movement.driveRobotTurn(1.0,40.0,true)
            movement.driveRobotStrafe(1.0, -37.0, true)
            movement.driveRobotPosition(1.0,47.0,true)
        }
    }
}
