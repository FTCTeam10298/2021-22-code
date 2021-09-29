package interfacing

import locationTracking.PositionAndRotation
import interfacing.localization.World
import interfacing.path.Path

interface PathFinder {
    fun calculatePath(world: World, from: PositionAndRotation, to: PositionAndRotation): Path
}