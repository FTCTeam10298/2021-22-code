package us.brainstormz.localizer

import us.brainstormz.choivico.buttonHelper.ButtonHelper
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import org.opencv.imgproc.Imgproc
import us.brainstormz.telemetryWizard.TelemetryConsole


@Autonomous(name="Color Test and Calibration", group="Tests")
class LocalizerAutoAimTestAndCal : OpMode() {
    val console = TelemetryConsole(telemetry)
    val opencv = OpenCvAbstraction(this)
    val hubDetector = AllianceHubDetector(console)
    val font = Imgproc.FONT_HERSHEY_COMPLEX
    val XbuttonHelper = ButtonHelper()
    val YbuttonHelper = ButtonHelper()
    val DpadHelper = ButtonHelper()
    val RbumperHelper = ButtonHelper()
    val LbumperHelper = ButtonHelper()
    val AbuttonHelper = ButtonHelper()

    override fun init() {
        opencv.init(hardwareMap)
        opencv.start()
        opencv.onNewFrame(hubDetector::processFrame)
    }

    private var varBeingEdited: AllianceHubDetector.NamedVar = hubDetector.goalColor.U_H
    fun render() {
        console.display(2, "Active Var; ${varBeingEdited.name}")
        console.display(4, "${varBeingEdited.value}")
    }

    override fun init_loop() {
     if (XbuttonHelper.stateChanged(gamepad1.x) && gamepad1.x) {
            console.display(3, "TrainerMODE; ${hubDetector.displayMode}")
            when (hubDetector.displayMode) {
                "frame" -> hubDetector.displayMode = "mask"
                "mask" -> hubDetector.displayMode = "kernel"
                "kernel" -> hubDetector.displayMode = "frame"
            }
            render()
        }


        when {
            DpadHelper.stateChanged(gamepad1.dpad_left) && gamepad1.dpad_left -> {
                when (varBeingEdited) {
                    hubDetector.goalColor.L_H -> varBeingEdited = hubDetector.goalColor.L_S
                    hubDetector.goalColor.L_S -> varBeingEdited = hubDetector.goalColor.L_V
                    hubDetector.goalColor.L_V -> varBeingEdited = hubDetector.goalColor.U_H
                    hubDetector.goalColor.U_H -> varBeingEdited = hubDetector.goalColor.U_S
                    hubDetector.goalColor.U_S -> varBeingEdited = hubDetector.goalColor.U_V
                    hubDetector.goalColor.U_V -> varBeingEdited = hubDetector.goalColor.L_H
                }
                render()
            }
        }

        if (YbuttonHelper.stateChanged(gamepad1.y) && gamepad1.y) {
            console.display(1, "Vals Zeroed")
            hubDetector.goalColor.L_H.value = 0.0
            hubDetector.goalColor.L_S.value = 0.0
            hubDetector.goalColor.L_V.value = 0.0
            hubDetector.goalColor.U_H.value = 0.0
            hubDetector.goalColor.U_S.value = 0.0
            hubDetector.goalColor.U_V.value = 0.0
            render()
        }
        if (YbuttonHelper.stateChanged(gamepad1.y) && gamepad1.y) {
            console.display(1, "Vals Recalled")
            hubDetector.goalColor.L_H.value = 5.0
            hubDetector.goalColor.L_S.value = 10.0
            hubDetector.goalColor.L_V.value = 110.0
            hubDetector.goalColor.U_H.value = 95.0
            hubDetector.goalColor.U_S.value = 200.0
            hubDetector.goalColor.U_V.value = 255.0
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
            hubDetector.goalColor.L_H.value = 0.0
            hubDetector.goalColor.L_S.value = 0.0
            hubDetector.goalColor.L_V.value = 0.0
            hubDetector.goalColor.U_H.value = 255.0
            hubDetector.goalColor.U_S.value = 255.0
            hubDetector.goalColor.U_V.value = 255.0
            console.display(1, "Vals Squonked")
            render()
        }
    }

    override fun loop() {
//        aimer.updateAimAndAdjustRobot()
    }
}

