package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.pid.PID
import us.brainstormz.rataTony.RataTonyHardware
import us.brainstormz.telemetryWizard.TelemetryConsole

class DepositorLK(private val hardware: RataTonyHardware, private val console: TelemetryConsole) {
    data class MovementConstraints(val allowedYHeights: MutableList<ClosedFloatingPointRange<Double>> = mutableListOf(),
                                   val allowedXLengths: MutableList<ClosedFloatingPointRange<Double>> = mutableListOf(),
                                   val allowedDropperPositions: MutableList<DropperPos> = mutableListOf()) {
        fun inLimit() {

        }
    }
    data class SlideConversions(private val countsPerMotorRev: Double = 28.0,
                                private val gearReduction: Int = 1 / 1,
                                private val spoolCircumferenceMM: Double = 112.0,
                                val countsPerInch: Double = countsPerMotorRev * gearReduction / (spoolCircumferenceMM / 25.4))

//    Dropper Variables
    enum class DropperPos(val posValue: Double) {
        Open(0.0),
        Closed(0.7)
    }

//    X Variables
    private val xPrecision = 1
    val xConversion = SlideConversions(countsPerMotorRev = 28.0)
    val xConstraints = MovementConstraints(allowedYHeights = mutableListOf(),
                                           allowedDropperPositions = mutableListOf(DropperPos.Closed))
    val xPID = PID()

//    Y Variables
    val yConversion = SlideConversions(countsPerMotorRev = 28.0)
    val yConstraints = MovementConstraints(allowedXLengths = mutableListOf(),
                                           allowedDropperPositions = mutableListOf(DropperPos.Closed, DropperPos.Open))

    fun moveToPosition(yIn: Double, xIn: Double) {
        var yAtTarget = false
        var xAtTarget = false

        while (!(yAtTarget && xAtTarget) || opmode.opModeIsActive()) {

            if ()
            yAtTarget = yTowardPosition(yIn)

            xAtTarget = xTowardPosition(xIn)
        }
    }

    fun xTowardPosition(inches: Double): Boolean {
        val targetCounts = (inches * xConversion.countsPerInch).toInt()

        val error = targetCounts - hardware.horiMotor.currentPosition
        hardware.horiMotor.power = xPID.calcPID(error.toDouble())

        return if (error in (-xPrecision..xPrecision)) {
            hardware.horiMotor.power = 0.0
            true
        } else {
            false
        }
    }

    fun yTowardPosition(inches: Double): Boolean {
        val targetCounts = (inches * yConversion.countsPerInch).toInt()

        hardware.liftMotor.power = if (targetCounts - hardware.liftMotor.currentPosition > 0)
            1.0
        else
            0.5

        hardware.liftMotor.targetPosition = targetCounts
        hardware.liftMotor.mode = DcMotor.RunMode.RUN_TO_POSITION

        return hardware.liftMotor.isBusy
    }

    /**
     * run at the beginning of the program
     * */
    private lateinit var opmode: LinearOpMode
    fun runInLinearOpmode(opmode: LinearOpMode) {
        this.opmode = opmode
    }
}

class OldDepositorLK(private val hardware: RataTonyHardware, private val console: TelemetryConsole) {


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



    /**
     * Synchronizes movements to avoid internal collisions
     */

//    Bucket stay closed conditions
    private val closedCauseX = 100..795

    //    x/y collisions
    private val collideCauseX = 100..795
    private val collideCauseY = 100.0..133.0

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

    private fun posOrNeg(num: Int): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }
}