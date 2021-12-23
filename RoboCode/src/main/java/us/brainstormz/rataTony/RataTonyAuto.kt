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

@Autonomous(name="Minibot Auto", group="A")
class RataTonyAuto: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = RataTonyHardware()
    val movement = EncoderDriveMovement(hardware, console)
    val jovement = JamesEncoderMovement(hardware,console)
    val depositor = OldDepositor(hardware)


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

        wizard.newMenu("Alliance", "Which alliance are we on?", listOf("Blue", "Red"), "StartPos", firstMenu = true)
        wizard.newMenu("StartPos", "Which are we closer to?", listOf("Warehouse", "Ducc"))
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

        when {
            wizard.wasItemChosen("Alliance", "Blue") -> {
                when {
                    wizard.wasItemChosen("StartPos", "Ducc") -> {
                        movement.driveRobotPosition(1.0, -3.0, true)
                        movement.driveRobotStrafe(0.8, 50.0, true)
                        movement.driveRobotStrafe(1.0, -2.0, true)
                        movement.driveRobotTurn(1.0, -90.0, true)
                        movement.driveRobotStrafe(1.0, 3.5, true)
                        if (tsePosition != TSEPosition.One)
                            movement.driveRobotStrafe(1.0, 0.7, true)
                        depositor.yToPositionBlocking(level)
                        deposit(level)

                        if (tsePosition != TSEPosition.Three)
                            movement.driveRobotPosition(1.0, -7.0, true)

                        movement.driveRobotStrafe(1.0, -2.0, true)
                        movement.driveRobotTurn(1.0, 90.0, true)
                        movement.driveRobotPosition(0.5, 35.0, true)

                        if (tsePosition == TSEPosition.Three)
                            movement.driveRobotStrafe(0.3, -39.0, true)
                        else
                            movement.driveRobotStrafe(0.3, -32.0, true)

                        hardware.carouselSpinner.power = 1.0
                        sleep(4000)
                        hardware.carouselSpinner.power = 0.0

                        movement.driveRobotStrafe(1.0, 25.0, true)
                        movement.driveRobotPosition(1.0, 10.0, true)
                    }
                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
//        deliver
                        movement.driveRobotStrafe(1.0, 5.0, true)
                        movement.driveRobotTurn(1.0,40.0,true)
                        depositor.yToPosition(level)
//                        if (tsePosition == TSEPosition.Two) {
                            movement.driveRobotStrafe(0.8, 34.0, true)
                            movement.driveRobotTurn(1.0, -5.0, true)
//                        } else {
//                            movement.driveRobotStrafe(1.0, 11.0, true)
//                            movement.driveRobotPosition(1.0, 5.0, true)
//                            movement.driveRobotStrafe(1.0, 15.0, true)
//                            movement.driveRobotPosition(1.0, -10.0, true)
//                            movement.driveRobotStrafe(1.0, 9.0, true)
//                        }
//                        hardware.horiServo.power = 1.0
                        sleep(2000)
//                        hardware.horiServo.power = 0.0
                        sleep(1000)
                        depositor.drop()
                        sleep(1000)
                        depositor.yToPositionBlocking(level+60)
//                        hardware.horiServo.power = -1.0
                        sleep(1000)
                        depositor.close()
//                        hardware.horiServo.power = -1.0
                        sleep(1000)
//                        hardware.horiServo.power = 0.0
                        depositor.yToPosition(0)
//        go to warehouse
                        movement.driveRobotStrafe(1.0, -10.0, true)
                        movement.driveRobotTurn(1.0,-40.0,true)
                        movement.driveRobotStrafe(1.0, -37.0, true)
                        movement.driveRobotPosition(1.0,-49.0,true)
                    }
                }
            }
            wizard.wasItemChosen("Alliance", "Red") -> {
                when {
                    wizard.wasItemChosen("StartPos", "Ducc") -> {
                        movement.driveRobotPosition(1.0, 3.0, true)
                        if (tsePosition == TSEPosition.One) {
                            movement.driveRobotStrafe(0.8, 50.0, true)
                            movement.driveRobotStrafe(1.0, -2.0, true)
                        } else {
                            movement.driveRobotStrafe(0.8, 55.0, true)
                            movement.driveRobotStrafe(1.0, -7.0, true)
                        }

                        movement.driveRobotTurn(1.0, 90.0, true)
                        movement.driveRobotStrafe(1.0, 5.5, true)
                        if (tsePosition != TSEPosition.One)
                            movement.driveRobotStrafe(1.0, 1.0, true)
                        depositor.yToPosition(level)
                        deposit(level)

                        if (tsePosition != TSEPosition.One)
                            movement.driveRobotPosition(1.0, 7.0, true)

                        movement.driveRobotStrafe(0.8, -39.0, true)

                        if (tsePosition == TSEPosition.One)
                            movement.driveRobotPosition(0.4, 36.0, true)
                        else
                            movement.driveRobotPosition(0.4, 29.0, true)
                        movement.driveRobotPosition(0.2, 3.0, true)

                        hardware.carouselSpinner.power = -1.0
                        sleep(4000)
                        hardware.carouselSpinner.power = 0.0

                        movement.driveRobotPosition(1.0, -20.0, true)
                        movement.driveRobotStrafe(1.0, -5.0, true)
                    }
                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
        //          deliver
                        movement.driveRobotStrafe(1.0, 5.0, true)
                        movement.driveRobotTurn(1.0, -33.0, true)
                        depositor.yToPosition(level)
                        movement.driveRobotStrafe(1.0, 11.0, true)
                        movement.driveRobotPosition(1.0, -5.0, true)
                        movement.driveRobotStrafe(1.0, 15.0, true)
                        movement.driveRobotPosition(1.0, 12.0, true)

//                        if (tsePosition == TSEPosition.Two) {
//                            movement.driveRobotTurn(1.0, 10.0, true)
//                        }
                        movement.driveRobotStrafe(1.0, 8.0, true)

//                        hardware.horiServo.power = 1.0
                        sleep(2000)
//                        hardware.horiServo.power = 0.0
                        sleep(1000)
                        depositor.drop()
                        sleep(1000)
                        depositor.yToPositionBlocking(level + 100)
//                        hardware.horiServo.power = -1.0
                        sleep(1000)
                        depositor.close()
//                        hardware.horiServo.power = -1.0
                        sleep(1000)
//                        hardware.horiServo.power = 0.0
                        depositor.yToPosition(0)
        //        go to warehouse
                        movement.driveRobotStrafe(1.0, -10.0, true)
                        movement.driveRobotTurn(1.0, 40.0, true)
                        movement.driveRobotStrafe(1.0, -40.0, true)
                        movement.driveRobotTurn(1.0, 3.0, true)
                        movement.driveRobotPosition(1.0, 60.0, true)
                    }
                }
            }

        }

//        make sure lift is down
        depositor.yToPositionBlocking(300)
//        hardware.horiServo.power = 1.0
        sleep(500)
//        hardware.horiServo.power = -1.0
        sleep(1000)
//        hardware.horiServo.power = 0.0
        depositor.yToPositionBlocking(0)
    }

//    out, drop, home
    fun deposit(yLevel: Int) {
//        hardware.horiServo.power = 1.0
        sleep(2000)
//        hardware.horiServo.power = 0.0
        sleep(100)

        depositor.drop()
        sleep(1000)

        depositor.yToPositionBlocking(yLevel+100)
//        hardware.horiServo.power = -1.0
        sleep(800)
        depositor.close()
        sleep(1700)
//        hardware.horiServo.power = 0.0
        depositor.yToPosition(0)
    }
}
