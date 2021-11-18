package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.examples.ExampleHardware
import us.brainstormz.pid.PID
import us.brainstormz.rattatoni.MinibotHardware
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.PI
import kotlin.math.abs

class JamesEncoderMovement (private val hardware: MecanumHardware, private val console: TelemetryConsole): MecanumDriveTrain(hardware) {

    var countsPerMotorRev = 537.0 // Rev HD Hex v2.1 Motor encoder
    var gearboxRatio = 19.2 // 40 for 40:1, 20 for 20:1
    var driveGearReduction = 1 / 1 // This is > 1.0 if geared for torque
    var wheelDiameterInches = 96.0 // For figuring circumference
    var drivetrainError = 1.0 // Error determined from testing
    val countsPerInch = countsPerMotorRev * gearboxRatio * driveGearReduction / (wheelDiameterInches * PI) / drivetrainError
    val countsPerDegree: Double = countsPerInch * 0.26 // Found by testing
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

        val avgTarget = (lfTarget + rfTarget + lbTarget + rbTarget) / 4
        fun avgCurrent(): Int = (hardware.lFDrive.currentPosition + hardware.rFDrive.currentPosition + hardware.lBDrive.currentPosition + hardware.rBDrive.currentPosition) / 4

        console.display(1, "avg Target: $avgTarget")
        console.display(2, "avg current: ${avgCurrent()}")

