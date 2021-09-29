package interfacing.localization

import locationTracking.PositionAndRotation

interface World {
    fun currentPositionAndRotation(): PositionAndRotation
}