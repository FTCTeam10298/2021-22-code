package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.hardwareClasses.JamesEncoderMovement
import us.brainstormz.localizer.EncoderLocalizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.rataTony.TeamScoringElementDetector.TSEPosition
import us.brainstormz.rataTony.Depositor.LiftPos
@Autonomous(name="RataTony Auto", group="A")
class RataTonyAuto: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = RataTonyHardware()
    val movement = EncoderDriveMovement(hardware, console)
    val depositor = Depositor(hardware,console)


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
        depositor.runInLinearOpmode(this)

        val tsePosition = tseDetector.position
        opencv.stop()

        val level: LiftPos = when (tsePosition) {
            TSEPosition.One -> LiftPos.LowGoal
            TSEPosition.Two -> LiftPos.MidGoal
            TSEPosition.Three -> LiftPos.HighGoal
        }

//        when {
//            wizard.wasItemChosen("Alliance", "Blue") -> {
//                when {
//                    wizard.wasItemChosen("StartPos", "Ducc") -> {
////                        Deposit
//                        movement.driveRobotStrafe(1.0, 10.0, true)
//                        movement.driveRobotTurn(1.0, -35.0, true)
//                        movement.driveRobotStrafe(1.0, 18.0, true)
//                        if (level == LiftPos.HighGoal)
//                            depositor.yToPosition(level.counts + 10)
//                        else
//                            depositor.yToPosition(level.counts)
//                        sleep(100)
//                        depositor.xToPosition(2400)
//                        depositor.drop()
//                        sleep(500)
//                        depositor.close()
//                        depositor.xToPosition(0)
//                        depositor.xTowardPosition(depositor.innerLimit)
//                        depositor.yToPosition(depositor.lowerLimit+10)
//                        depositor.xAtPower(0.0)
////                        Ducc
//                        movement.driveRobotTurn(1.0, 34.0, true)
//                        movement.driveRobotPosition(1.0, 38.0, true)
//                        movement.driveRobotStrafe(0.5, -13.0,true)
//                        movement.driveRobotStrafe(0.5, -1.0,true)
//                        hardware.carouselSpinner.power = 1.0
//                        movement.driveRobotStrafe(0.1, -1.0, true)
//                        sleep(4000)
//                        hardware.carouselSpinner.power = 0.0
////                        park
//                        movement.driveRobotStrafe(1.0, 25.0, true)
//                        movement.driveRobotPosition(1.0, 10.0, true)
//
//                    }
//                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
//                        Deposit
//                        movement.driveRobotStrafe(1.0, 10.0, true)
//                        movement.driveRobotTurn(1.0, 38.0, true)
//                        movement.driveRobotStrafe(1.0, 22.0, true)
//                        movement.driveRobotPosition(1.0, 2.0, true)
//                        if (level == LiftPos.HighGoal)
//                            depositor.yToPosition(level.counts + 10)
//                        else
//                            depositor.yToPosition(level.counts)
//                        sleep(100)
//                        depositor.xToPosition(2400)
//                        depositor.drop()
//                        sleep(500)
//                        depositor.close()
//                        depositor.xToPosition(0)
//                        depositor.xTowardPosition(depositor.innerLimit)
//                        sleep(2000)
//                        depositor.yToPosition(depositor.lowerLimit+10)
//                        depositor.xAtPower(0.0)
////                        Warehouse park
//                        movement.driveRobotStrafe(1.0, -18.0, true)
//                        movement.driveRobotTurn(1.0, -38.0, true)
//                        movement.driveRobotStrafe(1.0, -12.0, true)
//                        movement.driveRobotPosition(1.0,-40.0,true)
//                    }
//                }
//            }
//            wizard.wasItemChosen("Alliance", "Red") -> {
//                when {
//                    wizard.wasItemChosen("StartPos", "Ducc") -> {
////                        Deposit
                        movement.driveRobotStrafe(1.0, 10.0, true)
                        movement.driveRobotTurn(1.0, 35.0, true)
                        movement.driveRobotStrafe(1.0, 22.0, true)
                        movement.driveRobotPosition(1.0, -2.0, true)
                        if (level == LiftPos.HighGoal)
                            depositor.yToPosition(level.counts + 10)
                        else
                            depositor.yToPosition(level.counts)
                        sleep(100)
                        depositor.xToPosition(2400)
                        depositor.drop()
                        sleep(500)
                        depositor.close()
                        depositor.xToPosition(0)
                        depositor.xTowardPosition(depositor.innerLimit)
                        depositor.yToPosition(depositor.lowerLimit+10)
                        depositor.xAtPower(0.0)
//                        Ducc
                        movement.driveRobotTurn(1.0, -34.0, true)
                        movement.driveRobotPosition(1.0, -38.0, true)
                        movement.driveRobotTurn(1.0,90.0,true)
                        movement.driveRobotPosition(0.5, 13.0,true)
                        movement.driveRobotStrafe(0.5,-2.0,true)
                        movement.driveRobotPosition(0.5, -1.0,true)
                        hardware.carouselSpinner.power = -1.0
                        movement.driveRobotPosition(0.1, -1.0, true)
                        sleep(4000)
                        hardware.carouselSpinner.power = 0.0
//                        park
                        movement.driveRobotPosition(1.0, -25.0, true)
                        movement.driveRobotStrafe(1.0, -10.0, true)
//                    }
//                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
////                        Deposit
//                        movement.driveRobotStrafe(1.0, 10.0, true)
//                        movement.driveRobotTurn(1.0, -35.0, true)
//                        movement.driveRobotStrafe(1.0, 18.0, true)
////                        if (level == LiftPos.HighGoal)
////                            depositor.yToPosition(level.counts + 10)
////                        else
////                            depositor.yToPosition(level.counts)
////                        sleep(100)
////                        depositor.xToPosition(2400)
////                        depositor.drop()
////                        sleep(500)
////                        depositor.close()
////                        depositor.xToPosition(0)
////                        depositor.xTowardPosition(depositor.innerLimit)
////                        depositor.yToPosition(depositor.lowerLimit+10)
////                        depositor.xAtPower(0.0)
////                        Warehouse park
//                        movement.driveRobotStrafe(1.0,-18.0,true)
//                        movement.driveRobotTurn(1.0, 35.0, true)
//                        movement.driveRobotStrafe(1.0, -11.0, true)
//                        movement.driveRobotPosition(1.0,30.0,true)
//                    }
//                }
//            }

//        }
    }

//    out, drop, home
    fun deposit(yLevel: Int) {
        depositor.xToPosition(1000)

        depositor.drop()
        sleep(1000)

        depositor.yToPosition(yLevel+100)
        depositor.xToPosition(500)
        depositor.close()
        sleep(500)
        depositor.xToPosition(0)
        depositor.yToPosition(0)
    }
}
