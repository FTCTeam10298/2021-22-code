package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.pid.PID
import kotlinx.coroutines.*
import us.brainstormz.telemetryWizard.TelemetryConsole
import java.lang.Thread.sleep
import kotlin.math.abs


class Depositor(private val hardware: RataTonyHardware, private val console: TelemetryConsole) {

    enum class DropperPos(val posValue: Double) {
        Open(0.0),
        Closed(0.7)
    }

    private val xPID = PID(kp = 0.0019, ki = 0.0000008)
    private val xPIDOut = PID(kp = 0.0019)
    private val xPIDOther = PID(kp = 0.002, ki = 0.00001)
    private val xPrecision = -30..30
    val outerLimit = 1900
    val innerLimit = -20

    private val yPID = PID(kp = 0.0012, ki = 0.0)
    private val yPIDLinear = PID(kp = 0.0012, ki = 0.00000005)
    private val yPIDDown = PID(kp = 0.0007, ki = 0.0)
    private var liftPower = 0.0
    private val yPrecision = -1..1
    private val upperLimit = 1800
    val lowerLimit = -1
    enum class LiftPos(val counts: Int) {
        LowGoal(410),
        MidGoal(950),
        HighGoal(1750)
    }

    fun xTowardPosition(targetPos: Int): Boolean {
        val error = targetPos - hardware.horiMotor.currentPosition

        if (canXMove(targetPos))
            hardware.horiMotor.power = xPIDOther.calcPID(error.toDouble())

        return if (error in xPrecision) {
            console.display(3, "")
            hardware.horiMotor.power = 0.0
            true
        } else {
            console.display(3, "moving x")
            false
        }
    }

    fun yTowardPosition(targetPos: Int): Boolean {
        val error = targetPos - hardware.liftMotor.currentPosition

        liftPower = if (canYMove(targetPos)) {
            if (error > 0)
                yPIDLinear.calcPID(error.toDouble())
            else
                yPID.calcPID(error.toDouble())
        } else
            0.0

        return if (error in yPrecision) {
            console.display(4, "")
            true
        } else {
            console.display(4, "moving y")
            false
        }
    }

    private lateinit var opmode: LinearOpMode

    fun xToPosition(targetPos: Int) {
        while (opmode.opModeIsActive()) {
            val atPosition = xTowardPosition(targetPos)

            if (atPosition)
                break
        }
    }

    fun yToPosition(targetPos: Int) {
        if (canYMove(targetPos)) {
            if (targetPos - hardware.liftMotor.currentPosition > 0)
                hardware.liftMotor.power = 1.0
            else
                hardware.liftMotor.power = 0.5

            hardware.liftMotor.targetPosition = targetPos
            hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

            while (opmode.opModeIsActive() && hardware.liftMotor.isBusy) {
                sleep(10)
            }
        }
    }

    fun xAtPower(power: Double) {
        val anticipatedStop = (10 * posOrNeg(power)) + hardware.horiMotor.currentPosition

        val target = when {
            power > 0 -> outerLimit
            power < 0 -> innerLimit
            else -> hardware.horiMotor.currentPosition
        }

        hardware.horiMotor.power = if (canXMove(anticipatedStop)) {
            when {
                power > 0 -> {
                    xPIDOut.calcPID(target.toDouble(), hardware.horiMotor.currentPosition.toDouble())
                }
                power < 0 -> {
                    xPID.calcPID(target.toDouble(), hardware.horiMotor.currentPosition.toDouble())
                }
                else -> 0.0
            }
        } else
            0.0
    }

    fun yAtPower(power: Double) {
        val anticipatedStop = (40 * posOrNeg(power)) + hardware.liftMotor.currentPosition

        val target = when {
            power > 0 -> upperLimit
            power < 0 -> lowerLimit
            else -> hardware.liftMotor.currentPosition
        }

        hardware.liftMotor.power = if (canYMove(anticipatedStop))
            if (target == lowerLimit)
                yPIDDown.calcPID(target.toDouble(), hardware.liftMotor.currentPosition.toDouble()).coerceAtLeast(-0.6)
            else
                yPID.calcPID(target.toDouble(), hardware.liftMotor.currentPosition.toDouble()).coerceAtLeast(-0.6)/*.coerceIn(-abs(power), abs(power))*/
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
    private val closedCauseX = 100..650

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

        val direction = posOrNeg(target - hardware.horiMotor.currentPosition.toDouble())

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

        val direction = posOrNeg(target - hardware.liftMotor.currentPosition.toDouble())

        val result = when {
            target < lowerLimit && direction == -1 -> false
            target > upperLimit && direction == 1 -> false
            (target <= collideCauseY && xTarget in collideCauseX) && direction == -1 -> false
            else -> true
        }

        console.display(8, "Y condition: $result")

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

    private var prevLiftPos = 0
    fun updateY() {
        if (hardware.liftMotor.power == 0.0) {
            hardware.liftMotor.targetPosition = prevLiftPos
            hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION
        } else {
            prevLiftPos = hardware.liftMotor.currentPosition
            hardware.liftMotor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }


    /**
     * run at the beginning of the program
     * */
    fun runInLinearOpmode(opmode: LinearOpMode) {
        this.opmode = opmode
    }

    private fun posOrNeg(num: Double): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }
}