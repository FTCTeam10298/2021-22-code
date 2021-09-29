package posePlanner

import us.brainstormz.localization.PositionAndRotation


data class AStarPoint(val point: PositionAndRotation, var gCost: Double, var hCost: Double, var parent: AStarPoint?, var movementPenalty: Double) {
    val fCost: Double get() = gCost + hCost

    constructor(point: PositionAndRotation): this(point, 0.0, 0.0, null, 0.0)

    override operator fun equals(other: Any?): Boolean {
        val other = other as AStarPoint
        return this.point == other.point
    }
    override fun toString(): String {
        return "AStarPoint=(point: $point, gCost: $gCost, hCost: $hCost, movementPenalty: $movementPenalty)"
    }
}

class AStar(start: PositionAndRotation, private val target: PositionAndRotation, private val obstructions: List<Obstruction>, private val planner: PosePlanner) {

    val allPoints: List<PositionAndRotation>
        get() = openSet.map { it.point } + closedSet.map { it.point } + anotherPoint
    var anotherPoint = PositionAndRotation()
    var hitpoint = listOf<PositionAndRotation>()

    val startNode = AStarPoint(start)

    var openSet: MutableList<AStarPoint> = mutableListOf(startNode)
    val closedSet: MutableList<AStarPoint> = mutableListOf()

    fun cheapestNode(): AStarPoint {
        val newNode: AStarPoint = openSet.minBy { it.fCost }!!
        openSet.remove(newNode)
        closedSet.add(newNode)

        hitpoint += newNode.point

        return newNode
    }

    fun expand(current: AStarPoint, allNeighbors: List<AStarPoint>): List<AStarPoint> {
        allNeighbors.filter { neighbour ->
            if (!closedSet.contains(neighbour)) {
                val newMovementCostToNeighbour: Double =
                    current.gCost + current.point.distance(neighbour.point) + neighbour.movementPenalty

                if (newMovementCostToNeighbour < neighbour.gCost || neighbour.gCost == 0.0) {
                    neighbour.gCost = newMovementCostToNeighbour
                    neighbour.hCost = neighbour.point.distance(target)
                    neighbour.parent = current

                    if (!openSet.contains(neighbour))
                        openSet.add(neighbour)
                    else
                        openSet[openSet.indexOfFirst { neighbour.point == it.point }] = neighbour
                }
                true
            } else
                false
        }

        return allNeighbors
    }

    fun addNeighbors(neighbors: List<AStarPoint>) {
        val filteredNeighbors = neighbors.filterNot{ neighbor ->
            val openSetContains = openSet.fold(false) { acc, it ->
                if (neighbor == it)
                    true
                else
                    acc
            }
            val closedSetContains = closedSet.fold(false) { acc, it ->
                if (neighbor == it)
                    true
                else
                    acc
            }
            openSetContains || closedSetContains
        }

        openSet.addAll(filteredNeighbors)
    }


    fun tracePath(lastNode: AStarPoint): List<PositionAndRotation> {
        val path: MutableList<AStarPoint> = mutableListOf()

        var currentNode: AStarPoint = lastNode
        while (currentNode.parent != null) {
            path.add(currentNode)
            currentNode = currentNode.parent!!
        }
        path.add(startNode)


        val waypoints: List<PositionAndRotation> = path.map { it.point }

        return waypoints.reversed()

    }
}