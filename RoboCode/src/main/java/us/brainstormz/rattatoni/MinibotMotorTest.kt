package us.brainstormz.rattatoni

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.rattatoni.TeamScoringElementDetector.TSEPosition

@Autonomous(name="Minibot Motor Test", group="Minibot")
class MinibotMotorTest: LinearOpMode() {

    val console = TelemetryConsole(telemetry)

    val hardware = MinibotHardware()
    val movement = EncoderDriveMovement(hardware, console)
    val depositor = Depositor(hardware, telemetry)

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        console.display(1, "Initialization Complete")
        waitForStart()
        /** AUTONOMOUS  PHASE */

        console.display(1, "left Front")
        hardware.lFDrive.power = 0.3
        for (i in 0 until 500) {
            console.display(5, "lFDrive: ${hardware.lFDrive.currentPosition}")
            console.display(6, "rFDrive: ${hardware.rFDrive.currentPosition}")
            console.display(7, "lBDrive: ${hardware.lBDrive.currentPosition}")
            console.display(8, "rBDrive: ${hardware.rBDrive.currentPosition}")
            sleep(10)
        }
        sleep(1000)

        console.display(2, "right Front")
        hardware.rFDrive.power = 0.3
        for (i in 0 until 500) {
            console.display(5, "lFDrive: ${hardware.lFDrive.currentPosition}")
            console.display(6, "rFDrive: ${hardware.rFDrive.currentPosition}")
            console.display(7, "lBDrive: ${hardware.lBDrive.currentPosition}")
            console.display(8, "rBDrive: ${hardware.rBDrive.currentPosition}")
            sleep(10)
        }
        sleep(1000)

        console.display(3, "left back")
        hardware.lBDrive.power = 0.3
        for (i in 0 until 500) {
            console.display(5, "lFDrive: ${hardware.lFDrive.currentPosition}")
            console.display(6, "rFDrive: ${hardware.rFDrive.currentPosition}")
            console.display(7, "lBDrive: ${hardware.lBDrive.currentPosition}")
            console.display(8, "rBDrive: ${hardware.rBDrive.currentPosition}")
            sleep(10)
        }
        sleep(1000)

        console.display(4, "right Back")
        hardware.rBDrive.power = 0.3
        for (i in 0 until 500) {
            console.display(5, "lFDrive: ${hardware.lFDrive.currentPosition}")
            console.display(6, "rFDrive: ${hardware.rFDrive.currentPosition}")
            console.display(7, "lBDrive: ${hardware.lBDrive.currentPosition}")
            console.display(8, "rBDrive: ${hardware.rBDrive.currentPosition}")
            sleep(10)
        }
        sleep(1000)

    }
}