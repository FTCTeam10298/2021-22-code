package us.brainstormz.minibot

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.telemetryWizard.TelemetryConsole

class TeamScoringElementDetector(private val console: TelemetryConsole) {
    enum class TSEPosition {
        One, Two, Three
    }

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val red = Scalar(225.0, 0.0, 0.0)
    private val black = Scalar(255.0, 255.0, 255.0)
    private val transparent = Scalar(0.0, 255.0, 0.0)

    
    private val tseThreshold = 135

    private val regions = listOf(
        TSEPosition.One to Rect(Point(290.0, 197.0), Point(325.0, 222.0)),
        TSEPosition.Two to Rect(Point(290.0, 197.0), Point(325.0, 222.0)),
        TSEPosition.Three to Rect(Point(290.0, 197.0), Point(325.0, 222.0))
    )

    private val colors = listOf(
        TSEPosition.One to blue,
        TSEPosition.Two to black,
        TSEPosition.Three to red
    )

    @Volatile // Volatile since accessed by OpMode thread w/o synchronization
    var position = TSEPosition.One

    fun processFrame(frame: Mat): Mat {
        val cbFrame = inputToCb(frame)

        val result = regions.firstOrNull {
            colorInRect(cbFrame.submat(it.second)) > tseThreshold
        }?.first

        position = result ?: TSEPosition.Three

        colors.forEach {
            val rect = regions.toMap()[it.first]
            Imgproc.rectangle(frame, rect, it.second, 2)
        }

        console.display(8, "Position: $position")
        console.display(9, "Threshold: $tseThreshold")

        return frame
    }

    private fun colorInRect(rect: Mat): Int {
        return Core.mean(rect).`val`[0].toInt()
    }

    /**
     * This function takes the RGB frame, converts to YCrCb,
     * and extracts the Cb channel to the cb variable
     */
    private fun inputToCb(input: Mat?): Mat {
        var yCrCb = Mat()
        Imgproc.cvtColor(input, yCrCb, Imgproc.COLOR_RGB2YCrCb)
        var cb: Mat? = null
        Core.extractChannel(yCrCb, cb, 1)
        return cb!!
    }
}