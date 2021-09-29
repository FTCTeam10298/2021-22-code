package locationTracking

import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Creates a PositionAndRotation with given values. All alternate constructors assume 0 for unstated variables.
 * @param x X position
 * @param y Y position
 * @param r Angle, in radians
 */
open class PositionAndRotation(var x: Double = 0.0, var y: Double = 0.0, var r: Double = 0.0) {

    /**
     * Sets the parameters of the PositionAndRotation.
     * @param x The x value that we want to set.
     * @param y The y value that we want to set.
     * @param r The angle that we want to set in degrees
     */
    fun setPositionAndRotation(x: Double? = null, y: Double? = null, r: Double? = null) {
        if (x != null)
            this.x = x

        if (y != null)
            this.y = y

        if (r != null) {
//            var rAdjusted: Double = r
//
//            while (abs(rAdjusted) > 180)
//                rAdjusted += (Math.PI/* * 2*/)

//            this.r = rAdjusted
            this.r = r
        }
    }

    fun addPositionAndRotation(x: Double = 0.0, y: Double = 0.0, r: Double = 0.0) {
        setPositionAndRotation(this.x + x, this.y + y, this.r + r)
    }

    /**
     * Wraps the angle around so that the robot doesn't unnecessarily turn over 180 degrees.
     * @param angle The angle to wrap.
     * @return The wrapped angle.
     */
    fun wrapAngle(angle: Double): Double {
        return angle % (2 * Math.PI)
    }

    /**
     * Gives the absolute value of the distance between the given PositionAndRotation and the current PositionAndRotation.
     * @param PositionAndRotation PositionAndRotation to compare
     * @return distance from current PositionAndRotation
     */
    fun distance(PositionAndRotation: PositionAndRotation): Double {
        return hypot(PositionAndRotation.x - x, PositionAndRotation.y - y)
    }

    /**
     * Gives the absolute value of the distance between the X and Y values and the current PositionAndRotation.
     * @param targetX X
     * @param targetY Y
     * @return distance from current PositionAndRotation
     */
    fun distance(targetX: Double, targetY: Double): Double {
        return hypot(targetX - x, targetY - y)
    }

    /**
     * Gives the error of the angle from the given angle and the current PositionAndRotation.
     * @param targetA angle to compare
     * @return angle error from current PositionAndRotation
     */
    fun theta(targetA: Double): Double {
        return wrapAngle(targetA - r)
    }

    /**
     * Gives the error of the angle from the given PositionAndRotation and the current PositionAndRotation.
     * @param PositionAndRotation PositionAndRotation to compare
     * @return angle error from current PositionAndRotation
     */
    fun theta(PositionAndRotation: PositionAndRotation): Double {
        return theta(PositionAndRotation.r)
    }

    fun direction(PositionAndRotation: PositionAndRotation): Double {
        return Math.atan2(PositionAndRotation.y-this.y, PositionAndRotation.x-this.x)
    }

    fun coordinateAlongLine(distance: Double, p2: PositionAndRotation): PositionAndRotation {

        val d = this.distance(p2)

        return PositionAndRotation(
            this.x + ((distance / d) * (p2.x - this.x)),
            this.y + ((distance / d) * (p2.y - this.y))
        )
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

//    override fun toString(): String {
//        return "X: $x\nY: $y\nAngle: $r"
//    }


    override fun toString(): String {
        return "(x: $x, y: $y, angle: $r)"
    }

    override fun hashCode(): Int = x.hashCode() + y.hashCode() + r.hashCode()

    fun copy(x: Double, y: Double, r: Double): PositionAndRotation {
        val thisPlaceHolder = this
        thisPlaceHolder.addPositionAndRotation(x, y, r)
        return thisPlaceHolder
    }

    operator fun plus(n: Double): PositionAndRotation {
        return PositionAndRotation(this.x + n, this.y + n, this.r + n)
    }

    operator fun plus(n: PositionAndRotation): PositionAndRotation {
        return PositionAndRotation(this.x + n.x, this.y + n.y, this.r + n.r)
    }
    operator fun times(n: Double): PositionAndRotation {
        return PositionAndRotation(this.x * n, this.y * n, this.r * n)
    }

    operator fun compareTo(n: PositionAndRotation): Int {
        val avgN = n.x + n.y + n.r / 3
        val avgThis = this.x + this.y + this.r / 3

        val difference = avgThis - avgN

        return when{
            difference == 0.0 -> 0
            difference > 0 -> 1
            else -> -1
        }
    }

    operator fun div(n: Int): PositionAndRotation {
        return PositionAndRotation(x / n, y / n, r / n)
    }
}
