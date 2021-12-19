package us.brainstormz.motion

import us.brainstormz.hardwareClasses.HardwareClass
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.pid.PID

interface Movement {
//    Implement in constructor
    val localizer: Localizer
    val hardware: HardwareClass

//    Not in constructor
    val precisionRange: ClosedRange<Double>
    val movementPID: PID

    fun move(target: PositionAndRotation, powerRange: ClosedRange<Double>)
    fun completeMovement(target: PositionAndRotation, powerRange: ClosedRange<Double>)
}