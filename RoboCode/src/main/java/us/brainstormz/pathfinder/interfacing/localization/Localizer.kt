package interfacing.localization

import locationTracking.PositionAndRotation

interface Localizer {
    fun currentPositionAndRotation(): PositionAndRotation
    fun recalculatePositionAndRotation()
    fun setPositionAndRotation(x: Double? = null, y: Double? = null, r: Double? = null)
}