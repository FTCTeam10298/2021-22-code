package us.brainstormz.localizer

import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.*

class EncoderLocalizer(private val hardware: MecanumHardware, private val console: TelemetryConsole): Localizer {

    var countsPerMotorRev = 28.0 // Rev HD Hex v2.1 Motor encoder
    var gearboxRatio = 19.2 // 40 for 40:1, 20 for 20:1
    var driveGearReduction = 1 / 1 // This is > 1.0 if geared for torque
    var wheelDiameterInches = 3.77953 // For figuring circumference
    var drivetrainError = 1.0 // Error determined from testing
    val countsPerInch = countsPerMotorRev * gearboxRatio * driveGearReduction / (wheelDiameterInches * PI) / drivetrainError
    val countsPerDegree: Double = countsPerInch * 0.268 * 2/3 // Found by testing
    var trackWidth = 15


    private var currentPos = PositionAndRotation()
    private var previousPos = PositionAndRotation()

    override fun currentPositionAndRotation(): PositionAndRotation = currentPos

    override fun recalculatePositionAndRotation() {

        val lF = hardware.lFDrive.currentPosition
        val rF = hardware.rFDrive.currentPosition
        val lB = hardware.lBDrive.currentPosition
        val rB = hardware.rBDrive.currentPosition

        val currentX = -(-lF + rF + lB - rB) / 4 / countsPerInch
        val currentY = (lF + rF + lB + rB) / 4 / countsPerInch
        val currentR = -(-lF + rF - lB + rB) / 4 / countsPerDegree
        currentPos = PositionAndRotation(currentX, currentY, currentR)
//
//        val deltaY = cos(previousPos.r) * (currentY - previousPos.y) - sin(previousPos.r) * (currentX - previousPos.x)
//        val deltaX = sin(previousPos.r) * (currentY - previousPos.y) + cos(previousPos.r) * -(currentX - previousPos.x)
//        val deltaPos = PositionAndRotation(deltaX, deltaY, (currentR - previousPos.r))
//
//        val fromDelta = previousPos + deltaPos
//        console.display(12, "Other Current Pos: $fromDelta")
//
//        previousPos = fromDelta
    }

    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?) {
        currentPos.setCoordinate(x, y, r)
    }

    override fun startNewMovement() {
        setPositionAndRotation(0.0, 0.0, 0.0)
    }
}