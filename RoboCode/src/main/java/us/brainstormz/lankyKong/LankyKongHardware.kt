package us.brainstormz.lankyKong

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.*
import us.brainstormz.hardwareClasses.SmartLynxModule
import us.brainstormz.hardwareClasses.HardwareClass

class LankyKongHardware: HardwareClass/*MecanumHardware*/ {
    override lateinit var hwMap: HardwareMap

    lateinit var allHubs: List<LynxModule>
    private lateinit var smartLynxModuleCtrl: SmartLynxModule
    private lateinit var smartLynxModuleEx: SmartLynxModule


//    Drivetrain
//    override lateinit var lFDrive: DcMotor
//    override lateinit var rFDrive: DcMotor
//    override lateinit var lBDrive: DcMotor
//    override lateinit var rBDrive: DcMotor
    lateinit var lFDrive: DcMotor
//    Depositor
//    lateinit var liftMotor: DcMotorEx
//    lateinit var horiMotor: DcMotorEx
//    lateinit var dropperServo: Servo
//    lateinit var yLowerLimit: RevTouchSensor
//    lateinit var xInnerLimit: RevTouchSensor

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
        smartLynxModuleCtrl = SmartLynxModule(allHubs[0])
//        smartLynxModuleEx = SmartLynxModule(allHubs[1])

//        Drivetrain
        lFDrive = smartLynxModuleCtrl.getMotor(0) as DcMotorEx
//        lFDrive = hwMap["lFDrive"] as DcMotorEx
//        rFDrive = hwMap["rFDrive"] as DcMotorEx
//        lBDrive = hwMap["lBDrive"] as DcMotorEx
//        rBDrive = hwMap["rBDrive"] as DcMotorEx

//        rFDrive.direction = DcMotorSimple.Direction.FORWARD
//        lFDrive.direction = DcMotorSimple.Direction.REVERSE
//        rBDrive.direction = DcMotorSimple.Direction.FORWARD
//        lBDrive.direction = DcMotorSimple.Direction.REVERSE
//
//        rFDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        lFDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        rBDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        lBDrive.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//
//        rFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//        lFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//        rBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//        lBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//
//        rFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        lFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        rBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        lBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

//        Depositor
//        liftMotor = hwMap["liftMotor"] as DcMotorEx
//        liftMotor.direction = DcMotorSimple.Direction.REVERSE
//        liftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        liftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER
//
//        horiMotor = hwMap["horiMotor"] as DcMotorEx
//        horiMotor.direction = DcMotorSimple.Direction.FORWARD
//        horiMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
//        horiMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//        horiMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//
//        dropperServo = hwMap["dropper"] as Servo
//        dropperServo.direction = Servo.Direction.REVERSE
//        dropperServo.position = Depositor.DropperPos.Closed.posValue

//        xInnerLimit = hwMap["innerLimit"] as RevTouchSensor
//        yLowerLimit = hwMap["lowerLimit"] as RevTouchSensor

//        Collectors

        collector = smartLynxModuleCtrl.getMotor(2) as DcMotorEx
//        collector = hwMap["collector"] as DcMotor
        collector.direction = DcMotorSimple.Direction.REVERSE
        collector.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        collector2 = smartLynxModuleCtrl.getMotor(1) as DcMotorEx
//        collector2 = hwMap["collector2"] as DcMotor
        collector2.direction = DcMotorSimple.Direction.REVERSE
        collector2.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

//        Ducc Spinners
//        duccSpinner1 = hwMap["duccSpinner"] as CRServo
//        duccSpinner1.direction = DcMotorSimple.Direction.FORWARD
//
//        duccSpinner2 = hwMap["duccSpinner2"] as CRServo
//        duccSpinner2.direction = DcMotorSimple.Direction.FORWARD
    }
}