package us.brainstormz.rattatoni

import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.MecanumHardware

class RataTonyHardware: MecanumHardware {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor
    lateinit var carouselSpinner: CRServo
    lateinit var liftMotor: DcMotorEx
    lateinit var horiServo: CRServo
    lateinit var dropperServo: Servo
    lateinit var collector: DcMotor
    val cameraName = "Webcam 1"

    override lateinit var hwMap: HardwareMap

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

        // DRIVE TRAIN
        lFDrive = hwMap["lFDrive"] as DcMotorEx
        rFDrive = hwMap["rFDrive"] as DcMotorEx
        lBDrive = hwMap["lBDrive"] as DcMotorEx
        rBDrive = hwMap["rBDrive"] as DcMotorEx

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

        horiServo = hwMap["horiServo"] as CRServo
        horiServo.direction = DcMotorSimple.Direction.REVERSE
        horiServo.power = 0.0

        dropperServo = hwMap["dropper"] as Servo
        dropperServo.direction = Servo.Direction.REVERSE
        dropperServo.position = Depositor(this).dropperClosed

//        Collector
        collector = hwMap["collector"] as DcMotor

        collector.direction = DcMotorSimple.Direction.FORWARD
        collector.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

//        Ducc
        carouselSpinner = hwMap["duccSpinner"] as CRServo
        carouselSpinner.direction = DcMotorSimple.Direction.FORWARD
    }
}
