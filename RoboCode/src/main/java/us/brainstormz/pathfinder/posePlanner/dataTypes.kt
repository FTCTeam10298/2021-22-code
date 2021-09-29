package posePlanner

import locationTracking.PositionAndRotation
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

//abstract class Plot(val x: Double, val y: Double, val width: Double, val height: Double)

class Poly(p1: PositionAndRotation, p2: PositionAndRotation, p3:PositionAndRotation, vararg  points: PositionAndRotation) {
    val points = listOf(p1, p2, p3, *points)

    fun getLines(): List<Line> {
        return points.map {
            val next = if (it !== points.last())
                points[points.indexOf(it) + 1]
            else
                points.first()

            Line(it, next)
        }
    }

    fun centroid(): PositionAndRotation {

        val result = points.fold(PositionAndRotation()){ acc, it ->
            acc + it
        }
        result.x /= points.size
        result.y /= points.size

        return result
    }

    fun wrapAngle(n: Double): Double =
        if (n < 0.0)
            n + 2*PI
        else
            n


    fun intersection(l:Line): Pair<PositionAndRotation, Line>? {
        var result: Pair<PositionAndRotation, Line>? = null

//        println("\nnew obs")
        getLines().forEach {
            val thisIntersect = it.lineIntersection(l)

//            println("line $line")
            if (thisIntersect != null){
//                println("intersect")
                if (result == null || thisIntersect < result!!.first) {
                    result = thisIntersect to it
                }
            }
        }

        return result
    }


    fun isPointInBounds(point: PositionAndRotation): Boolean {

        val nextList = (points+points.first()).drop(1)
        val prevList = (listOf(points.last())+points).dropLast(1)
        return prevList.zip(points.zip(nextList)).fold(true) { acc, it ->
            print("\n")

            val prev = it.first; val current = it.second.first; val next = it.second.second
            println("next $next")
            println("current $current")
            println("prev $prev")

            val direc1 = wrapAngle(current.direction(next))
            val direcPoint = (wrapAngle(current.direction(point)))
            val direc2 = wrapAngle(current.direction(prev))
            println("direc1 $direc1")
            println("direcPoint $direcPoint")
            println("direc2 $direc2")

//            println("asdffads ${wrapAngle(atan2(-2.0, 0.0))}")
            if (direcPoint !in (direc1..direc2))
                false
            else
                acc
        }
    }

    override fun toString(): String =
        "Poly: ${points.joinToString(",")}"

}

interface FieldElement{
    val poly:Poly
}
class Obstruction(override val poly:Poly):FieldElement {
    constructor() : this(Poly(PositionAndRotation(), PositionAndRotation(), PositionAndRotation()))

    fun codeString(): String {
        val interior: String = poly.points.map {
            "PositionAndRotation$it"
        }.toString().drop(1).dropLast(1).replace(" angle:", "").replace(" y:", "").replace("x: ", "")

        return "Obstruction(Poly(${interior})),\n"
    }
}
class UndesirableArea(override val poly:Poly, val cost: Double):FieldElement

open class Point3D(val x: Double, val y: Double, val z: Double) {

    constructor() : this(0.0, 0.0, 0.0)

    operator fun plus(point: Point3D): Point3D {
        return Point3D(point.x + this.x, point.y + this.y, point.z + this.z)
    }

    operator fun minus(point: Point3D): Point3D {
        return Point3D(point.x - this.x, point.y - this.y, point.z - this.z)
    }

    fun pow(i: Int): Point3D {
        return Point3D(this.x.pow(i), this.y.pow(i), this.z.pow(i))
    }

    operator fun times(d: Double): Point3D {
        val x = this.x * d
        val y = this.y * d
        val z = this.z * d
        return Point3D(x, y, z)
    }

    fun copy(x: Double, y: Double, z: Double): Point3D = Point3D(this.x + x, this.y + y, this.z + z)

    fun toPoint2D(): Point2D = Point2D(this.x, this.y)

