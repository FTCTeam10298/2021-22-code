package us.brainstormz.localizer

import us.brainstormz.choivico.buttonHelper.ButtonHelper
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import org.opencv.imgproc.Imgproc
import us.brainstormz.choivico.robotCode.ChoiVicoHardware
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole


@Autonomous(name="Creamsicle Test and Calibration", group="Tests")
class LocalizerAutoAimTestAndCal : OpMode() {
    val console = TelemetryConsole(telemetry)
    val opencv = OpenCvAbstraction(this)
    val clutterDetector = ClutterDetector(console)
    val font = Imgproc.FONT_HERSHEY_COMPLEX
    val XbuttonHelper = ButtonHelper()
    val YbuttonHelper = ButtonHelper()
    val DpadHelper = ButtonHelper()
    val RbumperHelper = ButtonHelper()
    val LbumperHelper = ButtonHelper()
    val AbuttonHelper = ButtonHelper()

    override fun init() {
        opencv.start()
        opencv.onNewFrame(clutterDetector::scoopFrame)
    }

    private var varBeingEdited: ClutterDetector.NamedVar = ClutterDetector.goalColor
    fun render() {
        console.display(2, "Active Var; ${varBeingEdited.name}")
        console.display(4, "${varBeingEdited.value}")
    }

    override fun init_loop() {
        if (XbuttonHelper.stateChanged(gamepad1.x) && gamepad1.x) {
            console.display(3, "TrainerMODE; ${ClutterDetector.displayMode}")
            when (ClutterDetector.displayMode) {
                "frame" -> ClutterDetector.displayMode = "mask"
                "mask" -> ClutterDetector.displayMode = "kernel"
                "kernel" -> ClutterDetector.displayMode = "frame"
            }
            render()
        }


        when {
            DpadHelper.stateChanged(gamepad1.dpad_left) && gamepad1.dpad_left -> {
                when (varBeingEdited) {
                    ClutterDetector.goalColor.L_H -> varBeingEdited = ClutterDetector.goalColor.L_S
                    ClutterDetector.goalColor.L_S -> varBeingEdited = ClutterDetector.goalColor.L_V
                    ClutterDetector.goalColor.L_V -> varBeingEdited = ClutterDetector.goalColor.U_H
                    ClutterDetector.goalColor.U_H -> varBeingEdited = ClutterDetector.goalColor.U_S
                    ClutterDetector.goalColor.U_S -> varBeingEdited = ClutterDetector.goalColor.U_V
                    ClutterDetector.goalColor.U_V -> varBeingEdited = ClutterDetector.goalColor.L_H
                }
                render()
            }
        }

        if (YbuttonHelper.stateChanged(gamepad1.y) && gamepad1.y) {
            console.display(1, "Vals Zeroed")
            goalDetector.goalColor.L_H.value = 0.0
            goalDetector.goalColor.L_S.value = 0.0
            goalDetector.goalColor.L_V.value = 0.0
            goalDetector.goalColor.U_H.value = 0.0
            goalDetector.goalColor.U_S.value = 0.0
            goalDetector.goalColor.U_V.value = 0.0
            render()
        }

        if (RbumperHelper.stateChanged(gamepad1.right_bumper) && gamepad1.right_bumper) {
            varBeingEdited.value += 5
            render()
        }
        if (LbumperHelper.stateChanged(gamepad1.left_bumper) && gamepad1.left_bumper) {
            varBeingEdited.value -= 5
            render()
        }

        if (AbuttonHelper.stateChanged(gamepad1.a) && gamepad1.a) {
            goalDetector.goalColor.L_H.value = 0.0
            goalDetector.goalColor.L_S.value = 0.0
            goalDetector.goalColor.L_V.value = 0.0
            goalDetector.goalColor.U_H.value = 255.0
            goalDetector.goalColor.U_S.value = 255.0
            goalDetector.goalColor.U_V.value = 255.0
            console.display(1, "Vals Squonked")
            render()
        }
    }

    override fun loop() {
//        aimer.updateAimAndAdjustRobot()
    }
}

