package us.brainstormz.motion

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.rataTony.MiniblottoHardware
import us.brainstormz.rataTony.RataTonyHardware

@Autonomous
class test: OpMode() {
    val hardware = RataTonyHardware()
    val driveTrain = MecanumDriveTrain(hardware)
    val encoderDrive = EfficientEncoderDrive(hardware, driveTrain)

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun loop() {
        encoderDrive.changePosition(10.0, 0.0, 90.0, 1.0)
    }

}

class EfficientEncoderDrive(private val hardware: MecanumHardware, private val driveTrain: MecanumDriveTrain): MecanumDrive(1.0, 1.0, 1.0, 1.0) {
    override fun setMotorPowers(
        frontLeft: Double,
        rearLeft: Double,
        rearRight: Double,
        frontRight: Double
    ) {
        driveTrain.driveSetPower(frontLeft, frontRight, rearLeft, rearRight)
    }

    override fun getWheelPositions(): List<Double> = listOf(
        hardware.lFDrive.currentPosition.toDouble(),
        hardware.lBDrive.currentPosition.toDouble(),
        hardware.rFDrive.currentPosition.toDouble(),
        hardware.rBDrive.currentPosition.toDouble()
    )

    fun changePosition(x: Double, y: Double, r: Double, power: Double) {
        val targetPos = PositionAndRotation(x, y, r)
        val speed = PositionAndRotation(power, power, power)
        setDriveSignal(targetPos, speed)
        while (localizer.currentPositionAndRotation() >= targetPos) {
            localizer.recalculatePositionAndRotation()
        }
        setDriveSignal(targetPos, PositionAndRotation(0.0, 0.0, 0.0))
    }

}


/**
 * This class provides the basic functionality of a mecanum drive using [MecanumKinematics].
 *
 * @param kV velocity feedforward
 * @param kA acceleration feedforward
 * @param kStatic additive constant feedforward
 * @param trackWidth lateral distance between pairs of wheels on different sides of the robot
 * @param wheelBase distance between pairs of wheels on the same side of the robot
 * @param lateralMultiplier lateral multiplier
 */
abstract class MecanumDrive @JvmOverloads constructor(
    private val kV: Double,
    private val kA: Double,
    private val kStatic: Double,
    val trackWidth: Double,
    val wheelBase: Double = trackWidth,
    val lateralMultiplier: Double = 1.0
) {

    var localizer: Localizer = MecanumLocalizer(this)

    fun setDriveSignal(vel: PositionAndRotation = PositionAndRotation(), accel: PositionAndRotation = PositionAndRotation()) {
        val velocities = MecanumKinematics.robotToWheelVelocities(
            vel,
            trackWidth,
            wheelBase,
            lateralMultiplier
        )
        val accelerations = MecanumKinematics.robotToWheelAccelerations(
            accel,
            trackWidth,
            wheelBase,
            lateralMultiplier
        )
        val powers = MecanumKinematics.calculateMotorFeedforward(velocities, accelerations, kV, kA, kStatic)
        setMotorPowers(powers[0], powers[1], powers[2], powers[3])
    }

    fun setDrivePower(drivePower: PositionAndRotation) {
        val powers = MecanumKinematics.robotToWheelVelocities(
            drivePower,
            1.0,
            1.0,
            lateralMultiplier
        )
        setMotorPowers(powers[0], powers[1], powers[2], powers[3])
    }

    /**
     * Sets the following motor powers (normalized voltages). All arguments are on the interval `[-1.0, 1.0]`.
     */
    abstract fun setMotorPowers(frontLeft: Double, rearLeft: Double, rearRight: Double, frontRight: Double)

    /**
     * Returns the positions of the wheels in linear distance units. Positions should exactly match the ordering in
     * [setMotorPowers].
     */
    abstract fun getWheelPositions(): List<Double>

    /**
     * Returns the velocities of the wheels in linear distance units. Positions should exactly match the ordering in
     * [setMotorPowers].
     */
    open fun getWheelVelocities(): List<Double>? = null
}

/**
 * Default localizer for mecanum drives based on the drive encoders.
 *
 * @param drive drive
 */
class MecanumLocalizer constructor(
    private val drive: MecanumDrive
) : Localizer {
    private var _poseEstimate = PositionAndRotation()
    var poseEstimate: PositionAndRotation
        get() = _poseEstimate
        set(value) {
            lastWheelPositions = emptyList()
            lastExtHeading = Double.NaN
        }
    var poseVelocity: PositionAndRotation? = null
        private set
    private var lastWheelPositions = emptyList<Double>()
    private var lastExtHeading = Double.NaN

    override fun recalculatePositionAndRotation() {
        val wheelPositions = drive.getWheelPositions()
        if (lastWheelPositions.isNotEmpty()) {
            val wheelDeltas = wheelPositions
                .zip(lastWheelPositions)
                .map { it.first - it.second }
            val robotPoseDelta = MecanumKinematics.wheelToRobotVelocities(
                wheelDeltas,
                drive.trackWidth,
                drive.wheelBase,
                drive.lateralMultiplier
            )
            val finalHeadingDelta = robotPoseDelta.r

            _poseEstimate = Kinematics.relativeOdometryUpdate(
                _poseEstimate,
                PositionAndRotation(robotPoseDelta.x, robotPoseDelta.y, finalHeadingDelta)
            )
        }

        val wheelVelocities = drive.getWheelVelocities()
        if (wheelVelocities != null) {
            poseVelocity = MecanumKinematics.wheelToRobotVelocities(
                wheelVelocities,
                drive.trackWidth,
                drive.wheelBase,
                drive.lateralMultiplier
            )
        }

        lastWheelPositions = wheelPositions
    }

    override fun currentPositionAndRotation(): PositionAndRotation = poseEstimate

    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?) {}
}