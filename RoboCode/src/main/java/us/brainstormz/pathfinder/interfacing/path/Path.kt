package interfacing.path

import locationTracking.PositionAndRotation

interface Path {
    fun length():Double
    fun positionAt(distance:Double): PositionAndRotation
}