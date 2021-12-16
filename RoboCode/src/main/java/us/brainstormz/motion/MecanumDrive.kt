package us.brainstormz.motion

import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
//
///**
// * This class provides the basic functionality of a mecanum drive using [MecanumKinematics].
// *
// * @param kV velocity feedforward
// * @param kA acceleration feedforward
// * @param kStatic additive constant feedforward
// * @param trackWidth lateral distance between pairs of wheels on different sides of the robot
// * @param wheelBase distance between pairs of wheels on the same side of the robot
// * @param lateralMultiplier lateral multiplier
// */
//abstract class MecanumDrive @JvmOverloads constructor(
//    private val kV: Double,
//    private val kA: Double,
//    private val kStatic: Double,
//    val trackWidth: Double,
//    val wheelBase: Double = trackWidth,
//    val lateralMultiplier: Double = 1.0
//) {
//
//    var localizer: Localizer = MecanumLocalizer(this)
//
//    fun setDriveSignal(vel: PositionAndRotation = PositionAndRotation(), accel: PositionAndRotation = PositionAndRotation()) {
//        val velocities = MecanumKinematics.robotToWheelVelocities(
//            vel,
//            trackWidth,
//            wheelBase,
//            lateralMultiplier
//        )
//        val accelerations = MecanumKinematics.robotToWheelAccelerations(
//            accel,
//            trackWidth,
//            wheelBase,
//            lateralMultiplier
//        )
//        val powers = MecanumKinematics.calculateMotorFeedforward(velocities, accelerations, kV, kA, kStatic)
//        setMotorPowers(powers[0], powers[1], powers[2], powers[3])
//    }
//
//    fun setDrivePower(drivePower: PositionAndRotation) {
//        val powers = MecanumKinematics.robotToWheelVelocities(
//            drivePower,
//            1.0,
//            1.0,
//            lateralMultiplier
//        )
//        setMotorPowers(powers[0], powers[1], powers[2], powers[3])
//    }
//
//    /**
//     * Sets the following motor powers (normalized voltages). All arguments are on the interval `[-1.0, 1.0]`.
//     */
//    abstract fun setMotorPowers(frontLeft: Double, rearLeft: Double, rearRight: Double, frontRight: Double)
//
//    /**
//     * Returns the positions of the wheels in linear distance units. Positions should exactly match the ordering in
//     * [setMotorPowers].
//     */
//    abstract fun getWheelPositions(): List<Double>
//
//    /**
//     * Returns the velocities of the wheels in linear distance units. Positions should exactly match the ordering in
//     * [setMotorPowers].
//     */
//    open fun getWheelVelocities(): List<Double>? = null
//}
//
