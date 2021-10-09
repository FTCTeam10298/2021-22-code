/*
Copyright (c) 2016-19, FTC team #10298 Brain Stormz

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Brain Stormz nor the names of its contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package us.brainstormz.brian

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.Range
import kotlin.math.abs

@TeleOp(name = "Brian TeleOp Kotlin", group = "Brian")
class BrianTeleOp : OpMode() {
    /* Declare OpMode members. */
    var robot = BrianHardware() // use the class created to define FutureBot's hardware
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var inertia = 0.5
    var time_a = 0.0
    var dt = 0.0
    var direction = 1
    var collectOtronACTIVE = false
    var collectOtronSWITCHING = false
    var collectOtronREVERSE = false
    var refinatorACTIVE = false
    var hangPositionACTIVE = false
    var endGameModeACTIVE = false
    var endGameModeSWITCHING = false
    var target_position = 0.638

    // Code to run once when the driver hits INIT
    override fun init() {

        // Initialize the hardware variables.
        // The init() method of the hardware class does all the work here
        robot.init(hardwareMap)

        // Send telemetry message to signify robot waiting
        telemetry.addData("Say", "Robot ready")
    }

    // Code to run in a loop after the driver hits play until they hit the stop button
    override fun loop() {

        // Calculate loop Period (dt).
        // Let's not repeat last year's failure/laziness that killed our performance at Regionals...
        dt = runtime - time_a
        time_a = runtime
        telemetry.addData("Loop Time", "%f", dt)
        telemetry.addData("Inertia", "%f", inertia)
        telemetry.addData("Arm power", "%f", robot.pivotArm1.power)
        telemetry.addData("Arm position (raw)", robot.potentiometer.voltage)
        telemetry.addData("Arm position (degrees)", robot.pivotArmGetPosition())
        telemetry.addData("Current arm position (encoder counts)", robot.pivotArm1.currentPosition)

        // Send telemetry message to signify robot running
        telemetry.addData("Say", "N8 is the gr8est without deb8")
        //telemetry.addData("Extension arm encoder count", robot.extendoArm5000.getCurrentPosition());
        if (gamepad1.dpad_down && gamepad1.dpad_left || gamepad2.dpad_down && gamepad2.dpad_left) {
            robot.driveSetPower(-0.2, -1.0, -0.2, -1.0)
        } else if (gamepad1.dpad_up && gamepad1.dpad_left || gamepad2.dpad_up && gamepad2.dpad_left) {
            robot.driveSetPower(0.05, 1.0, 0.05, 1.0)
        } else if (gamepad1.dpad_down && gamepad1.dpad_right || gamepad2.dpad_down && gamepad2.dpad_right) {
            robot.driveSetPower(-1.0, -0.3, -1.0, -0.3)
        } else if (gamepad1.dpad_up && gamepad1.dpad_right || gamepad2.dpad_up && gamepad2.dpad_right) {
            robot.driveSetPower(1.0, 0.25, 1.0, 0.25)
        } else if (gamepad1.dpad_down || gamepad2.dpad_down) {
            robot.DrivePowerAll(-1.0)
        } else if (gamepad1.dpad_up || gamepad2.dpad_up) {
            robot.DrivePowerAll(1.0)
        } else if (gamepad1.dpad_right || gamepad2.dpad_right) {
            DriveSideways(-.5)
        } else if (gamepad1.dpad_left || gamepad2.dpad_left) {
            DriveSideways(.5)
        } else {
            y = if (gamepad1.left_stick_y > .1 || gamepad1.left_stick_y < -.1) {
                gamepad1.left_stick_y.toDouble()
            } else if (gamepad2.left_stick_y > .1 || gamepad2.left_stick_y < -.1) {
                gamepad2.left_stick_y.toDouble()
            } else {
                0.0
            }
            x = if (gamepad1.left_stick_x > .1 || gamepad1.left_stick_x < -.1) {
                gamepad1.left_stick_x.toDouble()
            } else if (gamepad2.left_stick_x > .1 || gamepad2.left_stick_x < -.1) {
                gamepad2.left_stick_x.toDouble()
            } else {
                0.0
            }
            z = if (gamepad1.right_stick_x > .1 || gamepad1.right_stick_x < -.1) {
                -gamepad1.right_stick_x * 0.8
            } else if (gamepad2.right_stick_x > .1 || gamepad2.right_stick_x < -.1) {
                -gamepad2.right_stick_x * 0.8
            } else {
                0.0
            }
            var maxvalue = abs(y + x - z)
            if (abs(y + x - z) > maxvalue) {
                maxvalue = abs(y + x - z)
            }
            if (abs(y - x + z) > maxvalue) {
                maxvalue = abs(y - x + z)
            }
            if (abs(y + x + z) > maxvalue) {
                maxvalue = abs(y + x + z)
            }
            if (abs(y - x - z) > maxvalue) {
                maxvalue = abs(y - x - z)
            }
            if (maxvalue < 1.0) {
                maxvalue = 1.0
            }
            val frontLeftPower =
                -1 * Range.clip((y - x + z) / maxvalue, -1.0, 1.0)
            val frontRightPower =
                -1 * Range.clip((y + x - z) / maxvalue, -1.0, 1.0)
            val backLeftPower =
                -1 * Range.clip((y + x + z) / maxvalue, -1.0, 1.0)
            val backRightPower =
                -1 * Range.clip((y - x - z) / maxvalue, -1.0, 1.0)
            if (frontLeftPower > 0.1 || frontRightPower > 0.1 || backLeftPower > 0.1 || backRightPower > 0.1
                || frontLeftPower < -0.1 || frontRightPower < -0.1 || backLeftPower < -0.1 || backRightPower < -0.1
            ) {
                inertia += 0.6 * dt
                inertia = Range.clip(inertia, 0.0, 1.0)
            } else {
                inertia = 0.4
            }
            robot.driveSetPower(
                frontLeftPower * inertia, frontRightPower * inertia,
                backLeftPower * inertia, backRightPower * inertia
            )
        }
        if (gamepad1.left_stick_button || gamepad2.left_stick_button) {
            endGameModeSWITCHING = true
        } else if (endGameModeSWITCHING) {
            endGameModeSWITCHING = false
            endGameModeACTIVE = !endGameModeACTIVE
        }
        if (gamepad1.x || gamepad2.x) {
            robot.markerDumper.power = 0.5
        } else {
            robot.markerDumper.power = 0.0
        }
        if (gamepad1.y || gamepad2.y) {
            refinatorACTIVE = false
            hangPositionACTIVE = false
            robot.pivotArm1.mode = DcMotor.RunMode.RUN_USING_ENCODER
            robot.pivotArm2.mode = DcMotor.RunMode.RUN_USING_ENCODER
            if (endGameModeACTIVE) {
                robot.pivotArm1.power = 0.25
                robot.pivotArm2.power = 0.25
            } else {
                robot.pivotArm1.power = 1.0
                robot.pivotArm2.power = 1.0
            }
            direction = 1
        } else if (gamepad1.a || gamepad2.a) {
            refinatorACTIVE = false
            hangPositionACTIVE = false
            robot.pivotArm1.mode = DcMotor.RunMode.RUN_USING_ENCODER
            robot.pivotArm2.mode = DcMotor.RunMode.RUN_USING_ENCODER
            if (endGameModeACTIVE) {
                robot.pivotArm1.power = -0.25
                robot.pivotArm2.power = -0.25
            } else {
                robot.pivotArm1.power = -1.0
                robot.pivotArm2.power = -1.0
            }
            direction = -1
        } else if (hangPositionACTIVE && robot.potentiometer.voltage > target_position + 0.1) {
            robot.pivotArm1.power = 1.0
            robot.pivotArm2.power = 1.0
        } else if (hangPositionACTIVE && robot.potentiometer.voltage < target_position - 0.1) {
            robot.pivotArm1.power = -1.0
            robot.pivotArm2.power = -1.0
        } else if (hangPositionACTIVE && robot.potentiometer.voltage > target_position + 0.02) {
            robot.pivotArm1.power = 0.1
            robot.pivotArm2.power = 0.1
        } else if (hangPositionACTIVE && robot.potentiometer.voltage < target_position - 0.02) {
            robot.pivotArm1.power = -0.1
            robot.pivotArm2.power = -0.1
        } else if (!refinatorACTIVE) {
            robot.pivotArm1.power = 0.0
            robot.pivotArm2.power = 0.0
        }
        if (gamepad1.right_stick_button || gamepad2.right_stick_button) {
            hangPositionACTIVE = true
        }
        if (gamepad1.right_trigger > 0) robot.extendoArm5000.power =
            -gamepad1.right_trigger.toDouble() else if (gamepad2.right_trigger > 0) robot.extendoArm5000.power =
            -gamepad2.right_trigger.toDouble() else if (gamepad1.left_trigger > 0) robot.extendoArm5000.power =
            gamepad1.left_trigger.toDouble() else if (gamepad2.left_trigger > 0) robot.extendoArm5000.power =
            gamepad2.left_trigger.toDouble() else robot.extendoArm5000.power = 0.0
        if (gamepad1.left_bumper || gamepad1.right_bumper || gamepad2.left_bumper || gamepad2.right_bumper) collectOtronSWITCHING =
            true else if (collectOtronSWITCHING) {
            collectOtronSWITCHING = false
            collectOtronACTIVE = !collectOtronACTIVE
        }
        if (gamepad1.left_bumper || gamepad2.left_bumper) collectOtronREVERSE =
            true else if (gamepad1.right_bumper || gamepad2.right_bumper) collectOtronREVERSE =
            false
        if (collectOtronACTIVE && !collectOtronREVERSE) {
            robot.collectOtron.power = 1.0
        } else if (collectOtronACTIVE) robot.collectOtron.power =
            -1.0 else robot.collectOtron.power =
            0.0

        // Collector lid
        if (gamepad1.b || gamepad2.b) robot.collectorGate.position = .25 else {
            robot.collectorGate.position = .68
        }
    }

    /**
     * FUNCTIONS------------------------------------------------------------------------------------------------------
     */
    fun DriveSideways(power: Double) {
        robot.driveSetPower(-power, power, power, -power)
    }

    fun PivotArmSetRotation(power: Double, degrees: Double) {
        val position = (degrees * PIVOTARM_CONSTANT).toInt()
        robot.pivotArm1.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        robot.pivotArm2.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        robot.pivotArm1.mode = DcMotor.RunMode.RUN_TO_POSITION
        robot.pivotArm2.mode = DcMotor.RunMode.RUN_TO_POSITION
        robot.pivotArm1.power = power
        robot.pivotArm2.power = power
        robot.pivotArm1.targetPosition = -position
        robot.pivotArm2.targetPosition = -position
    }

    companion object {
        const val PIVOTARM_CONSTANT =
            1440.0 * 10.0 / 360.0 * 0.6667 // Constant that converts pivot arm to degrees (1440*10/360 for Torquenado)
    }
}