package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.pid.PID
import us.brainstormz.rataTony.RataTonyHardware
import us.brainstormz.telemetryWizard.TelemetryConsole
import java.lang.Thread.sleep
import kotlin.math.PI

class JamesEncoderMovement (private val hardware: MecanumHardware, private val console: TelemetryConsole): MecanumDriveTrain(hardware) {


    val countsPerMotorRev = 28.0 // Rev HD Hex v2.1 Motor encoder
    val gearboxRatio = 19.2 // 40 for 40:1, 20 for 20:1
    val driveGearReduction = 1 / 1 // This is > 1.0 if geared for torque
    val wheelDiameterInches = 3.77953 // For figuring circumference
    val drivetrainError = 1.0 // Error determined from testing
    val countsPerInch = countsPerMotorRev * gearboxRatio * driveGearReduction / (wheelDiameterInches * PI) / drivetrainError
    val countsPerDegree: Double = countsPerInch * 0.268 * 2/3 // Found by testing

    var pid = PID(0.001, 0.0, 0.0)
    val precision = -1.0..1.0

    private var opModeStop = false

    /**
     * DriveRobotPosition drives the robot the set number of inches at the given power level.
     * @param power Power level to set motors to. 0 - 1.0
     * @param forwardIn Number of inches to move forward. Can be negative.
     * @param sidewaysIn Number of inches to move sideways. Can be negative.
     */
    fun changePosition(power: Double, forwardIn: Double, sidewaysIn: Double, rotationDegrees: Double) {
        driveSetMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)
        driveSetMode(DcMotor.RunMode.RUN_USING_ENCODER)

        val y = forwardIn * countsPerInch
        val x = -sidewaysIn * countsPerInch
        val r = rotationDegrees * countsPerDegree
        val targetPos = PositionAndRotation(x, y, r)

        while (!opModeStop) {
            val lF = hardware.lFDrive.currentPosition
            val rF = hardware.rFDrive.currentPosition
            val lB = hardware.lBDrive.currentPosition
            val rB = hardware.rBDrive.currentPosition

            val currentX = -lF + rF + lB - rB
            val currentY = lF + rF + lB + rB
            val currentR = -lF + rF - lB + rB
            val currentPos = PositionAndRotation(currentX.toDouble(), currentY.toDouble(), currentR.toDouble())
            val posDifferance = targetPos - currentPos

            if (posDifferance in precision) {
                console.display(20, "done with it")
                break
            }

            val lfSpeed = (targetPos.y + targetPos.x - targetPos.r).toInt()
            val rfSpeed = (targetPos.y - targetPos.x + targetPos.r).toInt()
            val lbSpeed = (targetPos.y - targetPos.x - targetPos.r).toInt()
            val rbSpeed = (targetPos.y + targetPos.x + targetPos.r).toInt()

            val pidValue: Double = pid.calcPID(posDifferance).coerceIn(-power, power)

            val lfPower: Double = posOrNeg(lfSpeed) * pidValue
            val rfPower: Double = posOrNeg(rfSpeed) * pidValue
            val lbPower: Double = posOrNeg(lbSpeed) * pidValue
            val rbPower: Double = posOrNeg(rbSpeed) * pidValue

            driveSetPower(lfPower, rfPower, lbPower, rbPower)

            console.display(1, "avg Target: $targetPos")
            console.display(5, "avg current: $currentPos")

            console.display(8, "lfPower $lfPower")
            console.display(9, "rfPower $rfPower")
            console.display(10, "lbPower $lbPower")
            console.display(11, "rbPower $rbPower")
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

    fun onStop() {
        opModeStop = true
    }
}

@Autonomous(name= "James Movement Test", group= "Tests")
class NewMovementTest: OpMode() {

    val console = TelemetryConsole(telemetry)
    val hardware = RataTonyHardware()

    val joovement = JamesEncoderMovement(hardware, console)
    val movement = EncoderDriveMovement(hardware, console)

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun loop() {
        movement.driveRobotTurn(1.0, 90.0, true)
        console.display(20, "turned!")
        sleep(1000)
        joovement.changePosition(1.0, 0.0, 0.0, 90.0)
        console.display(20, "jooved!")
        sleep(1000)
        requestOpModeStop()
    }

    override fun stop() {
        joovement.onStop()
    }
}