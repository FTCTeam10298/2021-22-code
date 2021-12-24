package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.pid.PID
import java.lang.Thread.sleep
import kotlinx.coroutines.*
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.abs


class Depositor(private val hardware: RataTonyHardware, private val console: TelemetryConsole) {

    enum class DropperPos(val posValue: Double) {
        Open(0.0),
        Closed(0.7)
    }

    private val xPID = PID(kp = 0.0018, ki = 0.0)
    private val xPrecision = -10..10
    private val outerLimit = 2500
    private val innerLimit = -35

    private val yPID = PID(kp = 0.0012, ki = 0.0)
    private var liftPower = 0.0
    private val yPrecision = -10..10
    private val upperLimit = 900
    private val lowerLimit = -1
    enum class LiftPos(val counts: Int) {
        LowGoal(190),
        MidGoal(510),
        HighGoal(850)
    }

    fun xTowardPosition(targetPos: Int): Boolean {
        val error = targetPos - hardware.horiMotor.currentPosition

        if (canXMove(targetPos))
            hardware.horiMotor.power = xPID.calcPID(error.toDouble())

        return if (error in xPrecision) {
            hardware.horiMotor.power = 0.0
            true
        } else
            false
    }

    fun yTowardPosition(targetPos: Int): Boolean {
        val error = targetPos - hardware.liftMotor.currentPosition

        if (canYMove(targetPos))
            liftPower = yPID.calcPID(error.toDouble())

        return error in yPrecision
    }

    fun xToPosition(targetPos: Int) {
        while (true) {
            val atPosition = xTowardPosition(targetPos)

            if (atPosition)
                break
        }
    }

    fun yToPosition(targetPos: Int) {
        while (true) {
            val atPosition = yTowardPosition(targetPos)

            if (atPosition) {
                break
            }
        }
    }

    fun xAtPower(power: Double) {
        val anticipatedStop = (10 * posOrNeg(power.toInt())) + hardware.horiMotor.currentPosition

        val target = when {
            power > 0 -> outerLimit
            power < 0 -> 0
            else -> hardware.horiMotor.currentPosition
        }

        hardware.horiMotor.power = if (canXMove(anticipatedStop))
            xPID.calcPID(target.toDouble(), hardware.horiMotor.currentPosition.toDouble())
        else
            0.0
    }

    fun yAtPower(power: Double) {
        val anticipatedStop = (40 * posOrNeg(power.toInt())) + hardware.liftMotor.currentPosition

        val target = when {
            power > 0 -> upperLimit
            power < 0 -> lowerLimit
            else -> hardware.liftMotor.currentPosition
        }

        liftPower = if (canYMove(anticipatedStop))
            yPID.calcPID(target.toDouble(), hardware.liftMotor.currentPosition.toDouble()).coerceAtLeast(-0.7)/*.coerceIn(-abs(power), abs(power))*/
        else
            0.0
    }

    fun xToPositionAsync(targetPos: Int): Unit = runBlocking { async {
        xToPosition(targetPos)
    } }

    fun yToPositionAsync(targetPos: Int): Unit = runBlocking { async {
        yToPosition(targetPos)
    } }

    fun drop() {
        if (canDropperDrop(DropperPos.Open))
            hardware.dropperServo.position = DropperPos.Open.posValue
    }

    fun close() {
        if (canDropperDrop(DropperPos.Closed))
            hardware.dropperServo.position = DropperPos.Closed.posValue
    }

    /**
     * Pre-set routines
     */
    fun home() {
        close()
        xTowardPosition(innerLimit)
        yTowardPosition(lowerLimit)
    }

    /**
     * Synchronizes movements to avoid internal collisions
     */

//    Bucket stay closed conditions
    private val closedCauseX = 100..795

    //    x/y collisions
    private val collideCauseX = 100..795
    private val collideCauseY = 100

    //    target trackers
    private var xTarget: Int? = null
    private var yTarget: Int? = null

    private fun canXMove(target: Int): Boolean {
        if (hardware.xInnerLimit.isPressed) {
            hardware.horiMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.horiMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }

        val direction = posOrNeg(target - hardware.horiMotor.currentPosition)

        val result = when {
            target < innerLimit && direction == -1 -> false
//            hardware.xInnerLimit.isPressed && direction == -1 -> false
            target > outerLimit && direction == 1 -> false
            (yTarget ?: hardware.liftMotor.currentPosition) <= collideCauseY -> false
            target in collideCauseX && DropperPos.Open.posValue == hardware.dropperServo.position -> false
            else -> true
        }

        console.display(6, "X condition: $result")

        xTarget = if (result)
            target
        else
            null

        return result
    }

    private fun canYMove(target: Int): Boolean {
        if (hardware.yLowerLimit.isPressed) {
            hardware.liftMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.liftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }

        val direction = posOrNeg(target - hardware.liftMotor.currentPosition)

        val result = when {
            target < lowerLimit && direction == -1 -> false
            target > upperLimit && direction == 1 -> false
            (target <= collideCauseY && xTarget in collideCauseX) && direction == -1 -> false
            else -> true
        }

        console.display(8, "Y condition: $result")
        console.display(9, "Y fully down: ${hardware.yLowerLimit.isPressed}")

        yTarget = if (result)
            target
        else
            null

        return result
    }

    fun canDropperDrop(target: DropperPos): Boolean {
        return when {
            xTarget in closedCauseX && DropperPos.Open == target -> false
            (yTarget ?: hardware.liftMotor.currentPosition) <= collideCauseY && DropperPos.Open == target -> false
            else -> true
        }
    }

    fun resolve() {

    }

    private var previousLiftPower = 0.0
    fun updateYPosition() {
        if (liftPower != 0.0) {
            hardware.liftMotor.power = liftPower
            previousLiftPower = liftPower
        } else if (liftPower == 0.0 && previousLiftPower != 0.0) {
            yTowardPosition(hardware.liftMotor.currentPosition)
            previousLiftPower = liftPower
        } /*else
            yTowardPosition(hardware.liftMotor.currentPosition)*/
    }

    private fun posOrNeg(num: Int): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }
}