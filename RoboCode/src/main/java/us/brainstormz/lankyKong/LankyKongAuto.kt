package us.brainstormz.lankyKong

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.rataTony.AutoTeleopTransition.Alliance
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard

@Autonomous(name= "Lanky Kong Auto", group= "B")
class LankyKongAuto: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = LankyKongHardware()
    val movement = EncoderDriveMovement(hardware, console)
    lateinit var depo: DepositorLK

    val opencv = OpenCvAbstraction(this)
    override fun runOpMode() {
        hardware.cachingMode = LynxModule.BulkCachingMode.OFF
        hardware.init(hardwareMap)
        depo = DepositorLK(hardware, console)

//        opencv.init(hardwareMap)
//        opencv.cameraName = hardware.cameraName
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_RIGHT
//        opencv.start()
//        opencv.onNewFrame(tseDetector::processFrame)

        wizard.newMenu("Alliance", "Which alliance are we on?", listOf("Blue", "Red"), "StartPos", firstMenu = true)
        wizard.newMenu("StartPos", "Which are we closer to?", listOf("Warehouse" to null, "Ducc" to "ParkLocation"))
        wizard.newMenu("ParkLocation", "Where to park?", listOf("Warehouse", "Storage Unit"))
        wizard.summonWizard(gamepad1)

        console.display(1, "Initialization Complete")
        waitForStart()
//        opencv.stop()

//        val level: Depositor.LiftPos = when (tsePosition) {
//            TeamScoringElementDetector.TSEPosition.One -> Depositor.LiftPos.LowGoal
//            TeamScoringElementDetector.TSEPosition.Two -> Depositor.LiftPos.MidGoal
//            TeamScoringElementDetector.TSEPosition.Three -> Depositor.LiftPos.HighGoal
//        }
        val level: DepositorLK.LiftPos = DepositorLK.LiftPos.HighGoal

//        when {
//            wizard.wasItemChosen("Alliance", "Blue") -> {
//                movement.driveRobotStrafe(1.0, 10.0, true)
//                movement.driveRobotTurn(1.0, 20.0, true)
//            }
//            wizard.wasItemChosen("Alliance", "Red") -> {
//                movement.driveRobotStrafe(1.0, -10.0, true)
//                movement.driveRobotTurn(1.0, -20.0, true)
//            }
//        }
//
//        deposit(3000, level)
//
//        when {
//            wizard.wasItemChosen("Alliance", "Blue") -> {
//                movement.driveRobotTurn(1.0, -20.0, true)
//                movement.driveRobotStrafe(1.0, -11.0, true)
//                movement.driveRobotPosition(1.0, 36.0, true)
//
//            }
//            wizard.wasItemChosen("Alliance", "Red") -> {
//                movement.driveRobotTurn(1.0, 20.0, true)
//                movement.driveRobotStrafe(1.0, 11.0, true)
//                movement.driveRobotPosition(1.0, -36.0, true)
//
//            }
//        }

        when {
            wizard.wasItemChosen("Alliance", "Blue") -> {
                movement.driveRobotPosition(1.0, -40.0, true)
            }
            wizard.wasItemChosen("Alliance", "Red") -> {
                movement.driveRobotPosition(1.0, 40.0, true)
            }
        }

//        movement.driveRobotHug(1.0, -15.0 *2, true)

//        cycle(Alliance.Blue, 23.0)
////        (collect here)
//        console.display(1, "Collecting..")
//        sleep(2000)
//        movement.driveRobotHug(1.0, 30.0 *2, true)
//        movement.driveRobotPosition(1.0, 35.0, true)
////        (deposit here)
//        console.display(1, "Depositing...")
//        sleep(1000)
//        var cycles = 1
//        console.display(2, "Complete cycles: $cycles")
//        while (opModeIsActive()) {
////            movement.driveRobotHug(1.0, -40.0 *2, true)
//////        (collect here)
////            console.display(1, "Collecting..")
////            sleep(2000)
////            movement.driveRobotHug(1.0, 30.0 *2, true)
////            movement.driveRobotPosition(1.0, 35.0, true)
//////        (deposit here)
////            console.display(1, "Depositing...")
////            sleep(1000)
////
////            cycles++
////            console.display(2, "Complete cycles: $cycles")
//        }
    }

    //    out, drop, in
    fun deposit(xCounts: Int, yLevel: DepositorLK.LiftPos) {
        depo.moveTowardPosition(yLevel.counts.toDouble(), 0.0)
        sleep(100)
        depo.moveToPosition(yLevel.counts.toDouble(), xCounts.toDouble())
        depo.dropper(DepositorLK.DropperPos.Open, false)
        sleep(500)
        depo.dropper(DepositorLK.DropperPos.Closed, false)
        depo.moveToPosition(yLevel.counts.toDouble(), 0.0)
        depo.moveTowardPosition(0.0, 0.0)
    }

    fun cycle(side: Alliance, initalYIn: Double = 0.0) {
        val drivePolarity = when (side) { Alliance.Red -> -1
                                          Alliance.Blue -> 1 }
        val collectorMotor = when (side) { Alliance.Red -> hardware.collector
                                           Alliance.Blue -> hardware.collector2 }
        val inPower = 1.0

        var yAdjuster = -initalYIn

/**        Stage 1: Collect */
        val inchesToCollect = 40.0 *2
        movement.driveRobotHug(1.0, (-inchesToCollect + yAdjuster) * drivePolarity, true)

//        collectorMotor.power = inPower
        movement.driveRobotPosition(0.3, 5.0, true)
//        collectorMotor.power = 0.0

/**        Stage 2: Deposit */
        yAdjuster = inchesToCollect - hardware.frontDistance.getDistance(DistanceUnit.INCH)

        val inchesToDeposit = 40.0 *2
        movement.driveRobotHug(1.0, (inchesToDeposit + yAdjuster) * drivePolarity, true)
//        depo.deposit()
        sleep(1000)

    }

}