package us.brainstormz.miniMouse

import com.qualcomm.hardware.rev.RevTouchSensor
import com.qualcomm.robotcore.hardware.*
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.rataTony.Depositor

class MiniMouseHardware: MecanumHardware {
    override lateinit var hwMap: HardwareMap

//    Drivetrain
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

//    Depositor
    lateinit var liftMotor: DcMotorEx
    lateinit var horiMotor: DcMotorEx
    lateinit var dropperServo: Servo
    lateinit var yLowerLimit: RevTouchSensor
    lateinit var xInnerLimit: RevTouchSensor

//    Collectors
    lateinit var collector: DcMotor
    lateinit var collector2: DcMotor

//    Ducc Spinners
    lateinit var duccSpinner1: CRServo
    lateinit var duccSpinner2: CRServo

    val cameraName = "Webcam 1"

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

//        Drivetrain
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
        liftMotor = hwMap["liftMotor"] as DcMotorEx
        liftMotor.direction = DcMotorSimple.Direction.REVERSE
        liftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        liftMotor.mode = DcMotor.RunMode.RUN_USING_ENCODER

        horiMotor = hwMap["horiMotor"] as DcMotorEx
        horiMotor.direction = DcMotorSimple.Direction.FORWARD
        horiMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        horiMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        horiMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        dropperServo = hwMap["dropper"] as Servo
        dropperServo.direction = Servo.Direction.REVERSE
        dropperServo.position = Depositor.DropperPos.Closed.posValue

//        xInnerLimit = hwMap["innerLimit"] as RevTouchSensor
//        yLowerLimit = hwMap["lowerLimit"] as RevTouchSensor

//        Collectors
        collector = hwMap["collector"] as DcMotor
        collector.direction = DcMotorSimple.Direction.FORWARD
        collector.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        collector2 = hwMap["collector2"] as DcMotor
        collector2.direction = DcMotorSimple.Direction.FORWARD
        collector2.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

//        Ducc Spinners
        duccSpinner1 = hwMap["duccSpinner"] as CRServo
        duccSpinner1.direction = DcMotorSimple.Direction.FORWARD

        duccSpinner2 = hwMap["duccSpinner2"] as CRServo
        duccSpinner2.direction = DcMotorSimple.Direction.FORWARD
    }
}