        fun pidValue(): Double = pid.calcPID(avgCurrent().toDouble(), avgTarget.toDouble())
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
                console.display(1, "avg Target: $avgTarget")
                console.display(2, "avg current: ${avgCurrent()}")
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

//    /**
//     * DriveRobotPosition drives the robot the set number of inches at the given power level.
//     * @param inches How far to drive, can be negative
//     * @param power Power level to set motors to
//     */
//    fun driveRobotPosition(power: Double, inches: Double, smartAccel: Boolean) {
//
//        var state = 0 // 0 = NONE, 1 = ACCEL, 2 = DRIVE, 3 = DECEL
//        val position: Double = inches * countsPerInch
//
//
//        if (smartAccel && power > 0.25) {
//            drivePowerAll(0.25) // Use abs() to make sure power is positive
//            state = 1 // ACCEL
//        } else {
//            drivePowerAll(abs(power)) // Use abs() to make sure power is positive
//        }
//        val flOrigTarget: Int = hardware.lFDrive.targetPosition
//        val frOrigTarget: Int = hardware.rFDrive.targetPosition
//        val blOrigTarget: Int = hardware.lBDrive.targetPosition
//        val brOrigTarget: Int = hardware.rBDrive.targetPosition
//        driveSetRunToPosition()
//        driveAddTargetPosition(position.toInt(), position.toInt(), position.toInt(), position.toInt())
//        for (i in 0..4) {    // Repeat check 5 times, sleeping 10ms between,
//            // as isBusy can be a bit unreliable
//            while (driveAllAreBusy()) {
//                val flDrive: Int = hardware.lFDrive.currentPosition
//                val frDrive: Int = hardware.rFDrive.currentPosition
//                val blDrive: Int = hardware.lBDrive.currentPosition
//                val brDrive: Int = hardware.rBDrive.currentPosition
//                console.display(3, "Front left encoder: $flDrive")
//                console.display(4, "Front right encoder: $frDrive")
//                console.display(5, "Back left encoder: $blDrive")
//                console.display(6, "Back right encoder $brDrive")
//                console.display(7, "Front left target: ${hardware.lFDrive.targetPosition}")
//                console.display(8, "Front right target: ${hardware.rFDrive.targetPosition}")
//                console.display(9, "Back left target: ${hardware.lBDrive.targetPosition}")
//                console.display(10, "Back right target ${hardware.rBDrive.targetPosition}")
//
//                // State magic
//                if (state == 1 &&
//                    (abs(flDrive - flOrigTarget) > 2 * countsPerInch || abs(frDrive - frOrigTarget) > 2 * countsPerInch || abs(blDrive - blOrigTarget) > 2 * countsPerInch || abs(brDrive - brOrigTarget) > 2 * countsPerInch)) {
//                    // We have gone 2 inches, go to full power
//                    drivePowerAll(abs(power)) // Use abs() to make sure power is positive
//                    state = 2
//                } else if (state == 2 &&
//                    (abs(flDrive - flOrigTarget) > countsPerInch * (abs(inches) - 2) || abs(frDrive - frOrigTarget) > countsPerInch * (abs(inches) - 2) || abs(blDrive - blOrigTarget) > countsPerInch * (abs(inches) - 2) || abs(brDrive - brOrigTarget) > countsPerInch * (abs(inches) - 2))) {
//                    // Cut power by half to DECEL
//                    drivePowerAll(abs(power) / 2) // Use abs() to make sure power is positive
//                    state = 3 // We are DECELing now
//                }
//                console.display(7, "State: $state (0=NONE,1=ACCEL,2=DRIVING,3=DECEL")
//            }
//            Thread.sleep(10)
//        }
//        drivePowerAll(0.0)
//        // Clear used section of dashboard
//        console.display(3, "")
//        console.display(4, "")
//        console.display(5, "")
//        console.display(6, "")
//        console.display(7, "")
//    }
//
//
//    fun driveRobotTurn(power: Double, degree: Double, smartAccel: Boolean) {
//
//        val position: Double = degree * countsPerDegree
//        var state = 0 // 0 = NONE, 1 = ACCEL, 2 = DRIVE, 3 = DECEL
//
//        driveSetRunToPosition()
//        if (smartAccel) {
//            state = 1
//            driveSetPower(power * 0.5, -power * 0.5, power * 0.5, -power * 0.5)
//        } else {
//            driveSetPower(power, -power, power, -power)
//        }
//        val flOrigTarget: Int = hardware.lFDrive.targetPosition
//        val frOrigTarget: Int = hardware.rFDrive.targetPosition
//        val blOrigTarget: Int = hardware.lBDrive.targetPosition
//        val brOrigTarget: Int = hardware.rBDrive.targetPosition
//        driveAddTargetPosition(position.toInt(), -position.toInt(), position.toInt(), -position.toInt())
//        for (i in 0..4) {    // Repeat check 5 times, sleeping 10ms between,
//            // as isBusy can be a bit unreliable
//            while (driveAllAreBusy()) {
//                val flDrive: Int = hardware.lFDrive.currentPosition
//                val frDrive: Int = hardware.rFDrive.currentPosition
//                val blDrive: Int = hardware.lBDrive.currentPosition
//                val brDrive: Int = hardware.rBDrive.currentPosition
//                console.display(3, "Front left encoder: $flDrive")
//                console.display(4, "Front right encoder: $frDrive")
//                console.display(5, "Back left encoder: $blDrive")
//                console.display(6, "Back right encoder: $brDrive")
//
//                // State magic
//                if (state == 1 &&
//                    (abs(flDrive - flOrigTarget) > countsPerDegree * 10 || abs(frDrive - frOrigTarget) > countsPerDegree * 10 || abs(blDrive - blOrigTarget) > countsPerDegree * 10 || abs(brDrive - brOrigTarget) > countsPerDegree * 10)) {
//                    // We have rotated 10 degrees, go to full power
//                    drivePowerAll(abs(power)) // Use abs() to make sure power is positive
//                    state = 2
//                } else if (state == 2 &&
//                    (abs(flDrive - flOrigTarget) > countsPerDegree * (abs(degree) - 10) || abs(frDrive - frOrigTarget) > countsPerDegree * (abs(degree) - 10) || abs(blDrive - blOrigTarget) > countsPerDegree * (abs(degree) - 10) || abs(brDrive - brOrigTarget) > countsPerDegree * (abs(degree) - 10))) {
//                    // We are within 10 degrees of our destination, cut power by half to DECEL
//                    drivePowerAll(abs(power) / 2) // Use abs() to make sure power is positive
//                    state = 3 // We are DECELing now
//                }
//                console.display(7, "State: $state (0=NONE,1=ACCEL,2=DRIVING,3=DECEL")
//            }
//            Thread.sleep(10)
//        }
//        drivePowerAll(0.0)
//        // Clear used section of dashboard
//        console.display(3, "")
//        console.display(4, "")
//        console.display(5, "")
//        console.display(6, "")
//        console.display(7, "")
//    }
//
//    /**
//     * DriveRobotStrafe drives the robot the set number of inches at the given power level.
//     * @param inches How far to drive, can be negative
//     * @param power Power level to set motors to
//     */
//    fun driveRobotStrafe(power: Double, inches: Double, smartAccel: Boolean) {
//
//        var state = 0 // 0 = NONE, 1 = ACCEL, 2 = DRIVE, 3 = DECEL
//        val position: Double = inches * countsPerInch
//
//        if (smartAccel && power > 0.25) {
//            drivePowerAll(0.25) // Use abs() to make sure power is positive
//            state = 1 // ACCEL
//        } else {
//            drivePowerAll(abs(power)) // Use abs() to make sure power is positive
//        }
//        val flOrigTarget: Int = hardware.lFDrive.targetPosition
//        val frOrigTarget: Int = hardware.rFDrive.targetPosition
//        val blOrigTarget: Int = hardware.lBDrive.targetPosition
//        val brOrigTarget: Int = hardware.rBDrive.targetPosition
//
//        driveSetRunToPosition()
//        driveAddTargetPosition(-position.toInt(), position.toInt(), position.toInt(), -position.toInt())
//        for (i in 0..4) {    // Repeat check 5 times, sleeping 10ms between,
//            // as isBusy can be a bit unreliable
//            while (driveAllAreBusy()) {
//                val flDrive: Int = hardware.lFDrive.currentPosition
//                val frDrive: Int = hardware.rFDrive.currentPosition
//                val blDrive: Int = hardware.lBDrive.currentPosition
//                val brDrive: Int = hardware.rBDrive.currentPosition
//                console.display(3, "Front left encoder: $flDrive")
//                console.display(4, "Front right encoder: $frDrive")
//                console.display(5, "Back left encoder: $blDrive")
//                console.display(6, "Back right encoder $brDrive")
//                console.display(7, "Front left target: ${hardware.lFDrive.targetPosition}")
//                console.display(8, "Front right target: ${hardware.rFDrive.targetPosition}")
//                console.display(9, "Back left target: ${hardware.lBDrive.targetPosition}")
//                console.display(10, "Back right target ${hardware.rBDrive.targetPosition}")
//
//                // State magic
//                if (state == 1 &&
//                    (abs(flDrive - flOrigTarget) > 2 * countsPerInch || abs(frDrive - frOrigTarget) > 2 * countsPerInch || abs(blDrive - blOrigTarget) > 2 * countsPerInch || abs(brDrive - brOrigTarget) > 2 * countsPerInch)) {
//                    // We have gone 2 inches, go to full power
//                    drivePowerAll(abs(power)) // Use abs() to make sure power is positive
//                    state = 2
//                } else if (state == 2 &&
//                    (abs(flDrive - flOrigTarget) > countsPerInch * (abs(inches) - 2) || abs(frDrive - frOrigTarget) > countsPerInch * (abs(inches) - 2) || abs(blDrive - blOrigTarget) > countsPerInch * (abs(inches) - 2) || abs(brDrive - brOrigTarget) > countsPerInch * (abs(inches) - 2))) {
//                    // Cut power by half to DECEL
//                    drivePowerAll(abs(power) / 2) // Use abs() to make sure power is positive
//                    state = 3 // We are DECELing now
//                }
//                console.display(7, "State: $state (0=NONE,1=ACCEL,2=DRIVING,3=DECEL")
//            }
//            Thread.sleep(10)
//        }
//        drivePowerAll(0.0)
//        // Clear used section of dashboard
//        console.display(3, "")
//        console.display(4, "")
//        console.display(5, "")
//        console.display(6, "")
//        console.display(7, "")
//    }
//
//    /**
//     * DriveSidewaysTime makes the robot drive sideways for the specified time and power.
//     * @param time How long to drive in seconds
//     * @param power The power to use while driving,
//     * positive values go right and negative values go left
//     */
//    fun driveSidewaysTime(time: Double, power: Double) {
//        driveSetMode(DcMotor.RunMode.RUN_USING_ENCODER)
//        driveSetPower(-power, power, power, -power)
//
//        // Continue driving for the specified amount of time, then stop
//        val ms = time * 1000
//        Thread.sleep(ms.toLong())
//        drivePowerAll(0.0)
//        driveSetRunToPosition()
//        driveSetTargetPosition(0, 0, 0, 0)
//    }
//
//    /**
//     * DriveRobotHug is used to make the robot drive hugging a wall.
//     * The robot will move mostly straight and slightly to the side,
//     * so it will stay against the wall.
//     * @param power Power to use while driving
//     * @param inches How many inches to drive
//     * @param hugLeft Whether to hug left or right
//     */
//    fun driveRobotHug(power: Double, inches: Int, hugLeft: Boolean) {
//
//        val position: Double = inches * countsPerInch
//
//        driveSetRunToPosition()
//        if (!hugLeft && inches > 0 || hugLeft && inches < 0) {
//            driveSetPower(power * .5, power, power, power * .5)
//            driveAddTargetPosition(position.toInt() / 2, position.toInt(), position.toInt(), position.toInt() / 2)
//        } else if (!hugLeft && inches < 0 || hugLeft && inches > 0) {
//            driveSetPower(power, power * .5, power * .5, power)
//            driveAddTargetPosition(position.toInt(), position.toInt() / 2, position.toInt() / 2, position.toInt())
//        }
//
//        for (i in 0..4) {    // Repeat check 5 times, sleeping 10ms between,
//            // as isBusy can be a bit unreliable
//            while (driveAllAreBusy()) {
//                console.display(3, "Left front encoder: ${hardware.lFDrive.currentPosition}")
//                console.display(4, "Right front encoder: ${hardware.rFDrive.currentPosition}")
//                console.display(5, "Left back encoder: ${hardware.lBDrive.currentPosition}")
//                console.display(6, "Right back encoder: ${hardware.rBDrive.currentPosition}")
//            }
//            Thread.sleep(10)
//        }
//        drivePowerAll(0.0)
//    }
//
//    fun driveRobotArc(power: Double, inches: Double, difference: Double) {
//
//        var difference = difference
//        val position: Double = inches * countsPerInch
//
//        difference = Range.clip(difference, -1.0, 1.0)
//        //power 1, inches -48, difference -.5
//        driveSetMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)
//        if (difference > 0) {
//            hardware.rFDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
//            hardware.rBDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
//            hardware.lFDrive.mode = DcMotor.RunMode.RUN_TO_POSITION
//            hardware.lBDrive.mode = DcMotor.RunMode.RUN_TO_POSITION
//        } else {
//            hardware.rFDrive.mode = DcMotor.RunMode.RUN_TO_POSITION
//            hardware.rBDrive.mode = DcMotor.RunMode.RUN_TO_POSITION
//            hardware.lFDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
//            hardware.lBDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
//        }
//
//        if (difference > 0 && inches > 0)
//            driveSetPower(abs(power), abs(power * difference), abs(power), abs(power * difference))
//        else if (difference > 0 && inches < 0)
//            driveSetPower(abs(power), -abs(power * difference), abs(power), -abs(power * difference))
//        else if (difference < 0 && inches > 0)
//            driveSetPower(abs(power * difference), abs(power), abs(power * difference), abs(power))
//        else if (difference < 0 && inches < 0)
//            driveSetPower(-abs(power * difference), abs(power), -abs(power * difference), abs(power))
//
//        if (difference > 0) {
//            hardware.lFDrive.targetPosition = position.toInt()
//            hardware.lBDrive.targetPosition = position.toInt()
//        } else {
//            hardware.rFDrive.targetPosition = position.toInt()
//            hardware.rBDrive.targetPosition = position.toInt()
//        }
//        for (i in 0..4) {    // Repeat check 5 times, sleeping 10ms between,
//            // as isBusy can be a bit unreliable
//            if (difference > 0) {
//                while (hardware.lFDrive.isBusy && hardware.lBDrive.isBusy) {
//                    val flDrive: Int = hardware.lFDrive.currentPosition
//                    val blDrive: Int = hardware.lBDrive.currentPosition
//                    console.display(3, "Front left encoder: $flDrive")
//                    console.display(4, "Back left encoder: $blDrive")
//                }
//            } else {
//                while (hardware.rFDrive.isBusy && hardware.rBDrive.isBusy) {
//                    val frDrive: Int = hardware.rFDrive.currentPosition
//                    val brDrive: Int = hardware.rBDrive.currentPosition
//                    console.display(3, "Front left encoder: $frDrive")
//                    console.display(4, "Back left encoder: $brDrive")
//                }
//            }
//            Thread.sleep(10)
//        }
//        drivePowerAll(0.0)
//    }
//
//    fun driveRobotArcStrafe(power: Double, inches: Double, difference: Double) {
//
//        var difference = difference
//        val position: Double = inches * countsPerInch
//
//        difference = Range.clip(difference, -1.0, 1.0)
//        //power 1, inches -48, difference -.5
//        driveSetMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)
//        if (difference > 0) {
//            hardware.rFDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
//            hardware.rBDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
//            hardware.lFDrive.mode = DcMotor.RunMode.RUN_TO_POSITION
//            hardware.lBDrive.mode = DcMotor.RunMode.RUN_TO_POSITION
//        } else {
//            hardware.rFDrive.mode = DcMotor.RunMode.RUN_TO_POSITION
//            hardware.rBDrive.mode = DcMotor.RunMode.RUN_TO_POSITION
//            hardware.lFDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
//            hardware.lBDrive.mode = DcMotor.RunMode.RUN_USING_ENCODER
//        }
//
//        if (difference > 0 && inches > 0)
//            driveSetPower(-abs(power), abs(power * difference), abs(power), -abs(power * difference))
//        else if (difference > 0 && inches < 0)
//            driveSetPower(-abs(power), -abs(power * difference), abs(power), abs(power * difference))
//        else if (difference < 0 && inches > 0)
//            driveSetPower(-abs(power * difference), abs(power), abs(power * difference), -abs(power))
//        else if (difference < 0 && inches < 0)
//            driveSetPower(abs(power * difference), abs(power), -abs(power * difference), -abs(power))
//
//        if (difference > 0) {
//            hardware.lFDrive.targetPosition = -position.toInt()
//            hardware.lBDrive.targetPosition = position.toInt()
//        } else {
//            hardware.rFDrive.targetPosition = position.toInt()
//            hardware.rBDrive.targetPosition = -position.toInt()
//        }
//
//        for (i in 0..4) {    // Repeat check 5 times, sleeping 10ms between,
//            // as isBusy can be a bit unreliable
//            if (difference > 0) {
//                while (hardware.lFDrive.isBusy && hardware.lBDrive.isBusy) {
//                    val flDrive: Int = hardware.lFDrive.currentPosition
//                    val blDrive: Int = hardware.lBDrive.currentPosition
//                    console.display(3, "Front left encoder: $flDrive")
//                    console.display(4, "Back left encoder: $blDrive")
//                }
//            } else {
//                while (hardware.rFDrive.isBusy && hardware.rBDrive.isBusy) {
//                    val frDrive: Int = hardware.rFDrive.currentPosition
//                    val brDrive: Int = hardware.rBDrive.currentPosition
//                    console.display(3, "Front left encoder: $frDrive")
//                    console.display(4, "Back left encoder: $brDrive")
//                }
//            }
//            Thread.sleep(10)
//        }
//        drivePowerAll(0.0)
//    }
}

@Autonomous
class NewMovementTest: LinearOpMode() {

    val console = TelemetryConsole(telemetry)
    val hardware = MinibotHardware()

    val movement = JamesEncoderMovement(hardware, console)

    override fun runOpMode() {
        hardware.init(hardwareMap)

        waitForStart()

        movement.changePosition(1.0, 10.0, 10.0, 90.0)
//        movement.changePosition(1.0, forwardIn = 10.0, 0.0, 0.0)
//        console.display(13, "forward 10")
//        sleep(1000)
//
//        movement.changePosition(1.0, 0.0, sidewaysIn = 10.0, 0.0)
//        console.display(13, "side 10")
//        sleep(1000)
//
//        movement.changePosition(1.0, 0.0, 0.0, rotationDegrees = 90.0)
//        console.display(13, "rotation 90")
//        sleep(1000)
    }

}