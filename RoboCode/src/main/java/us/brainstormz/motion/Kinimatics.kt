package us.brainstormz.motion

import us.brainstormz.localizer.PositionAndRotation
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

/**
 * A collection of methods for various kinematics-related tasks.
 */
object Kinematics {
//
//    /**
//     * Returns the robot pose velocity corresponding to [fieldPose] and [fieldVel].
//     */
//    @JvmStatic
//    fun fieldToRobotVelocity(fieldPose: PositionAndRotation, fieldVel: PositionAndRotation) =
//        PositionAndRotation(fieldVel.rotated(-fieldPose.r), fieldVel.r)
//
//    /**
//     * Returns the robot pose acceleration corresponding to [fieldPose], [fieldVel], and [fieldAccel].
//     */
//    @JvmStatic
//    fun fieldToRobotAcceleration(fieldPose: PositionAndRotation, fieldVel: PositionAndRotation, fieldAccel: PositionAndRotation) =
//        PositionAndRotation(
//            fieldAccel.rotated(-fieldPose.r),
//            fieldAccel.r
//        ) +
//                PositionAndRotation(
//                    -fieldVel.x * sin(fieldPose.r) + fieldVel.y * cos(fieldPose.r),
//                    -fieldVel.x * cos(fieldPose.r) - fieldVel.y * sin(fieldPose.r),
//                    0.0
//                ) * fieldVel.r

    /**
     * Returns the error between [targetFieldPose] and [currentFieldPose] in the field frame.
     */
    @JvmStatic
    fun calculateFieldPoseError(targetFieldPose: PositionAndRotation, currentFieldPose: PositionAndRotation) =
        PositionAndRotation(
//            (targetFieldPose - currentFieldPose).vec(),
//            Angle.normDelta(targetFieldPose.r - currentFieldPose.r)
        )

    /**
     * Returns the error between [targetFieldPose] and [currentFieldPose] in the robot frame.
     */
    @JvmStatic
    fun calculateRobotPoseError(targetFieldPose: PositionAndRotation, currentFieldPose: PositionAndRotation): PositionAndRotation {
        val errorInFieldFrame = calculateFieldPoseError(targetFieldPose, currentFieldPose)
        return PositionAndRotation(
//            errorInFieldFrame.rotated(-currentFieldPose.r),
//            errorInFieldFrame.r
        )
    }

    /**
     * Computes the motor feedforward (i.e., open loop powers) for the given set of coefficients.
     */
    @JvmStatic
    fun calculateMotorFeedforward(
        vels: List<Double>,
        accels: List<Double>,
        kV: Double,
        kA: Double,
        kStatic: Double
    ) =
        vels.zip(accels)
            .map { (vel, accel) -> calculateMotorFeedforward(vel, accel, kV, kA, kStatic) }

    /**
     * Computes the motor feedforward (i.e., open loop power) for the given set of coefficients.
     */
    @JvmStatic
    fun calculateMotorFeedforward(vel: Double, accel: Double, kV: Double, kA: Double, kStatic: Double): Double {
        val basePower = vel * kV + accel * kA
        return if (basePower epsilonEquals 0.0) {
            0.0
        } else {
            basePower + sign(basePower) * kStatic
        }
    }


    /**
     * Performs a relative odometry update. Note: this assumes that the robot moves with constant velocity over the
     * measurement interval.
     */
    @JvmStatic
    fun relativeOdometryUpdate(fieldPose: PositionAndRotation, robotPoseDelta: PositionAndRotation): PositionAndRotation {
        val dtheta = robotPoseDelta.r
        val (sineTerm, cosTerm) = if (dtheta epsilonEquals 0.0) {
            1.0 - dtheta * dtheta / 6.0 to dtheta / 2.0
        } else {
            sin(dtheta) / dtheta to (1 - cos(dtheta)) / dtheta
        }

        val fieldPositionDelta = PositionAndRotation(
            sineTerm * robotPoseDelta.x - cosTerm * robotPoseDelta.y,
            cosTerm * robotPoseDelta.x + sineTerm * robotPoseDelta.y
        )

//        val fieldPoseDelta = PositionAndRotation(fieldPositionDelta.rotated(fieldPose.r), robotPoseDelta.r)

        return PositionAndRotation(
//            fieldPose.x + fieldPoseDelta.x,
//            fieldPose.y + fieldPoseDelta.y,
//            Angle.norm(fieldPose.r + fieldPoseDelta.r)
        )
    }

/**Math*/

    const val EPSILON = 1e-6

    infix fun Double.epsilonEquals(other: Double) = abs(this - other) < EPSILON
}