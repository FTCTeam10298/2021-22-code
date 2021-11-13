package us.brainstormz.rattatoni

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.rattatoni.TeamScoringElementDetector.TSEPosition

@Autonomous(name="Minibot Auto", group="Minibot")
class MinibotAuto: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = MinibotHardware()
    val movement = EncoderDriveMovement(hardware, console)
    val depositor = Depositor(hardware)


    val opencv = OpenCvAbstraction(this)
    val tseDetector = TeamScoringElementDetector(console)
    var tsePosition = TSEPosition.One

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        wizard.newMenu("Alliance", "What alliance are we on?", listOf("Blue", "Red"), "StartPos", firstMenu = true)
        wizard.newMenu("StartPos", "What starting position are we in?", listOf("Ducc Side", "Warehouse Side"))

        opencv.optimizeView = true
        opencv.openCameraDeviceAsync = true
        opencv.cameraOrientation = OpenCvCameraRotation.UPSIDE_DOWN
        opencv.cameraName = hardware.cameraName
        opencv.init(hardwareMap)
        opencv.start()
        opencv.onNewFrame(tseDetector::processFrame)

//        wizard.summonWizard(gamepad1)

        console.display(1, "Initialization Complete")
        waitForStart()
        /** AUTONOMOUS  PHASE */

//        Store data and stop detector
        tsePosition = tseDetector.position
        opencv.stop()

//        when {
//            wizard.wasItemChosen("Alliance", "Red") -> {
//                if (wizard.wasItemChosen("StartPos", "Ducc Side")) {
//                Drop starter block
                    movement.driveRobotTurn(1.0, 26.0, true)
                    movement.driveRobotPosition(power = 1.0, inches = -20.0, smartAccel = true)
                    movement.driveRobotTurn(1.0, -70.0, true)
                    movement.driveRobotStrafe(power = 1.0, inches = 4.0, smartAccel = true)
                    movement.driveRobotPosition(1.0, -5.0, true)
                    movement.driveRobotStrafe(1.0, 6.0, true)
                    

//                    movement.driveRobotStrafe(power = 1.0, inches = 15.0, smartAccel = true)
//                    movement.driveRobotTurn(power = 0.0, degree = -90.0, smartAccel = true)
//                    movement.driveRobotStrafe(power = 1.0, inches = -12.0, smartAccel = true)

                    when (tsePosition) {
                        TSEPosition.One -> depositor.yToPositionBlocking(depositor.lowGoalHeight)
                        TSEPosition.Two -> depositor.yToPositionBlocking(depositor.midGoalHeight)
                        TSEPosition.Three -> depositor.yToPositionBlocking(depositor.highGoalHeight)
                    }
                    sleep(1000)
                    depositor.xToPosition(Depositor.XPosition.Extend)
                    depositor.drop()
                    sleep(200)
                    depositor.home()
/*
//                Spin ducc
                    movement.driveRobotStrafe(power = 1.0, inches = 12.0, smartAccel = true)
                    movement.driveRobotTurn(power = 0.0, degree = 90.0, smartAccel = true)
                    movement.driveRobotStrafe(power = 1.0, inches = -47.0, smartAccel = true)
                    hardware.carouselSpinner.power = 1.0
//                collect ducc
                    hardware.collector.power = 1.0
                    movement.driveRobotStrafe(power = 1.0, inches = 47.0, smartAccel = true)
                    movement.driveRobotTurn(power = 0.0, degree = -90.0, smartAccel = true)
                    movement.driveRobotStrafe(power = 1.0, inches = -12.0, smartAccel = true)
//                deliver ducc
                    depositor.yToPosition(depositor.highGoalHeight)
                    depositor.xToPosition(Depositor.XPosition.Extend)
                    depositor.drop()
                    depositor.home()
//                park*/

//                } else {
//
//                }
//            }
//            wizard.wasItemChosen("Alliance", "Blue") -> {
//            }
//        }
    }
}