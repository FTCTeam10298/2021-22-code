package us.brainstormz.minibot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.minibot.TeamScoringElementDetector.TSEPosition

@TeleOp(name="Minibot Auto", group="Minibot")
class MinibotAuto: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = MinibotHardware()
    val movement = EncoderDriveMovement(hardware, console)
    val depositor = Depositor(hardware, telemetry)


    val opencv = OpenCvAbstraction(this)
    val tseDetector = TeamScoringElementDetector(135, console)
    var tsePosition = TSEPosition.One

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        wizard.newMenu("Alliance", "What alliance are we on?", listOf("Blue", "Red"), firstMenu = true)

        opencv.optimizeView = true
        opencv.openCameraDeviceAsync = true
        opencv.cameraName = hardware.cameraName
        opencv.init(hardwareMap)
        opencv.start()
        opencv.onFirstFrame(tseDetector::init)
        opencv.onNewFrame(tseDetector::processFrame)

        wizard.summonWizard(gamepad1)

        console.display(1, "Initialization Complete")
        waitForStart()
        /** AUTONOMOUS  PHASE */

//        Store data and stop detector
        tsePosition = tseDetector.position
        opencv.stop()

        when {
            wizard.wasItemChosen("Alliance", "Blue") -> {
//                Drop starter block
                when (tsePosition) {
                    TSEPosition.One -> {}
                    TSEPosition.Two -> {}
                    TSEPosition.Three -> {}
                }
//                Spin ducc
//                collect ducc
//                deliver ducc
//                park
            }
            wizard.wasItemChosen("Alliance", "Red") -> {
//                Drop starter block
                when (tsePosition) {
                    TSEPosition.One -> {}
                    TSEPosition.Two -> {}
                    TSEPosition.Three -> {}
                }
//                Spin ducc
//                collect ducc
//                deliver ducc
//                park

            }
        }
//        movement.driveRobotPosition(power = 1.0, inches = 20.0, smartAccel = true)
//        movement.driveRobotStrafe(power = 1.0, inches = 20.0, smartAccel = true)
//        movement.driveRobotTurn(power = 1.0, degree = 20.0, smartAccel = true)
    }
}