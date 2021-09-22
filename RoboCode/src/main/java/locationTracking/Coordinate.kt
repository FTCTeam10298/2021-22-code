package locationTracking

import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Creates a Coordinate with given values. All alternate constructors assume 0 for unstated variables.
 * @param x X position
 * @param y Y position
 * @param r Angle, in radians
 */
open class Coordinate(var x: Double = 0.0, var y: Double = 0.0, var r: Double = 0.0) {

    /**
     * Sets the parameters of the Coordinate.
     * @param x The x value that we want to set.
     * @param y The y value that we want to set.
     * @param r The angle that we want to set in degrees
     */
    fun setCoordinate(x: Double? = null, y: Double? = null, r: Double? = null) {
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

    fun addCoordinate(x: Double = 0.0, y: Double = 0.0, r: Double = 0.0) {
        setCoordinate(this.x + x, this.y + y, this.r + r)
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
     * Gives the absolute value of the distance between the given Coordinate and the current Coordinate.
     * @param coordinate Coordinate to compare
     * @return distance from current Coordinate
     */
    fun distance(coordinate: Coordinate): Double {
        return hypot(coordinate.x - x, coordinate.y - y)
    }

    /**
     * Gives the absolute value of the distance between the X and Y values and the current Coordinate.
     * @param targetX X
     * @param targetY Y
     * @return distance from current Coordinate
     */
    fun distance(targetX: Double, targetY: Double): Double {
        return hypot(targetX - x, targetY - y)
    }

    /**
     * Gives the error of the angle from the given angle and the current Coordinate.
     * @param targetA angle to compare
     * @return angle error from current Coordinate
     */
    fun theta(targetA: Double): Double {
        return wrapAngle(targetA - r)
    }

    /**
     * Gives the error of the angle from the given Coordinate and the current Coordinate.
     * @param coordinate Coordinate to compare
     * @return angle error from current Coordinate
     */
    fun theta(coordinate: Coordinate): Double {
        return theta(coordinate.r)
    }

    fun direction(coordinate: Coordinate): Double {
        return Math.atan2(coordinate.y-this.y, coordinate.x-this.x)
    }

    fun coordinateAlongLine(distance: Double, p2: Coordinate): Coordinate {

        val d = this.distance(p2)

        return Coordinate(
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

    fun copy(x: Double, y: Double, r: Double): Coordinate {
        val thisPlaceHolder = this
        thisPlaceHolder.addCoordinate(x, y, r)
        return thisPlaceHolder
    }

    operator fun plus(n: Double): Coordinate {
        return Coordinate(this.x + n, this.y + n, this.r + n)
    }

    operator fun plus(n: Coordinate): Coordinate {
        return Coordinate(this.x + n.x, this.y + n.y, this.r + n.r)
    }
    operator fun times(n: Double): Coordinate {
        return Coordinate(this.x * n, this.y * n, this.r * n)
    }

    operator fun compareTo(n: Coordinate): Int {
        val avgN = n.x + n.y + n.r / 3
        val avgThis = this.x + this.y + this.r / 3

        val difference = avgThis - avgN

        return when{
            difference == 0.0 -> 0
            difference > 0 -> 1
            else -> -1
        }
    }

    operator fun div(n: Int): Coordinate {
        return Coordinate(x / n, y / n, r / n)
    }
}
