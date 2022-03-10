package us.brainstormz.lankyKong

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.Range
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.localizer.EncoderLocalizer
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.rataTony.AutoTeleopTransition.Alliance
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.lankyKong.DepositorLK.DropperPos
import us.brainstormz.lankyKong.DepositorLK.LiftPos
import us.brainstormz.localizer.PositionAndRotation

@Autonomous(name= "Lanky Kong Auto", group= "A")
class LankyKongAuto: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = LankyKongHardware()
    val oldMovement = EncoderDriveMovement(hardware, console)
    val movement = MecanumMovement(EncoderLocalizer(hardware, console), hardware, console)
    lateinit var depo: DepositorLK

    val opencv = OpenCvAbstraction(this)
    override fun runOpMode() {
        hardware.cachingMode = LynxModule.BulkCachingMode.OFF
        hardware.init(hardwareMap)
        depo = DepositorLK(hardware, console)
        depo.runInLinearOpmode(this)
        movement.linearOpMode = this

//        opencv.init(hardwareMap)
//        opencv.cameraName = hardware.cameraName
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_RIGHT
//        opencv.start()
//        opencv.onNewFrame(tseDetector::processFrame)

        wizard.newMenu("Alliance", "Which alliance are we on?", listOf("Blue", "Red"), "StartPos", firstMenu = true)
        wizard.newMenu("StartPos", "Which are we closer to?", listOf("Warehouse" to null, "Ducc" to "ParkLocation"))
        wizard.newMenu("ParkLocation", "Where to park?", listOf("Warehouse", "Storage Unit"))
//        wizard.summonWizard(gamepad1)

        var initBackDistance = 0.0
        var initFrontDistance = 0.0
        while(!opModeIsActive()) {
            initBackDistance = hardware.backDistance.getDistance(DistanceUnit.INCH)
            initFrontDistance = hardware.frontDistance.getDistance(DistanceUnit.INCH)
            console.display(2,
                            "Front Range: $initFrontDistance \nBack Range: $initBackDistance")
        }

        console.display(1, "Initialization Complete")
        waitForStart()

        val initDistance = 26.8
        oldMovement.driveRobotPosition(1.0, -(initBackDistance - initDistance), false)

//        opencv.stop()
//        val level: DepositorLK.LiftPos = when (tsePosition) {
//            TeamScoringElementDetector.TSEPosition.One -> Depositor.LiftPos.LowGoal
//            TeamScoringElementDetector.TSEPosition.Two -> Depositor.LiftPos.MidGoal
//            TeamScoringElementDetector.TSEPosition.Three -> Depositor.LiftPos.HighGoal
//        }

        oldMovement.driveRobotStrafe(0.8, 25.0, true)
        val preloadTurn = 30.0
        oldMovement.driveRobotTurn(1.0, preloadTurn, true)
        depo.moveToPosition(LiftPos.HighGoal.counts, 4500)
        hardware.dropperServo.position = DropperPos.Open.posValue
        sleep(100)
        hardware.dropperServo.position = DropperPos.Closed.posValue
        depo.moveToPosition(
            yPosition = LiftPos.HighGoal.counts,
            xPosition = depo.xFullyRetracted)
        depo.moveToPosition(
            yPosition = depo.fullyDown,
            xPosition = depo.xFullyRetracted)

//        Spin ducc
        oldMovement.driveRobotTurn(1.0, -preloadTurn, true)
        oldMovement.driveRobotPosition(1.0, -25.0, true)
        oldMovement.driveRobotTurn(1.0, 90.0, true)
        oldMovement.driveRobotStrafe(1.0, -13.0, false)
        sleep(700)
        val frontDistance = hardware.frontDistance.getDistance(DistanceUnit.INCH)
        val targetDistance = 9
        oldMovement.driveRobotPosition(1.0, frontDistance-targetDistance, true)
        hardware.duccSpinner1.power = 1.0
        sleep(1000)
//        hardware.duccSpinner1.power = 0.5
//        sleep(500)
        hardware.duccSpinner1.power = 0.1
        sleep(2000)
        /*
        val startTime = System.currentTimeMillis()
        while(System.currentTimeMillis() - startTime > 4000) {
            val dTime = System.currentTimeMillis() - startTime
            val timeToPower = Range.scale(dTime.toDouble(), 0.0, 4.0, 1.0, 0.0)
            hardware.duccSpinner1.power = timeToPower
        }*/
        hardware.duccSpinner1.power = 0.0

//        collect ducc
        hardware.collector.power = 1.0
        oldMovement.driveRobotStrafe(1.0, 8.0, true)
        oldMovement.driveRobotTurn(1.0,-50.0, true)
        oldMovement.driveRobotPosition(1.0, 20.0, true)
        oldMovement.driveRobotStrafe(1.0, -5.0, true)
        hardware.collector2.power = 1.0

//        Deliver Ducc
        oldMovement.driveRobotTurn(1.0,10.0, true)
        oldMovement.driveRobotStrafe(1.0, -5.0, false)
        oldMovement.driveRobotPosition(1.0, -10.0, true)
        hardware.collector.power = 0.0
        hardware.collector2.power = 0.0
        val backDistance = hardware.backDistance.getDistance(DistanceUnit.INCH)
        val hubLineup = 27
        oldMovement.driveRobotPosition(1.0, -(backDistance-hubLineup), true)
//        same as at the top
        oldMovement.driveRobotStrafe(0.8, 25.0, true)
        oldMovement.driveRobotTurn(1.0, preloadTurn, true)
        depo.moveToPosition(LiftPos.HighGoal.counts, 4500)
        hardware.dropperServo.position = DropperPos.Open.posValue
        sleep(100)
        hardware.dropperServo.position = DropperPos.Closed.posValue
        depo.moveToPosition(
            yPosition = LiftPos.HighGoal.counts,
            xPosition = depo.xFullyRetracted)
        depo.moveToPosition(
            yPosition = depo.fullyDown,
            xPosition = depo.xFullyRetracted)
    }

}