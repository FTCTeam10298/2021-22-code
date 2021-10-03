package us.brainstormz.brian

import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple

/**
 * This is NOT an opmode.
 * This class is used to define all the specific hardware for a single robot.
 */
class BrianHardware {
    /* Public OpMode members. */
    lateinit var frontLeftDrive: DcMotor
    lateinit var backLeftDrive: DcMotor
    lateinit var frontRightDrive: DcMotor
    lateinit var backRightDrive: DcMotor
    lateinit var extendoArm5000initial: DcMotor
    lateinit var extendoArm5000: DcMotorEx
    lateinit var pivotArm1initial: DcMotor
    lateinit var pivotArm2initial: DcMotor
    lateinit var pivotArm1: DcMotorEx
    lateinit var pivotArm2: DcMotorEx
    lateinit var collectOtron: DcMotor
    lateinit var collectorGate: Servo
    lateinit var markerDumper: CRServo
    lateinit var potentiometer: AnalogInput

    /* Local OpMode members. */
    lateinit var hwMap: HardwareMap

    /* Initialize standard Hardware interfaces */
    fun init(ahwMap: HardwareMap?) {
        // Save reference to Hardware map
        hwMap = ahwMap!!

        // Define and initialize motors
        frontLeftDrive = hwMap["front_left_drive"] as DcMotor
        backLeftDrive = hwMap["back_left_drive"] as DcMotor
        frontRightDrive = hwMap["front_right_drive"] as DcMotor
        backRightDrive = hwMap["back_right_drive"] as DcMotor
        extendoArm5000initial = hwMap["extendoArm_5000"] as DcMotor
        pivotArm1initial = hwMap["pivotArm1"] as DcMotor
        pivotArm2initial = hwMap["pivotArm2"] as DcMotor
        collectOtron = hwMap["collectOtron"] as DcMotor
        extendoArm5000 = extendoArm5000initial as DcMotorEx
        pivotArm1 = pivotArm1initial as DcMotorEx
        pivotArm2 = pivotArm2initial as DcMotorEx

        // Set direction for all motors
        frontLeftDrive.direction = DcMotorSimple.Direction.REVERSE
        backLeftDrive.direction = DcMotorSimple.Direction.REVERSE
        frontRightDrive.direction = DcMotorSimple.Direction.FORWARD
        backRightDrive.direction = DcMotorSimple.Direction.FORWARD
        extendoArm5000.direction = DcMotorSimple.Direction.REVERSE
        pivotArm1.direction = DcMotorSimple.Direction.REVERSE
        pivotArm2.direction = DcMotorSimple.Direction.FORWARD
        collectOtron.direction = DcMotorSimple.Direction.REVERSE


        // Set all motors to zero power
        frontLeftDrive.power = 0.0
        backLeftDrive.power = 0.0
        frontRightDrive.power = 0.0
        backRightDrive.power = 0.0
        extendoArm5000.power = 0.0
        pivotArm1.power = 0.0
        pivotArm2.power = 0.0
        collectOtron.power = 0.0


        // Set all motors to use brake mode
        frontRightDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        frontLeftDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        backRightDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        backLeftDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        extendoArm5000.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        pivotArm1.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        pivotArm2.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        collectOtron.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE


        // Set almost all motors to run with encoders
        frontLeftDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
        backLeftDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
        frontRightDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
        backRightDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
        extendoArm5000.mode = DcMotor.RunMode.RUN_USING_ENCODER
        pivotArm1.mode = DcMotor.RunMode.RUN_USING_ENCODER
        pivotArm2.mode = DcMotor.RunMode.RUN_USING_ENCODER

        // This has no encoder
        collectOtron.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER


        // Define and initialize all installed servos
        collectorGate = hwMap.servo["extension_lock"]
        markerDumper = hwMap.crservo["marker_dumper"]
        collectorGate.position = .68
        markerDumper.power = 0.0

        // Initialize arm position sensor
        potentiometer = hwMap.get(AnalogInput::class.java, "potentiometer")
    }

    /**
     * DrivePowerAll sets all of the drive train motors to the specified power level.
     * @param power Power level to set all motors to
     */
    fun DrivePowerAll(power: Double) {
        driveSetPower(power, power, power, power)
    }

    /**
     * driveSetPower sets all of the drive train motors to the specified power levels.
     * @param flPower Power level to set front left motor to
     * @param frPower Power level to set front right motor to
     * @param blPower Power level to set back left motor to
     * @param brPower Power level to set back right motor to
     */
    fun driveSetPower(flPower: Double, frPower: Double, blPower: Double, brPower: Double) {
        frontLeftDrive.power = flPower
        frontRightDrive.power = frPower
        backLeftDrive.power = blPower
        backRightDrive.power = brPower
    }

    /**
     * driveSetMode sets all of the drive train motors to the specified mode.
     * @param runmode RunMode to set motors to
     */
    fun driveSetMode(runmode: DcMotor.RunMode?) {
        frontLeftDrive.mode = runmode
        frontRightDrive.mode = runmode
        backLeftDrive.mode = runmode
        backRightDrive.mode = runmode
    }

    fun driveSetRunToPosition() {
        if (frontLeftDrive.mode != DcMotor.RunMode.RUN_TO_POSITION || frontRightDrive.mode != DcMotor.RunMode.RUN_TO_POSITION || backLeftDrive.mode != DcMotor.RunMode.RUN_TO_POSITION || backRightDrive.mode != DcMotor.RunMode.RUN_TO_POSITION) {
            driveSetMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)
            driveSetMode(DcMotor.RunMode.RUN_TO_POSITION)
            // When the encoder is reset, also reset the target position, so it doesn't add an old
            // target position when using driveAddTargetPosition().
            driveSetTargetPosition(0, 0, 0, 0)
        }
    }

    /**
     * driveSetMode sets all of the drive train motors to the specified positions.
     * @param flPosition Position to set front left motor to run to
     * @param frPosition Position to set front right motor to run to
     * @param blPosition Position to set back left motor to run to
     * @param brPosition Position to set back right motor to run to
     */
    fun driveSetTargetPosition(flPosition: Int, frPosition: Int, blPosition: Int, brPosition: Int) {
        frontLeftDrive.targetPosition = flPosition
        frontRightDrive.targetPosition = frPosition
        backLeftDrive.targetPosition = blPosition
        backRightDrive.targetPosition = brPosition
    }

    fun driveAddTargetPosition(flPosition: Int, frPosition: Int, blPosition: Int, brPosition: Int) {
        frontLeftDrive.targetPosition = frontLeftDrive.targetPosition + flPosition
        frontRightDrive.targetPosition = frontRightDrive.targetPosition + frPosition
        backLeftDrive.targetPosition = backLeftDrive.targetPosition + blPosition
        backRightDrive.targetPosition = backRightDrive.targetPosition + brPosition
    }

    fun driveAllAreBusy(): Boolean {
        return (frontLeftDrive.isBusy && frontRightDrive.isBusy && backLeftDrive.isBusy
                && backRightDrive.isBusy)
    }

    fun pivotArmGetPosition(): Double {
        // Sensor returns voltage from 0 to 3.34, normalize to a range of 0-1, then multiply by potentiometer range, then error
        return potentiometer.voltage / 3.34 * 270 * 1.578947368
    }

    companion object {
        // Constant that converts the pivot arm position to degrees (1120*10/360)
        const val PIVOTARM_CONSTANT = 280.0 / 9.0
    }
}