    fun magnitude(): Double {
        return this.x.pow(2) + y.pow(2) + z.pow(2)
    }
    fun normalized(): Point3D {
        val m = this.magnitude()
        return if (m > 0) {
            this / m
        } else
            Point3D()
    }

    operator fun div(i: Double): Point3D {
        return Point3D(this.x / i, this.y / i, this.z / i)
    }

    override fun toString(): String {
        return "($x, $y, $z)"
    }

    fun distanceTo(p1: PositionAndRotation): Double =
        sqrt((p1.x - x).pow(2.0) +
                (p1.y - y).pow(2.0) * 1.0)

    fun equals(other: Point3D): Boolean {
        return this.x == other.x && this.y == other.y && this.z == other.z
    }
}

class Point2D(x: Double, y: Double): Point3D(x, y, 0.0) {
    fun toPoint3D(): Point3D = Point3D(this.x, this.y, this.z)
}

class Line(val start: PositionAndRotation, val end: PositionAndRotation) {

    fun lineIntersection(line: Line): PositionAndRotation? {

        val p1 = this.start
        val p2 = this.end
        val p3 = line.start
        val p4 = line.end

        val x1 = p1.x
        val y1 = p1.y
        val x2 = p2.x
        val y2 = p2.y
        val x3 = p3.x
        val y3 = p3.y
        val x4 = p4.x
        val y4 = p4.y


        val tNumerator =   (x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)
        val tDenominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
        val t = tNumerator / tDenominator

        val uNumerator =   (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)
        val uDenominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)
        val u = uNumerator / uDenominator


        val lineIntersect = when {
            t in 0.0..1.0 -> {
                val tx = x1 + t*(x2 - x1)
                val ty = y1 + t*(y2 - y1)
                val tIntersect = PositionAndRotation(tx, ty)
                tIntersect
            }
            u in 0.0..1.0 -> {
                val ux = x3 + u * (x4 - x3)
                val uy = y3 + u * (y4 - y3)
                val uIntersect = PositionAndRotation(ux, uy)
                uIntersect
            }
            else -> null
        }

        return if (doSegmentsIntersect(p1, p2, p3, p4))
            lineIntersect
        else
            null
    }

    private fun doSegmentsIntersect(p1: PositionAndRotation, q1: PositionAndRotation, p2: PositionAndRotation, q2: PositionAndRotation): Boolean {
        val o1 = orientation(p1, q1, p2)
        val o2 = orientation(p1, q1, q2)
        val o3 = orientation(p2, q2, p1)
        val o4 = orientation(p2, q2, q1)

        return when {
            // General case
            (o1 != o2 && o3 != o4) -> true

            // Special cases
            (o1 == 0 && onSegment(p1, p2, q1)) -> true  // p1, q1 and p2 are colinear and p2 lies on segment p1q1
            (o2 == 0 && onSegment(p1, q2, q1)) -> true  // p1, q1 and q2 are colinear and q2 lies on segment p1q
            (o3 == 0 && onSegment(p2, p1, q2)) -> true  // p2, q2 and p1 are colinear and p1 lies on segment p2q2
            (o4 == 0 && onSegment(p2, q1, q2)) -> true  // p2, q2 and q1 are colinear and q1 lies on segment p2q2
            else -> false
        }
    }

    private fun orientation(p: PositionAndRotation, q: PositionAndRotation, r: PositionAndRotation): Int {
        val value: Double = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)

        return when {
            value == 0.0 -> {
                0  //colinear
            }
            value > 0.0 -> {
                1   //clockwise
            }
            else -> {
                2   //counterclock wise
            }
        }
    }

    private fun onSegment(p: PositionAndRotation, q: PositionAndRotation, r: PositionAndRotation): Boolean = q.x <= p.x.coerceAtLeast(r.x) && q.x >= p.x.coerceAtMost(r.x) && q.y <= p.y.coerceAtLeast(r.y) && q.y >= p.y.coerceAtMost(r.y)

    override fun toString(): String =
        "($start to $end)"

}