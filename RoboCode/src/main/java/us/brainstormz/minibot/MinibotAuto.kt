package us.brainstormz.minibot

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.choivico.ringDetector.RingDetector
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard

@TeleOp(name="Minibot Auto", group="Minibot")
class MinibotAuto: LinearOpMode() {

    val hardware = MinibotHardware()
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))
    val depositor = Depositor(hardware, telemetry)

    val wizard = TelemetryWizard(TelemetryConsole(telemetry), this)

    val opencv = OpenCvAbstraction(this)
    val tseDetector = TeamScoringElementDetector(135, console)
    var tsePosition = TSEPosition.One

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */
//        movement.driveRobotPosition(power = 1.0, inches = 20.0, smartAccel = true)
//        movement.driveRobotStrafe(power = 1.0, inches = 20.0, smartAccel = true)
//        movement.driveRobotTurn(power = 1.0, degree = 20.0, smartAccel = true)
    }
}