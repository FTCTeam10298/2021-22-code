package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.pid.PID
import us.brainstormz.rataTony.RataTonyHardware
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.PI

class JamesEncoderMovement (private val hardware: MecanumHardware, private val console: TelemetryConsole): MecanumDriveTrain(hardware) {

    var countsPerMotorRev = 537.7 // Rev HD Hex v2.1 Motor encoder
    var gearboxRatio = 19.2 // 40 for 40:1, 20 for 20:1
    var driveGearReduction = 1 / 1 // This is > 1.0 if geared for torque
    var wheelDiameterInches = 96.0 // For figuring circumference
    var drivetrainError = 1.1 // Error determined from testing
    val countsPerInch = countsPerMotorRev * gearboxRatio * driveGearReduction / (wheelDiameterInches * PI) / drivetrainError
    val countsPerDegree: Double = countsPerInch * 0.28 // Found by testing
    var pid = PID(0.001, 0.0, 0.0)

    /**
     * DriveRobotPosition drives the robot the set number of inches at the given power level.
     * @param power Power level to set motors to. 0 - 1.0
     * @param forwardIn Number of inches to move forward. Can be negative.
     * @param sidewaysIn Number of inches to move sideways. Can be negative.
     */
    fun changePosition(power: Double, forwardIn: Double, sidewaysIn: Double, rotationDegrees: Double) {
        val y = forwardIn * countsPerInch
        val x = -sidewaysIn * countsPerInch
        val r = rotationDegrees * countsPerDegree

        val lfTarget = (y + x - r).toInt()
        val rfTarget = (y - x + r).toInt()
        val lbTarget = (y - x - r).toInt()
        val rbTarget = (y + x + r).toInt()
        console.display(4, "lfTarget $lfTarget")
        console.display(5, "rfTarget $rfTarget")
        console.display(6, "lbTarget $lbTarget")
        console.display(7, "rbTarget $rbTarget")

        val targetPos = lfTarget + rfTarget + lbTarget + rbTarget
        fun currentPos(): Int = hardware.lFDrive.currentPosition + hardware.rFDrive.currentPosition + hardware.lBDrive.currentPosition + hardware.rBDrive.currentPosition

        console.display(1, "avg Target: $targetPos")
        console.display(2, "avg current: ${currentPos()}")

        fun pidValue(): Double = pid.calcPID(currentPos().toDouble(), targetPos.toDouble()).coerceIn(-power, power)
        fun lfPower(): Double = posOrNeg(lfTarget) * pidValue()
        fun rfPower(): Double = posOrNeg(rfTarget) * pidValue()
        fun lbPower(): Double = posOrNeg(lbTarget) * pidValue()
        fun rbPower(): Double = posOrNeg(rbTarget) * pidValue()
        console.display(8, "lfPower ${lfPower()}")
        console.display(9, "rfPower ${rfPower()}")
        console.display(10, "lbPower ${lbPower()}")
        console.display(11, "rbPower ${rbPower()}")

        driveSetPower(lfPower(), rfPower(), lbPower(), rbPower())

        driveSetRunToPosition()
        driveAddTargetPosition(lfTarget, rfTarget, lbTarget, rbTarget)

        for (i in 0..4) {
            while (driveAllAreBusy()) {
                console.display(1, "avg Target: $targetPos")
                console.display(2, "avg current: ${currentPos()}")
                driveSetPower(lfPower(), rfPower(), lbPower(), rbPower())
            }
            Thread.sleep(10)
        }
        drivePowerAll(0.0)
    }

    private fun posOrNeg(num: Int): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }

}

//@Autonomous
class NewMovementTest: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val hardware = RataTonyHardware()

    val movement = JamesEncoderMovement(hardware, console)

    override fun runOpMode() {
        hardware.init(hardwareMap)

        waitForStart()

        movement.changePosition(1.0, 10.0, 0.0, 90.0)
    }

}