package us.brainstormz.localizer

import us.brainstormz.hardwareClasses.MecanumHardware
import kotlin.math.*

class EncoderLocalizer(private val hardware: MecanumHardware): Localizer {

    private var currentPos = PositionAndRotation()
    private var previousPos = PositionAndRotation()

    override fun currentPositionAndRotation(): PositionAndRotation = currentPos

    override fun recalculatePositionAndRotation() {

        val lF = hardware.lFDrive.currentPosition
        val rF = hardware.rFDrive.currentPosition
        val lB = hardware.lBDrive.currentPosition
        val rB = hardware.rBDrive.currentPosition

        val currentX = (-lF + rF + lB - rB) / 4
        val currentY = (lF + rF + lB + rB) / 4
        val currentR = (-lF + rF - lB + rB) / 4
//        currentPos = PositionAndRotation(currentX.toDouble(), currentY.toDouble(), currentR.toDouble())

        val deltaY = cos(previousPos.r) * currentY - sin(previousPos.r) * currentX
        val deltaX = sin(previousPos.r) * currentY + cos(previousPos.r) * currentX
        val deltaPos = PositionAndRotation(deltaX, deltaY, (currentR - previousPos.r))

        val currentPos = previousPos + deltaPos
        previousPos = currentPos
    }

    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?) {
        currentPos.setCoordinate(x, y, r)
    }

    override fun startNewMovement() {
        setPositionAndRotation(0.0, 0.0, 0.0)
    }
}