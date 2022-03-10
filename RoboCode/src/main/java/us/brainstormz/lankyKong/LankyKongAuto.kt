package us.brainstormz.lankyKong

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
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

@Autonomous(name= "Lanky Kong Auto", group= "B")
class LankyKongAuto: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    val hardware = LankyKongHardware()
    val oldMovement = EncoderDriveMovement(hardware, console)
    val movement = MecanumMovement(EncoderLocalizer(hardware, console), hardware, console)
    lateinit var depo: DepositorLK

    val opencv = OpenCvAbstraction(this)
    override fun runOpMode() {
//        hardware.cachingMode = LynxModule.BulkCachingMode.OFF
        hardware.init(hardwareMap)


        hardware.clearHubCache()
        waitForStart()
        object:Thread(){
            override fun run() {
                while(true) {
                    hardware.dropperServo.position = DropperPos.Open.posValue
                }
            }
        }.start()

        depo = DepositorLK(hardware, console)
        depo.runInLinearOpmode(this)
        movement.linearOpMode = this

        while(true){
            println("Sleeping...")
            Thread.sleep(1000)
        }

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
//        val level: DepositorLK.LiftPos = when (tsePosition) {
//            TeamScoringElementDetector.TSEPosition.One -> Depositor.LiftPos.LowGoal
//            TeamScoringElementDetector.TSEPosition.Two -> Depositor.LiftPos.MidGoal
//            TeamScoringElementDetector.TSEPosition.Three -> Depositor.LiftPos.HighGoal
//        }

        oldMovement.driveRobotStrafe(1.0, 25.0, true)
        oldMovement.driveRobotTurn(1.0, 20.0, true)
        depo.moveToPosition(LiftPos.HighGoal.counts, 4000)
        hardware.dropperServo.position = DropperPos.Open.posValue
        sleep(1000)
        hardware.dropperServo.position = DropperPos.Closed.posValue
        depo.moveToPosition(
            yPosition = LiftPos.HighGoal.counts,
            xPosition = depo.xFullyRetracted)

        depo.moveToPosition(
            yPosition = depo.fullyDown,
            xPosition = depo.xFullyRetracted)

        oldMovement.driveRobotTurn(1.0, -20.0, true)
        oldMovement.driveRobotPosition(1.0, -25.0, true)
        oldMovement.driveRobotTurn(1.0, 90.0, true)
        oldMovement.driveRobotStrafe(1.0, -13.0, true)
        hardware.duccSpinner1.power = 1.0
        oldMovement.driveRobotPosition(0.3, 10.0,true)
        hardware.duccSpinner1.power = 0.0
    }

    //    out, drop, in
    fun deposit(xCounts: Int, yLevel: DepositorLK.LiftPos) {
        depo.moveTowardPosition(yLevel.counts.toDouble(), 0.0)
        sleep(100)
        depo.moveToPosition(yLevel.counts, xCounts)
        depo.dropper(DepositorLK.DropperPos.Open, false)
        sleep(500)
        depo.dropper(DepositorLK.DropperPos.Closed, false)
        depo.moveToPosition(yLevel.counts, 0)
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
//        movement.driveRobotHug(1.0, (-inchesToCollect + yAdjuster) * drivePolarity, true)

//        collectorMotor.power = inPower
//        movement.driveRobotPosition(0.3, 5.0, true)
//        collectorMotor.power = 0.0

/**        Stage 2: Deposit */
        yAdjuster = inchesToCollect - hardware.frontDistance.getDistance(DistanceUnit.INCH)

        val inchesToDeposit = 40.0 *2
//        movement.driveRobotHug(1.0, (inchesToDeposit + yAdjuster) * drivePolarity, true)
//        depo.deposit()
        sleep(1000)

    }

}