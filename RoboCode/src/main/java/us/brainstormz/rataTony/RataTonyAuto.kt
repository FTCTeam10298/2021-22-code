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

        when {
            true /*wizard.wasItemChosen("Alliance", "Blue")*/ -> {
                AutoTeleopTransition.alliance = AutoTeleopTransition.Alliance.Blue
                when {
                    true /*wizard.wasItemChosen("StartPos", "Ducc")*/ -> {
//                        Deposit
                        movement.driveRobotStrafe(1.0, 10.0, true)
                        movement.driveRobotTurn(1.0, -35.0, true)
                        movement.driveRobotStrafe(1.0, 18.0, true)
                        movement.driveRobotPosition(1.0, 5.0, true)
                        if (level == LiftPos.LowGoal) {
                            deposit(1500, level)
                        } else {
                            deposit(1700, level)
                        }
//                        Ducc
                        movement.driveRobotTurn(1.0, 34.0, true)
                        movement.driveRobotPosition(1.0, 38.0, true)
                        movement.driveRobotStrafe(0.7, -20.0,true)
                        movement.driveRobotStrafe(0.5, -1.0,true)
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
//                        deposit
                        movement.driveRobotTurn(1.0, -20.0, true)
                        movement.driveRobotPosition(1.0, -30.0, true)
                        movement.driveRobotTurn(1.0, -110.0, true)
                        deposit(1400, LiftPos.HighGoal)
//                        park
                        movement.driveRobotTurn(1.0, 45.0, true)
                        movement.driveRobotPosition(1.0, 35.0, true)

                    }
                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
//                        Deposit
                        movement.driveRobotStrafe(1.0, 10.0, true)
                        movement.driveRobotTurn(1.0, 38.0, true)
                        movement.driveRobotStrafe(1.0, 22.0, true)
                        movement.driveRobotPosition(1.0, 2.0, true)
                        deposit(1700, level)
//                        Warehouse park
                        movement.driveRobotStrafe(1.0, -18.0, true)
                        movement.driveRobotTurn(1.0, -38.0, true)
                        movement.driveRobotStrafe(1.0, -12.0, true)
                        movement.driveRobotPosition(1.0,-40.0,true)
                    }
                }
            }
            wizard.wasItemChosen("Alliance", "Red") -> {
                AutoTeleopTransition.alliance = AutoTeleopTransition.Alliance.Red
                when {
                    wizard.wasItemChosen("StartPos", "Ducc") -> {
//                        Deposit
                        movement.driveRobotStrafe(1.0, 10.0, true)
                        movement.driveRobotTurn(1.0, 35.0, true)
                        movement.driveRobotStrafe(1.0, 22.0, true)
                        movement.driveRobotPosition(1.0, 2.0, true)
                        deposit(1700, level)
//                        Ducc
                        movement.driveRobotTurn(1.0, -34.0, true)
                        movement.driveRobotPosition(1.0, -38.0, true)
                        movement.driveRobotTurn(1.0,90.0,true)
                        movement.driveRobotPosition(0.5, 12.0,true)
                        movement.driveRobotStrafe(0.5,-2.0,true)
                        movement.driveRobotPosition(0.5, 2.0,false)
                        hardware.carouselSpinner.power = -1.0
                        movement.driveRobotPosition(0.1, -1.0, true)
                        sleep(4000)
                        hardware.carouselSpinner.power = 0.0
//                        park
                        movement.driveRobotPosition(1.0, -22.0, true)
                        movement.driveRobotStrafe(1.0, -10.0, true)
                    }
                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
//                        Deposit
                        movement.driveRobotStrafe(1.0, 10.0, true)
                        movement.driveRobotTurn(1.0, -35.0, true)
                        movement.driveRobotStrafe(1.0, 20.0, true)
                        deposit(1700, level)
//                        Warehouse park
                        movement.driveRobotStrafe(1.0,-18.0,true)
                        movement.driveRobotTurn(1.0, 35.0, true)
                        movement.driveRobotStrafe(1.0, -11.0, true)
                        movement.driveRobotPosition(1.0,30.0,true)
                    }
                }
            }
        }

    }

//    out, drop, in
    fun deposit(xCounts: Int, yLevel: LiftPos) {
        if (yLevel == LiftPos.HighGoal)
            depositor.yToPosition(yLevel.counts + 10)
        else
            depositor.yToPosition(yLevel.counts)
            sleep(100)
        depositor.xToPosition(xCounts)
        depositor.drop()
        sleep(500)
        depositor.close()
        depositor.xToPosition(0)
        depositor.xTowardPosition(depositor.innerLimit)
        depositor.yToPosition(depositor.lowerLimit)
        depositor.xAtPower(0.0)
    }
}
