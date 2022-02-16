package us.brainstormz.lankyKong

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.rataTony.RataTonyHardware

@Autonomous(name= "Lanky Kong Auto", group= "B")
class LankyKongAuto: LinearOpMode() {

    val hardware = LankyKongHardware()
    val opencv = OpenCvAbstraction(this)
    override fun runOpMode() {

        opencv.init(hardwareMap)
        opencv.cameraName = hardware.cameraName
        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_RIGHT
        opencv.start()
//        opencv.onNewFrame(tseDetector::processFrame)

        waitForStart()
    }

}