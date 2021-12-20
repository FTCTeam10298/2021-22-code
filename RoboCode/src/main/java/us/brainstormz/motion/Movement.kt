package us.brainstormz.motion

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
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
    var linearOpMode: LinearOpMode

    fun goToPosition(target: PositionAndRotation, powerRange: ClosedRange<Double> = 0.0..1.0)
    fun moveTowardTarget(target: PositionAndRotation, powerRange: ClosedRange<Double> = 0.0..1.0): Boolean
}