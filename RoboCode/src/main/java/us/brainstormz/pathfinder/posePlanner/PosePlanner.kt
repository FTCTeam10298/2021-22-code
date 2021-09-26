package us.brainstormz.pathfinder.posePlanner

import us.brainstormz.pathfinder.Coordinate


class PosePlanner {
    var aStar = AStar(Coordinate(), Coordinate(), listOf(), this)
    var hitPoints = listOf<Coordinate>()

    val preLoadedPaths = listOf(BezierPath())
    val undesirableAreas: List<UndesirableArea> = listOf()
    var obstructions: List<Obstruction> = listOf()


    /**
    PATH GEN
     */
    fun generatePath(start: Coordinate, target: Coordinate): List<Coordinate> {
        println("\n\n\nNew Path=\n")

        val aStar = AStar(start, target, obstructions, this)

        this.aStar = aStar
        hitPoints = listOf()

        var currentNode = AStarPoint(start)

        while (currentNode.point != target){
            /**Choose*/
            currentNode = aStar.cheapestNode()
            println("\nNew Node: ${currentNode.point}\n")

            val collision = firstIntersection(Line(currentNode.point, target), obstructions) /*?: target to Obstruction()*/
            println("Collision ${collision?.first}\n")

            if (collision == null) {
                currentNode = AStarPoint(target, 0.0, 0.0, currentNode, 0.0)
                break
            }

            val adjustedCollision =
                collision.first.coordinateAlongLine(.2, start)
            val adjustedAStar = AStarPoint(adjustedCollision, currentNode.gCost + adjustedCollision.distance(currentNode.point), adjustedCollision.distance(target), currentNode, 0.0)

            hitPoints += adjustedCollision

            /**Find Neighbors*/
            val neighbors = if (collision.first != target)
                findAround(adjustedCollision, collision.second).filter{
                    firstIntersection(Line(adjustedCollision, it), obstructions) == null
                }
            else
                listOf(target)
            aStar.addNeighbors(neighbors.map { AStarPoint(it, 0.0, 0.0, adjustedAStar, 0.0) })

            val trueNeighbors = aStar.openSet.filter {
                firstIntersection(Line(adjustedCollision, it.point), obstructions) == null
            }

            /**Expand*/
            aStar.expand(adjustedAStar, trueNeighbors)
        }

        val rtn = aStar.tracePath(currentNode)
        return cutCorners(cutCorners(rtn))
    }


    private fun findAround(current: Coordinate, obstruction: Obstruction): List<Coordinate> {
        val edges = obstruction.poly.getLines()

        val centroid = obstruction.poly.centroid()
        var collisionEdge = edges.first()
        edges.forEach {
            val intersect = it.lineIntersection(Line(current, centroid))
            if (intersect != null && ((collisionEdge.start + collisionEdge.end) / 2).distance(current) > intersect.distance(current))
            collisionEdge = it
        }

        val farthestPoint = obstruction.poly.points.minByOrNull { it.distance(current) }!!
        val movedPoints = listOf(collisionEdge.start, collisionEdge.end).map {
            it.coordinateAlongLine(-.2, centroid).coordinateAlongLine(-.2, farthestPoint)
        }

        return movedPoints
    }


    private fun cutCorners(path: List<Coordinate>): List<Coordinate> {
        println("\n")
        println(path)

        val efficientPath: MutableList<Coordinate> = path.toMutableList()

        var current = path.first()

        while (efficientPath.indexOf(current) + 2 < efficientPath.size) {
            val next = efficientPath[efficientPath.indexOf(current) + 1]
            val nextNext = efficientPath[efficientPath.indexOf(current) + 2]

            println(efficientPath.size)

            val throughAllDistance = current.distance(next) + next.distance(nextNext)
            val directDistance = current.distance(nextNext)

            current = if ((directDistance <= throughAllDistance) && firstIntersection(Line(current, nextNext), obstructions) == null) {

                println(efficientPath.remove(next))
                println(next)
                current
            } else {
                next
            }
        }

        return efficientPath
    }

    fun firstIntersection(l: Line, obstructions: List<Obstruction>): Pair<Coordinate, Obstruction>? {
        var result: Coordinate? = null
        var intersect: Obstruction? = null
        obstructions.forEach {
            val newIntersect = it.poly.intersection(l)
            if (newIntersect != null)
                if (result == null || newIntersect.first.distance(l.start) < result!!.distance(l.start)){
                    result = newIntersect.first
                    intersect = it
                }
        }

        return if (intersect != null)
            result!! to intersect!!
        else
            null
    }


    /**
    PATH FOLLOWER
     */

//    broke needs work
//    ill fix it later

//
//    private var currentPath: List<BezierCurve>? = null
//    private var currentCurve: BezierCurve? = null
//    private var d = 0.0
//    private val granularity = 0.1
//
//    fun getCoordinate(current: Coordinate, target: Coordinate): Coordinate {
//
////        determine path
//        if (currentPath == null)
//            currentPath = isPathPreLoaded(current.toPoint3D(), target.toPoint3D())
//                        ?: generatePath(current, target)
//
////        determine current curve
//        if (d == 1.0) {
//            val currentPathcurves = currentPath!!.curves
//            currentCurve = currentPathcurves.firstOrNull {
//                it == currentCurve
//            } ?: currentPathcurves.iterator().next()
//            d = 0.0
//        }
//
////        determine d
//        d = calculateD()
//
////        return
//        val nextPoint = currentCurve!!.calculatePoint(d)
//        return nextPoint.toCoordinate()
//    }
//
//    private fun isPathPreLoaded(start: Point3D, end: Point3D): BezierPath? =
//        preLoadedPaths.firstOrNull {
//            it.curves.last().calculatePoint(1.0) == end
//        }
//
//    private fun calculateD(): Double {
//        return d + granularity
//    }

    val pointToCoordinate = Point3D(0.0, 0.0, 0.0)
    private fun Point3D.toCoordinate(): Coordinate {
        val adjusted = this.copy(x = pointToCoordinate.x,
                                 y = pointToCoordinate.y,
                                 z = pointToCoordinate.z)
        return Coordinate(x = adjusted.x,
                          y = adjusted.y,
                          r = adjusted.z)
    }
    private fun Coordinate.toPoint3D(): Point3D {
        val adjusted = this.copy(x = pointToCoordinate.x,
                                 y = pointToCoordinate.y,
                                 r = pointToCoordinate.z)
        return Point3D(x = adjusted.x,
                       y = adjusted.y,
                       z = adjusted.r)
    }

}