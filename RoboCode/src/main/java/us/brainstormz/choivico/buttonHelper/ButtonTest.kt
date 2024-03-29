package us.brainstormz.choivico.buttonHelper

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.telemetryWizard.TelemetryConsole

//@TeleOp(name="Button Test", group="Tests")
class ButtonTest(): OpMode() {

    val console = TelemetryConsole(telemetry)

    override fun init() {

    }

    var toggleState = false
    override fun loop() {
        if (toggleState)
            console.display(1, "toggle")
        else
            console.display(1, "nope")

        if (gamepad1.x) // only fire event on button down
            toggleState = !toggleState //invert
    }
}
