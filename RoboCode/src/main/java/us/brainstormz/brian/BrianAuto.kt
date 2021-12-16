package us.brainstormz.brian

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.brian.Brian_Hardware.PIVOTARM_CONSTANT
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.hardwareClasses.JamesEncoderMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard

//@Autonomous(name = "Meet 0 Brian Auto", group = "B")
class BrianAuto: LinearOpMode() {
    val hardware = BrianHardware()
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))
//    val newMove = JamesEncoderMovement(hardware, telemetry)
    val wizard = TelemetryWizard(TelemetryConsole(telemetry), this)
    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        wizard.newMenu("Alliance", "Which Alliance are we on?", listOf("Blue", "Red"), "Park", firstMenu = true)
        wizard.newMenu("Park", "Should we deliver the duck or, park in warehouse?", listOf("Ducc", "Warehouse"))
        wizard.summonWizard(gamepad1)

        waitForStart()
        /** AUTONOMOUS  PHASE */

//        newMove.changePosition(1.0, 25.0, 25.0, 0.0)

//        if(true/*wizard.wasItemChosen("Alliance", "Blue")*/) {
////        drop starting block
//            movement.driveRobotStrafe(power = 1.0, inches = 8.0, true)
//            movement.driveRobotPosition(power = 1.0, inches = -15.0, true)
//            movement.driveRobotTurn(power = 1.0, degree = 85.0, true)
//            movement.driveRobotPosition(power = 1.0, inches = -25.0, true)
//            pivotArmSetRotation(1.0, 50.0, true)
//            pivotArmSetRotation(0.5, 49.0, true)
//            sleep(400)
//            //extend
//            hardware.extendoArm5000.power = 0.5
//            sleep(200)
//            hardware.extendoArm5000.power = 1.0
//            sleep(300)
//            hardware.extendoArm5000.power = 0.0
//            sleep(1000)
//
//            hardware.collectorGate.position = 0.25
//            sleep(1500)
//            hardware.collectorGate.position = 0.68
//
//
////        spin carousel
//            hardware.extendoArm5000.power = -1.0
//            pivotArmSetRotation(1.0, -90.0, true)
//            hardware.extendoArm5000.power = 0.0
//            movement.driveRobotPosition(1.0, 13.0, true)
//            movement.driveRobotTurn(1.0, -68.0, true)
//            movement.driveRobotPosition(1.0, 40.0, true)
//            movement.driveRobotTurn(1.0, -243.0, true)
//            movement.driveRobotPosition(1.0, -10.0, true)
//            movement.driveRobotPosition(0.5, -5.0, true)
//            hardware.duccSpinner.power = 1.0
//            sleep(3500)
//            hardware.duccSpinner.power = 0.0
//            return
//
//            if (wizard.wasItemChosen("Park", "Ducc")) {
////                deliver ducc
//                movement.driveRobotPosition(1.0, 15.0, true)
//                movement.driveRobotTurn(1.0, 220.0, true)
//                pivotArmSetRotation(1.0, -9.0, true)
//                hardware.collectOtron.power = 1.0
//                movement.driveRobotPosition(1.0, 2.0, true)
//                movement.driveRobotTurn(1.0, -50.0, true)
//                movement.driveRobotPosition(1.0, 3.0, true)
//                movement.driveRobotTurn(1.0, 70.0, true)
//                movement.driveRobotPosition(1.0, 5.0, true)
//                movement.driveRobotTurn(1.0, -70.0, true)
//                hardware.collectOtron.power = 0.3
//                //deliver
//                pivotArmSetRotation(1.0, 10.0, true)
//                movement.driveRobotTurn(1.0, 40.0, true)
//                movement.driveRobotStrafe(1.0, 60.0, true)
//                pivotArmSetRotation(1.0, 50.0, true)
//                pivotArmSetRotation(0.5, 49.0, true)
//                sleep(400)
//                //extend
//                hardware.extendoArm5000.power = 0.5
//                sleep(200)
//                hardware.extendoArm5000.power = 1.0
//                sleep(300)
//                hardware.extendoArm5000.power = 0.0
//                sleep(1000)
//
//                hardware.collectorGate.position = 0.25
//                sleep(1500)
//                hardware.collectorGate.position = 0.68
//            } else {
////            park in warehouse
//
//            }
//        } else /**Red*/ {
////        drop starting block
//            movement.driveRobotStrafe(power = 1.0, inches = -8.0, true)
//            movement.driveRobotPosition(power = 1.0, inches = -15.0, true)
//            movement.driveRobotTurn(power = 1.0, degree = -85.0, true)
//            movement.driveRobotPosition(power = 1.0, inches = -25.0, true)
//            pivotArmSetRotation(1.0, 50.0, true)
//            pivotArmSetRotation(0.5, 49.0, true)
//            sleep(400)
//            //extend
//            hardware.extendoArm5000.power = 0.5
//            sleep(200)
//            hardware.extendoArm5000.power = 1.0
//            sleep(300)
//            hardware.extendoArm5000.power = 0.0
//            sleep(1000)
//
//            hardware.collectorGate.position = 0.25
//            sleep(1500)
//            hardware.collectorGate.position = 0.68
//
////        spin carousel
//            hardware.extendoArm5000.power = -1.0
//            pivotArmSetRotation(1.0, -90.0, true)
//            hardware.extendoArm5000.power = 0.0
//            movement.driveRobotPosition(1.0, 13.0, true)
//            movement.driveRobotTurn(1.0, 65.0, true)
//            movement.driveRobotPosition(1.0, 40.0, true)
//            movement.driveRobotTurn(1.0, 129.0, true)
//            movement.driveRobotStrafe(1.0, 10.0, true)
//            movement.driveRobotStrafe(.5, 7.0, true)
//            hardware.duccSpinner.power = -1.0
//            sleep(3500)
//            hardware.duccSpinner.power = 0.0
//
////        deliver ducc
//            movement.driveRobotPosition(1.0, 15.0, true)
//            movement.driveRobotTurn(1.0, 220.0, true)
//            pivotArmSetRotation(1.0, -9.0, true)
//            hardware.collectOtron.power = 1.0
//            movement.driveRobotPosition(1.0, 2.0, true)
//            movement.driveRobotTurn(1.0, -50.0, true)
//            movement.driveRobotPosition(1.0, 3.0, true)
//            movement.driveRobotTurn(1.0, 70.0, true)
//            movement.driveRobotPosition(1.0, 5.0, true)
//            movement.driveRobotTurn(1.0, -70.0, true)
//            hardware.collectOtron.power = 0.3
//            //deliver
//            pivotArmSetRotation(1.0, 10.0, true)
//            movement.driveRobotTurn(1.0, 40.0, true)
//            movement.driveRobotStrafe(1.0, 60.0, true)
//            pivotArmSetRotation(1.0, 50.0, true)
//            pivotArmSetRotation(0.5, 49.0, true)
//            sleep(400)
//            //extend
//            hardware.extendoArm5000.power = 0.5
//            sleep(200)
//            hardware.extendoArm5000.power = 1.0
//            sleep(300)
//            hardware.extendoArm5000.power = 0.0
//            sleep(1000)
//
//            hardware.collectorGate.position = 0.25
//            sleep(1500)
//            hardware.collectorGate.position = 0.68
//
////        park in warehouse
//        }
    }


    /**
     * PivotArmSetRotation
     * Positive swings up/back
     * @param power Power level
     * @param degrees Degrees of rotation
     * @param asynkk Allows for another function to take place simultaneously
     */
    fun pivotArmSetRotation(power: Double, degrees: Double, asynkk: Boolean) {
        val position: Int = (degrees* PIVOTARM_CONSTANT).toInt()
        if (hardware.pivotArm1.mode != DcMotor.RunMode.RUN_TO_POSITION ||
            hardware.pivotArm2.mode != DcMotor.RunMode.RUN_TO_POSITION)
        {
            hardware.pivotArm1.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.pivotArm2.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.pivotArm1.mode = DcMotor.RunMode.RUN_TO_POSITION
            hardware.pivotArm2.mode = DcMotor.RunMode.RUN_TO_POSITION
        }
        hardware.pivotArm1.power = power
        hardware.pivotArm2.power = power
        hardware.pivotArm1.targetPosition = hardware.pivotArm1.getTargetPosition()+position
        hardware.pivotArm2.targetPosition = hardware.pivotArm2.getTargetPosition()+position

        if (asynkk)
            return

        sleep(150)

        for (i in 0 until 2) {    // Repeat check 3 times, sleeping 10ms between,
        // as isBusy can be a bit unreliable
        while (hardware.pivotArm1.isBusy && hardware.pivotArm2.isBusy) {
            telemetry.addLine("pivotArm1: ${hardware.pivotArm1.targetPosition} target, ${hardware.pivotArm1.currentPosition} current")
            telemetry.addLine("pivotArm2: ${hardware.pivotArm2.targetPosition} target, ${hardware.pivotArm2.currentPosition} current")
            if ((hardware.pivotArm1.velocity < 10 && hardware.pivotArm1.velocity > -10) &&
                (hardware.pivotArm2.velocity < 10 && hardware.pivotArm2.velocity > -10) && power != 0.0) {
                telemetry.addLine("Warning: Failsafe invoked on pivotArmSetRotation()")
                break;
            }
        }
        sleep(10);
    }
    }

    /**
     * PivotArmSetRotationAbs
     * Positive swings up/back
     * @param power Power level
     * @param degrees Degrees of rotation
     * @param asynkk Allows for another function to take place simultaneously
     */
    fun pivotArmSetRotationAbs(power: Double, degrees: Double, asynkk: Boolean) {
        val position = (degrees* PIVOTARM_CONSTANT).toInt()
        if (hardware.pivotArm1.mode != DcMotor.RunMode.RUN_TO_POSITION ||
            hardware.pivotArm2.mode != DcMotor.RunMode.RUN_TO_POSITION)
        {
            hardware.pivotArm1.mode = DcMotor.RunMode.RUN_TO_POSITION
            hardware.pivotArm2.mode = DcMotor.RunMode.RUN_TO_POSITION
        }
        hardware.pivotArm1.power = power
        hardware.pivotArm2.power = power
        hardware.pivotArm1.targetPosition = position
        hardware.pivotArm2.targetPosition = position

        if (asynkk)
            return

        sleep(150)

        for (i in 0 until 2) {    // Repeat check 3 times, sleeping 10ms between,
        // as isBusy can be a bit unreliable
        while (hardware.pivotArm1.isBusy && hardware.pivotArm2.isBusy) {
            telemetry.addLine("pivotArm1: ${hardware.pivotArm1.targetPosition} target, ${hardware.pivotArm1.currentPosition} current")
            telemetry.addLine("pivotArm2: ${hardware.pivotArm2.targetPosition} target, ${hardware.pivotArm2.currentPosition} current")
            if ((hardware.pivotArm1.velocity < 10 && hardware.pivotArm1.velocity > -10) &&
                (hardware.pivotArm2.velocity < 10 && hardware.pivotArm2.velocity > -10) && power != 0.0) {
                telemetry.addLine("Warning: Failsafe invoked on pivotArmSetRotation()")
                break;
            }
        }
        sleep(10);
    }
    }


}