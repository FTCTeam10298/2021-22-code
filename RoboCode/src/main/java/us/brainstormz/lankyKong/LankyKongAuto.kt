package us.brainstormz.lankyKong

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.lankyKong.DepositorLK.DropperPos
import us.brainstormz.lankyKong.DepositorLK.LiftPos
import us.brainstormz.telemetryWizard.GlobalConsole

@Autonomous(name= "Lanky Kong Auto", group= "A")
class LankyKongAuto: LinearOpMode() {

    val console = GlobalConsole.newConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = LankyKongHardware()
    val movement = EncoderDriveMovement(hardware, console)
    lateinit var depo: DepositorLK

    val opencv = OpenCvAbstraction(this)

    var distanceInWarehouse = 0.0

    override fun runOpMode() {
        hardware.cachingMode = LynxModule.BulkCachingMode.OFF
        hardware.init(hardwareMap)
        depo = DepositorLK(hardware)
        depo.runInLinearOpmode(this)

//        opencv.init(hardwareMap)
//        opencv.cameraName = hardware.cameraName
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_RIGHT
//        opencv.start()
//        opencv.onNewFrame(tseDetector::processFrame)

        wizard.newMenu("Alliance", "Which alliance are we on?", listOf("Blue", "Red"), "StartPos", firstMenu = true)
        wizard.newMenu("StartPos", "Which are we closer to?", listOf("Warehouse", "Ducc"))
//        wizard.newMenu("ParkLocation", "Where to park?", listOf("Warehouse", "Storage Unit"))
        wizard.summonWizard(gamepad1)

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

//        opencv.stop()
//        val level: DepositorLK.LiftPos = when (tsePosition) {
//            TeamScoringElementDetector.TSEPosition.One -> Depositor.LiftPos.LowGoal
//            TeamScoringElementDetector.TSEPosition.Two -> Depositor.LiftPos.MidGoal
//            TeamScoringElementDetector.TSEPosition.Three -> Depositor.LiftPos.HighGoal
//        }

        /**
         * Auto Sequence
         * */
        when {
            wizard.wasItemChosen("Alliance", "Red") -> {
                val alliance = AutoTeleopTransitionLK.Alliance.Red
                AutoTeleopTransitionLK.alliance = alliance
                when {
                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
                        cycleAuto(alliance, startTime)
                    }
                    wizard.wasItemChosen("StartPos", "Ducc") -> {
//                        deliver preload
                        val preloadTurn = 42.0
                        synchronousDeposit(LiftPos.HighGoal.counts, 4500, syncAction = {
                            movement.driveRobotStrafe(0.8, 25.0, true)
                            movement.driveRobotTurn(1.0, preloadTurn, true)
                        })
                        synchronousRetract{}
//                        Spin ducc
                        movement.driveRobotTurn(1.0, -preloadTurn, true)
                        movement.driveRobotPosition(1.0, -25.0, true)
                        movement.driveRobotTurn(1.0, 90.0, true)
                        movement.driveRobotStrafe(1.0, -13.0, false)
                        sleep(700)
                        val frontDistance = hardware.frontDistance.getDistance(DistanceUnit.INCH)
                        val targetDistance = 9
                        movement.driveRobotPosition(1.0, frontDistance-targetDistance, true)
                        hardware.duccSpinner1.power = 1.0
                        sleep(3000)
                        hardware.duccSpinner1.power = 0.0
//                        Park
                        val target = -25.0
                        val disantsFormWall = hardware.frontDistance.getDistance(DistanceUnit.INCH)
                        movement.driveRobotPosition(1.0, target + disantsFormWall, true)
//                        movement.driveRobotPosition(1.0, -15.0, true)
                    }
                }
            }
            wizard.wasItemChosen("Alliance", "Blue") -> {
                val alliance = AutoTeleopTransitionLK.Alliance.Blue
                AutoTeleopTransitionLK.alliance = alliance
                when {
                    wizard.wasItemChosen("StartPos", "Warehouse") -> {
                        cycleAuto(alliance, startTime)
                    }
                    wizard.wasItemChosen("StartPos", "Ducc") -> {

                    }
                }
            }
        }
    }

    fun cycleAuto(alliance: AutoTeleopTransitionLK.Alliance, startTime: Long) {
        val allianceMultiplier = when(alliance) {
            AutoTeleopTransitionLK.Alliance.Red -> 1
            AutoTeleopTransitionLK.Alliance.Blue -> -1
        }

//        val initDistance = 38
//        movement.driveRobotPosition(1.0, (initFrontDistance - initDistance), false)
        val preloadStrafe = 25.0
        val preloadTurn = -50.0

        synchronousDeposit(
            liftHeight = LiftPos.HighGoal.counts,
            extensionLength = 5000,
            syncAction = {
                movement.driveRobotStrafe(0.8, preloadStrafe, true)
                movement.driveRobotTurn(1.0, preloadTurn * allianceMultiplier, true) })

        synchronousRetract {
            movement.driveRobotTurn(1.0, -preloadTurn * allianceMultiplier, true)
            movement.driveRobotStrafe(0.8, -(preloadStrafe + 3), true) }

//        collect block
        val preCollectMovement = 25.0
        movement.driveRobotPosition(1.0, preCollectMovement * allianceMultiplier, true)

//        collect a block
        collect(alliance)

//        Cycle
        cycle(alliance, startTime)
    }

    /**
     * Functions
     * */
    fun cycle(alliance: AutoTeleopTransitionLK.Alliance, startTime: Long) {
        val allianceMultiplier = when(alliance) {
            AutoTeleopTransitionLK.Alliance.Red -> 1
            AutoTeleopTransitionLK.Alliance.Blue -> -1
        }

        /**
         * Cycles
         * */
        val cycleTime = 10
        var remainingTime = System.currentTimeMillis() - startTime
        while (remainingTime > 30 - cycleTime && opModeIsActive()) {
//                            align to wall
            alignToWall()
//                            drive to hub while extending
            synchronousDeposit(
                liftHeight = LiftPos.HighGoal.counts,
                extensionLength = 6500,
                syncAction = {
                    alignToWall()
                    val targetDistance = 67
                    movement.driveRobotPosition(1.0, (distanceInWarehouse - targetDistance) * allianceMultiplier, true)
                })

//                            drive to warehouse while retracting
            synchronousRetract {
                alignToWall()
                movement.driveRobotPosition(1.0, 55.0 * allianceMultiplier, true) }

//                            collect
            collect(alliance)

            remainingTime = System.currentTimeMillis() - startTime
            console.display(4, "Remaining Time: $remainingTime")
        }
    }

    fun alignToWall() {
        movement.driveRobotStrafe(1.0, -5.0, false)
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
        sleep(200)
        hardware.dropperServo.position = DropperPos.Closed.posValue

    }

    fun synchronousRetract(syncAction: ()->Unit) {
        val syncThread = Thread {
            console.display(11, "Thread running.")
        }

//        start lowering
        depo.moveToPosition(
            yPosition = hardware.liftMotor.currentPosition,
            xPosition = depo.outWhileMovingPos)

//        syncThread.start()
        syncAction()

        depo.moveToPosition(
            yPosition = 350,
            xPosition = depo.outWhileMovingPos)


        depo.moveToPosition(
            yPosition = 350,
            xPosition = depo.xFullyRetracted)

//        make sure it's down
        depo.moveToPosition(
            yPosition = depo.fullyDown,
            xPosition = depo.xFullyRetracted)
//        syncThread.join()
    }

    fun collect(alliance: AutoTeleopTransitionLK.Alliance) {
        var allianceMultiplier = 1
        var collectorMotor = hardware.collector
        var forwardDistanceSensor = hardware.frontDistance

        if (AutoTeleopTransitionLK.alliance == AutoTeleopTransitionLK.Alliance.Blue) {
            allianceMultiplier = -1
            collectorMotor = hardware.collector2
            forwardDistanceSensor = hardware.backDistance
        }

        collectorMotor.power = 1.0
        movement.driveSetMode(DcMotor.RunMode.RUN_USING_ENCODER)
        val creepPower = 0.1 * allianceMultiplier
        while (hardware.dropperColor.alpha() < hardware.colorThreshold && opModeIsActive()){
            movement.drivePowerAll(creepPower)
            if (collectorMotor.isOverCurrent) {
                collectorMotor.power = -1.0
                sleep(700)
                collectorMotor.power = 1.0
                movement.drivePowerAll(0.0)
            }
        }
        movement.drivePowerAll(0.0)
        hardware.collector.power = -1.0
        hardware.collector2.power = -1.0
        sleep(800)
        distanceInWarehouse = forwardDistanceSensor.getDistance(DistanceUnit.INCH)
        hardware.collector.power = 0.0
        hardware.collector2.power = 0.0

    }

    fun isDepoHome(): Boolean {
        return depo.moveTowardPosition(
            yPosition = depo.fullyDown,
            xPosition = depo.xFullyRetracted)
    }
}
