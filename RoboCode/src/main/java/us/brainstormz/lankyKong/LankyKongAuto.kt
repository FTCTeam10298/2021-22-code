package us.brainstormz.lankyKong

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.lankyKong.DepositorLK.DropperPos
import us.brainstormz.lankyKong.DepositorLK.LiftPos

@Autonomous(name= "Lanky Kong Auto", group= "A")
class LankyKongAuto: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = LankyKongHardware()
    val movement = EncoderDriveMovement(hardware, console)
    lateinit var depo: DepositorLK

    val opencv = OpenCvAbstraction(this)

    var distanceInWarehouse = 0.0

    override fun runOpMode() {
        hardware.cachingMode = LynxModule.BulkCachingMode.OFF
        hardware.init(hardwareMap)
        depo = DepositorLK(hardware, console)
        depo.runInLinearOpmode(this)

//        opencv.init(hardwareMap)
//        opencv.cameraName = hardware.cameraName
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_RIGHT
//        opencv.start()
//        opencv.onNewFrame(tseDetector::processFrame)

        wizard.newMenu("Alliance", "Which alliance are we on?", listOf("Blue", "Red"), "StartPos", firstMenu = true)
        wizard.newMenu("StartPos", "Which are we closer to?", listOf("Warehouse", "Ducc"))
//        wizard.newMenu("ParkLocation", "Where to park?", listOf("Warehouse", "Storage Unit"))
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
        val startTime = System.currentTimeMillis()


//        val initDistance = 26.8
//        oldMovement.driveRobotPosition(1.0, -(initBackDistance - initDistance), false)

//        opencv.stop()
//        val level: DepositorLK.LiftPos = when (tsePosition) {
//            TeamScoringElementDetector.TSEPosition.One -> Depositor.LiftPos.LowGoal
//            TeamScoringElementDetector.TSEPosition.Two -> Depositor.LiftPos.MidGoal
//            TeamScoringElementDetector.TSEPosition.Three -> Depositor.LiftPos.HighGoal
//        }
        when {
            true/*wizard.wasItemChosen("Alliance", "Red")*/ -> {
                when {
                    true/*wizard.wasItemChosen("StartPos", "Warehouse")*/ -> {

                        val initDistance = 38
                        movement.driveRobotPosition(1.0, (initFrontDistance - initDistance), false)
                        movement.driveRobotStrafe(0.8, 25.0, true)
                        val preloadTurn = -45.0
                        movement.driveRobotTurn(1.0, preloadTurn, true)
                        depo.moveToPosition(200, 5)
                        depo.moveToPosition(LiftPos.HighGoal.counts, 5000)
                        hardware.dropperServo.position = DropperPos.Open.posValue
                        sleep(100)
                        hardware.dropperServo.position = DropperPos.Closed.posValue
                        depo.moveToPosition(
                            yPosition = LiftPos.HighGoal.counts,
                            xPosition = depo.xFullyRetracted)
                        depo.moveToPosition(
                            yPosition = depo.fullyDown,
                            xPosition = depo.xFullyRetracted)
                        movement.driveRobotTurn(1.0, -preloadTurn, true)
                        movement.driveRobotStrafe(0.8, -28.0, true)
//                        collect block
                        collect()

//                        Cycles
                        val cycleTime = 6
                        while (System.currentTimeMillis() - startTime > 20 - cycleTime && opModeIsActive()) {
//                            align to wall
                            movement.driveRobotStrafe(1.0, -5.0, false)

//                            drive to hub while extending
                            synchronousDeposit(
                                liftHeight = LiftPos.HighGoal.counts,
                                extensionLength = 6500,
                                syncAction = {
                                    movement.driveRobotStrafe(1.0, -5.0, false)
                                    movement.driveRobotPosition(1.0, -50.0, true)
                                })

//                            drive to warehouse while retracting
                            synchronousRetract {
                                movement.driveRobotStrafe(0.7, -5.0, false)
                                movement.driveRobotPosition(1.0, 25.0, true) }

//                            collect
                            collect()
                        }
                    }
                    wizard.wasItemChosen("StartPos", "Ducc") -> {
//                        deliver preload
                        movement.driveRobotStrafe(0.8, 25.0, true)
                        val preloadTurn = 30.0
                        movement.driveRobotTurn(1.0, preloadTurn, true)
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
                        movement.driveRobotTurn(1.0, -preloadTurn, true)
                        movement.driveRobotPosition(1.0, -25.0, true)
                        movement.driveRobotTurn(1.0, 90.0, true)
                        movement.driveRobotStrafe(1.0, -13.0, false)
                        sleep(700)
                        val frontDistance = hardware.frontDistance.getDistance(DistanceUnit.INCH)
                        val targetDistance = 9
                        movement.driveRobotPosition(1.0, frontDistance-targetDistance, true)
                        hardware.duccSpinner1.power = 1.0
                        sleep(1000)
                        hardware.duccSpinner1.power = 0.1
                        sleep(2000)
                        hardware.duccSpinner1.power = 0.0
//        collect ducc
                        hardware.collector.power = 1.0
                        movement.driveRobotStrafe(1.0, 8.0, true)
                        movement.driveRobotTurn(1.0,-50.0, true)
                        movement.driveRobotPosition(1.0, 20.0, true)
                        movement.driveRobotStrafe(1.0, -5.0, true)
                        hardware.collector2.power = 1.0

//        Deliver Ducc
                        movement.driveRobotTurn(1.0,10.0, true)
                        movement.driveRobotStrafe(1.0, -5.0, false)
                        movement.driveRobotPosition(1.0, -10.0, true)
                        hardware.collector.power = 0.0
                        hardware.collector2.power = 0.0
                        val backDistance = hardware.backDistance.getDistance(DistanceUnit.INCH)
                        val hubLineup = 27
                        movement.driveRobotPosition(1.0, -(backDistance-hubLineup), true)
//        same as at the top
                        movement.driveRobotStrafe(0.8, 25.0, true)
                        movement.driveRobotTurn(1.0, preloadTurn, true)
                        depo.moveToPosition(LiftPos.HighGoal.counts, 4500)
                        hardware.dropperServo.position = DropperPos.Open.posValue
                        sleep(100)
                        hardware.dropperServo.position = DropperPos.Closed.posValue
                        depo.moveToPosition(
                            yPosition = LiftPos.HighGoal.counts,
                            xPosition = depo.xFullyRetracted)
                        depo.moveTowardPosition(
                            yPosition = depo.fullyDown,
                            xPosition = depo.xFullyRetracted)
                    }
                }
            }
            wizard.wasItemChosen("Alliance", "Blue") -> {
                when {
                    wizard.wasItemChosen("StartPos", "Warehouse") -> {

                    }
                    wizard.wasItemChosen("StartPos", "Ducc") -> {

                    }
                }
            }
        }
    }
    fun synchronousDeposit(liftHeight: Int, extensionLength: Int, syncAction: ()->Unit) {

//        start raising depo
        depo.moveToPosition(depo.preOutLiftPos.coerceAtMost(liftHeight), depo.xFullyRetracted)
        depo.moveTowardPosition(liftHeight, depo.outWhileMovingPos.coerceAtMost(extensionLength))

//        do an action while it's going up
        syncAction()

//        move fully out
        depo.moveToPosition(liftHeight, extensionLength)

//        drop
        hardware.dropperServo.position = DropperPos.Open.posValue
        sleep(100)
        hardware.dropperServo.position = DropperPos.Closed.posValue

    }

    fun synchronousRetract(syncAction: ()->Unit) {
//        start lowering
        depo.moveToPosition(
            yPosition = hardware.liftMotor.currentPosition,
            xPosition = depo.outWhileMovingPos)

        depo.moveTowardPosition(
            yPosition = depo.fullyDown,
            xPosition = depo.xFullyRetracted)

//        do an action while its going down
        syncAction()

//        make sure it's down
        if (!isDepoHome())
            depo.moveToPosition(
                yPosition = depo.fullyDown,
                xPosition = depo.xFullyRetracted)
    }

    fun collect() {
        hardware.collector.power = 1.0
        while (hardware.dropperColor.alpha() < hardware.colorThreshold && opModeIsActive()){
            if (hardware.collector.isOverCurrent) {
                hardware.collector.power = -1.0
                sleep(700)
                hardware.collector.power = 1.0
            }
            movement.driveRobotPosition(1.0, 3.0, false)
        }
        hardware.collector.power = -1.0
        hardware.collector2.power = -1.0
        sleep(700)
        distanceInWarehouse = hardware.frontDistance.getDistance(DistanceUnit.INCH)
        hardware.collector.power = 0.0
        hardware.collector2.power = 0.0

    }

    fun isDepoHome(): Boolean {
        return depo.moveTowardPosition(
            yPosition = depo.fullyDown,
            xPosition = depo.xFullyRetracted)
    }
}