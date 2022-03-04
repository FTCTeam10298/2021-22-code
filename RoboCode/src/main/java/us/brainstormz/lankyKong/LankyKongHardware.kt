package us.brainstormz.lankyKong

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import org.outoftheboxrobotics.neutrinoi2c.MB1242.AsyncMB1242
import us.brainstormz.hardwareClasses.SmartLynxModule
import us.brainstormz.hardwareClasses.HardwareClass
import us.brainstormz.hardwareClasses.MecanumHardware
import java.lang.Thread.sleep

class LankyKongHardware: HardwareClass, MecanumHardware {
    override lateinit var hwMap: HardwareMap

    lateinit var allHubs: List<LynxModule>
    private lateinit var ctrlHub: SmartLynxModule
    private lateinit var exHub: SmartLynxModule
    var cachingMode = LynxModule.BulkCachingMode.MANUAL


//    Drivetrain
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    lateinit var frontDistance: AsyncMB1242

//    Depositor
    lateinit var liftMotor: DcMotorEx
    lateinit var horiMotor: DcMotorEx
    lateinit var dropperServo: Servo

//    Collectors
    lateinit var collector: DcMotor
    lateinit var collector2: DcMotor

//    Ducc Spinners
//    lateinit var duccSpinner1: CRServo
//    lateinit var duccSpinner2: CRServo

    val cameraName = "Webcam 1"

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap
        allHubs = hwMap.getAll(LynxModule::class.java)
        allHubs.forEach { hub ->    hub.bulkCachingMode = cachingMode
            if (hub.isParent && LynxConstants.isEmbeddedSerialNumber(hub.serialNumber))
                ctrlHub = SmartLynxModule(hub)
            else
                exHub = SmartLynxModule(hub) }

//        Drivetrain
        lFDrive = ctrlHub.getMotor(1) as DcMotorEx // black
        rFDrive = ctrlHub.getMotor(0) as DcMotorEx // pink
        lBDrive = ctrlHub.getMotor(2) as DcMotorEx // blue
        rBDrive = ctrlHub.getMotor(3) as DcMotorEx // green

        rFDrive.direction = DcMotorSimple.Direction.REVERSE
        lFDrive.direction = DcMotorSimple.Direction.FORWARD
        rBDrive.direction = DcMotorSimple.Direction.REVERSE
        lBDrive.direction = DcMotorSimple.Direction.FORWARD

        rFDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        lFDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        rBDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        lBDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        rFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        lFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        rBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        lBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        rFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        frontDistance = hwMap["frontDistance"] as AsyncMB1242
        frontDistance.enable()

//        Depositor
        liftMotor = exHub.getMotor(2) as DcMotorEx
        liftMotor.direction = DcMotorSimple.Direction.FORWARD
        liftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        liftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER

        horiMotor = exHub.getMotor(3) as DcMotorEx
        horiMotor.direction = DcMotorSimple.Direction.FORWARD
        horiMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        horiMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        horiMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        dropperServo = exHub.getServo(0) as Servo
        dropperServo.direction = Servo.Direction.REVERSE
        dropperServo.position = DepositorLK.DropperPos.Closed.posValue


//        Collectors

        collector = exHub.getMotor(0) as DcMotor
        collector.direction = DcMotorSimple.Direction.REVERSE
        collector.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        collector2 = exHub.getMotor(1) as DcMotor
        collector2.direction = DcMotorSimple.Direction.REVERSE
        collector2.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

//        Ducc Spinners
//        duccSpinner1 = hwMap["duccSpinner"] as CRServo
//        duccSpinner1.direction = DcMotorSimple.Direction.FORWARD
//
//        duccSpinner2 = hwMap["duccSpinner2"] as CRServo
//        duccSpinner2.direction = DcMotorSimple.Direction.FORWARD
    }

    fun clearHubCache() {
        allHubs.forEach {
            it.clearBulkCache()
        }
    }
}