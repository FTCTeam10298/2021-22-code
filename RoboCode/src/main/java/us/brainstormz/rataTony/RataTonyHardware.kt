package us.brainstormz.rataTony

import com.qualcomm.hardware.rev.RevTouchSensor
import com.qualcomm.robotcore.hardware.*
import org.outoftheboxrobotics.neutrinoi2c.MB1242.AsyncMB1242
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.rataTony.Depositor.DropperPos

class RataTonyHardware: MecanumHardware {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    lateinit var collector: DcMotor
    lateinit var collector2: DcMotor

    lateinit var carouselSpinner: CRServo

    lateinit var liftMotor: DcMotorEx
    lateinit var horiMotor: DcMotorEx
    lateinit var dropperServo: Servo
    lateinit var yLowerLimit: RevTouchSensor
    lateinit var xInnerLimit: RevTouchSensor

    lateinit var rangeSensor: AsyncMB1242

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

        horiMotor = hwMap["horiMotor"] as DcMotorEx
        horiMotor.direction = DcMotorSimple.Direction.FORWARD
        horiMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        horiMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        horiMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        dropperServo = hwMap["dropper"] as Servo
        dropperServo.direction = Servo.Direction.REVERSE
        dropperServo.position = DropperPos.Closed.posValue

        xInnerLimit = hwMap["innerLimit"] as RevTouchSensor
        yLowerLimit = hwMap["lowerLimit"] as RevTouchSensor

//        Collector
        collector = hwMap["collector"] as DcMotor
        collector.direction = DcMotorSimple.Direction.FORWARD
        collector.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        collector2 = hwMap["collector2"] as DcMotor
        collector2.direction = DcMotorSimple.Direction.FORWARD
        collector2.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

//        Ducc
        carouselSpinner = hwMap["duccSpinner"] as CRServo
        carouselSpinner.direction = DcMotorSimple.Direction.FORWARD

        rangeSensor = hwMap["ultrasonic"] as AsyncMB1242
        rangeSensor.enable()
    }
}
