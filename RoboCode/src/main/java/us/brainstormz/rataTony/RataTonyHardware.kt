package us.brainstormz.rataTony

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevTouchSensor
import com.qualcomm.robotcore.hardware.*
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.SmartLynxModule
import us.brainstormz.rataTony.Depositor.DropperPos
import com.qualcomm.robotcore.hardware.configuration.LynxConstants




class RataTonyHardware: MecanumHardware {

    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    lateinit var collector: DcMotor
    lateinit var collector2: DcMotor

    lateinit var duccSpinner: CRServo

    lateinit var liftMotor: DcMotorEx
    lateinit var horiMotor: DcMotorEx
    lateinit var dropperServo: Servo
//    lateinit var yLowerLimit: RevTouchSensor
//    lateinit var xInnerLimit: RevTouchSensor

    val cameraName = "Webcam 1"

    override lateinit var hwMap: HardwareMap

    lateinit var allHubs: List<LynxModule>
    private lateinit var ctrlHub: SmartLynxModule
    private lateinit var exHub: SmartLynxModule

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap
        allHubs = hwMap.getAll(LynxModule::class.java)
        allHubs.forEach { hub ->    hub.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
                                    if (hub.isParent && LynxConstants.isEmbeddedSerialNumber(hub.serialNumber))
                                        ctrlHub = SmartLynxModule(hub)
                                    else
                                        exHub = SmartLynxModule(hub) }

//        Hardware Assignments
        lFDrive = ctrlHub.getMotor(0) as DcMotorEx
        rFDrive = ctrlHub.getMotor(1) as DcMotorEx
        lBDrive = ctrlHub.getMotor(2) as DcMotorEx
        rBDrive = ctrlHub.getMotor(3) as DcMotorEx
        dropperServo = ctrlHub.getServo(5) as Servo

        collector2 = exHub.getMotor(0) as DcMotor
        liftMotor = exHub.getMotor(1) as DcMotorEx
        collector = exHub.getMotor(2) as DcMotor
        horiMotor = exHub.getMotor(3) as DcMotorEx
        duccSpinner = exHub.getCRServo(1) as CRServo

        // DRIVE TRAIN
        rFDrive.direction = DcMotorSimple.Direction.FORWARD
        lFDrive.direction = DcMotorSimple.Direction.REVERSE
        rBDrive.direction = DcMotorSimple.Direction.FORWARD
        lBDrive.direction = DcMotorSimple.Direction.REVERSE

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

//        Depositor
        liftMotor.direction = DcMotorSimple.Direction.REVERSE
        liftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        liftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER

        horiMotor.direction = DcMotorSimple.Direction.FORWARD
        horiMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        horiMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        horiMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        dropperServo.direction = Servo.Direction.REVERSE
        dropperServo.position = DropperPos.Closed.posValue

//        xInnerLimit = hwMap["innerLimit"] as RevTouchSensor
//        yLowerLimit = hwMap["lowerLimit"] as RevTouchSensor

//        Collector
        collector.direction = DcMotorSimple.Direction.FORWARD
        collector.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        collector2.direction = DcMotorSimple.Direction.FORWARD
        collector2.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

//        Ducc
        duccSpinner.direction = DcMotorSimple.Direction.FORWARD
    }
}
