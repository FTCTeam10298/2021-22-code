package us.brainstormz.motion

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.Range
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.OdometryDriveMovement
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.pid.PID
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class MecanumMovement(override val localizer: Localizer, override val hardware: MecanumHardware): Movement, MecanumDriveTrain(hardware) {

    override var movementPID = PID(0.0001)
    override var precisionRange: ClosedRange<Double> = 1.0..1.0

    /**
     * Only required when using goToPosition
     */
    override lateinit var linearOpMode: LinearOpMode

    /**
     * Blocking function
     */
    override fun goToPosition(target: PositionAndRotation, powerRange: ClosedRange<Double>) {
        localizer.startNewMovement()
        while (linearOpMode.opModeIsActive()) {

            localizer.recalculatePositionAndRotation()
            val targetReached = moveTowardTarget(target, powerRange)

            if (targetReached)
                break
        }
    }

    override fun moveTowardTarget(target: PositionAndRotation, powerRange: ClosedRange<Double>): Boolean {
        val posError = target - localizer.currentPositionAndRotation()

//        adjust angle error
        while (posError.r > Math.PI)
            posError.r -= Math.PI * 2

        while (posError.r < -Math.PI)
            posError.r += Math.PI * 2

//        calculate distance error
        val distanceError = hypot(posError.x, posError.y)

        // Check to see if we've reached the desired position already

        // Check to see if we've reached the desired position already
        if (abs(distanceError) <= precisionRange.start &&
            abs(posError.r) <= Math.toRadians(precisionRange.start)) {
            return true
        }

        // Calculate the error in x and y and use the PID to find the error in angle
        val speedX: Double = movementPID.calcPID(
            sin(localizer.currentPositionAndRotation().r) * posError.y + cos(localizer.currentPositionAndRotation().r) * -posError.x
        )
        val speedY: Double = movementPID.calcPID(
            cos(localizer.currentPositionAndRotation().r) * posError.y + sin(localizer.currentPositionAndRotation().r) * posError.x
        )
        val speedA: Double = movementPID.calcPID(posError.r)

        setSpeedAll(speedX, speedY, speedA, powerRange.start, powerRange.endInclusive)

        return false
    }


    private fun setSpeedAll(vX: Double, vY: Double, vA: Double, minPower: Double, maxPower: Double) {

        // Calculate theoretical values for motor powers using transformation matrix
        var fl = vY - vX + vA
        var fr = vY + vX - vA
        var bl = vY + vX + vA
        var br = vY - vX - vA

        // Find the largest magnitude of power and the average magnitude of power to scale down to
        // maxPower and up to minPower
        var max = abs(fl)
        max = max.coerceAtLeast(abs(fr))
        max = max.coerceAtLeast(abs(bl))
        max = max.coerceAtLeast(abs(br))
        val ave = (abs(fl) + abs(fr) + abs(bl) + abs(br)) / 4
        if (max > maxPower) {
            fl *= maxPower / max
            bl *= maxPower / max
            br *= maxPower / max
            fr *= maxPower / max
        } else if (ave < minPower) {
            fl *= minPower / ave
            bl *= minPower / ave
            br *= minPower / ave
            fr *= minPower / ave
        }

        // Range clip just to be safe
        fl = Range.clip(fl, -1.0, 1.0)
        fr = Range.clip(fr, -1.0, 1.0)
        bl = Range.clip(bl, -1.0, 1.0)
        br = Range.clip(br, -1.0, 1.0)

        // Set powers
        driveSetPower(fl, fr, bl, br)
    }

}