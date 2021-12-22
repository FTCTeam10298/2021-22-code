package us.brainstormz.localizer

import us.brainstormz.choivico.buttonHelper.ButtonHelper
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import org.opencv.imgproc.Imgproc
import us.brainstormz.choivico.robotCode.ChoiVicoHardware
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole


@Autonomous(name="Color Test and Calibration", group="Tests")
class LocalizerAutoAimTestAndCal : OpMode() {
    val console = TelemetryConsole(telemetry)
    val opencv = OpenCvAbstraction(this)
    val clutterAvatar = clutterAvatar(console)
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
        opencv.onNewFrame(clutterAvatar::scoopFrame)
    }

    private var varBeingEdited: clutterAvatar.NamedVar = clutterAvatar.goalColor.U_H
    fun render() {
        console.display(2, "Active Var; ${varBeingEdited.name}")
        console.display(4, "${varBeingEdited.value}")
    }

    override fun init_loop() {
     if (XbuttonHelper.stateChanged(gamepad1.x) && gamepad1.x) {
            console.display(3, "TrainerMODE; ${clutterAvatar.displayMode}")
            when (clutterAvatar.displayMode) {
                "frame" -> clutterAvatar.displayMode = "mask"
                "mask" -> clutterAvatar.displayMode = "kernel"
                "kernel" -> clutterAvatar.displayMode = "frame"
            }
            render()
        }


        when {
            DpadHelper.stateChanged(gamepad1.dpad_left) && gamepad1.dpad_left -> {
                when (varBeingEdited) {
                    clutterAvatar.goalColor.L_H -> varBeingEdited = clutterAvatar.goalColor.L_S
                    clutterAvatar.goalColor.L_S -> varBeingEdited = clutterAvatar.goalColor.L_V
                    clutterAvatar.goalColor.L_V -> varBeingEdited = clutterAvatar.goalColor.U_H
                    clutterAvatar.goalColor.U_H -> varBeingEdited = clutterAvatar.goalColor.U_S
                    clutterAvatar.goalColor.U_S -> varBeingEdited = clutterAvatar.goalColor.U_V
                    clutterAvatar.goalColor.U_V -> varBeingEdited = clutterAvatar.goalColor.L_H
                }
                render()
            }
        }

        if (YbuttonHelper.stateChanged(gamepad1.y) && gamepad1.y) {
            console.display(1, "Vals Zeroed")
            clutterAvatar.goalColor.L_H.value = 0.0
            clutterAvatar.goalColor.L_S.value = 0.0
            clutterAvatar.goalColor.L_V.value = 0.0
            clutterAvatar.goalColor.U_H.value = 0.0
            clutterAvatar.goalColor.U_S.value = 0.0
            clutterAvatar.goalColor.U_V.value = 0.0
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
            clutterAvatar.goalColor.L_H.value = 0.0
            clutterAvatar.goalColor.L_S.value = 0.0
            clutterAvatar.goalColor.L_V.value = 0.0
            clutterAvatar.goalColor.U_H.value = 255.0
            clutterAvatar.goalColor.U_S.value = 255.0
            clutterAvatar.goalColor.U_V.value = 255.0
            console.display(1, "Vals Squonked")
            render()
        }
    }

    override fun loop() {
//        aimer.updateAimAndAdjustRobot()
    }
}

