package us.brainstormz.rataTony

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.telemetryWizard.TelemetryConsole

class TeamScoringElementDetector(private val console: TelemetryConsole) {
    enum class TSEPosition {
        One, Two, Three
    }

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val red = Scalar(225.0, 0.0, 0.0)
    private val black = Scalar(255.0, 0.0, 255.0)
    private val transparent = Scalar(0.0, 255.0, 0.0)

    
    private val tseThreshold = 135

    private val regions = listOf(
        TSEPosition.One to Rect(Point(100.0, 240.0), Point(0.0, 100.0)),
        TSEPosition.Two to Rect(Point(210.0, 100.0), Point(110.0, 240.0)),
        TSEPosition.Three to Rect(Point(220.0, 100.0), Point(240.0, 240.0))
    )

    private val colors = listOf(
        TSEPosition.One to blue,
        TSEPosition.Two to black,
        TSEPosition.Three to red
    )

    @Volatile // Volatile since accessed by OpMode thread w/o synchronization
    var position = TSEPosition.One

    private lateinit var submats: List<Pair<TSEPosition, Mat>>
    private lateinit var cbFrame: Mat
    fun processFrame(frame: Mat): Mat {

        cbFrame = inputToCb(frame)

        submats = regions.map {
            it.first to cbFrame.submat(it.second)
        }

        var result = TSEPosition.Three
        var prevColor = 0

        var addedColor = 0.0
        val combinedThreshold = 140
        submats.forEach {
            val color = colorInRect(it.second)
            if (color > prevColor) {
                prevColor = color
                result = it.first
            }

            if (it.first == TSEPosition.Three) {
                if (addedColor < combinedThreshold) {
                    result = TSEPosition.Three
                }
            } else
                addedColor += color
        }
        console.display(6, "combined color: $addedColor")


        position = result



        submats.forEach {
            val color = if (it.first == result)
                red
            else
                blue

            val rect = regions.toMap()[it.first]
            Imgproc.rectangle(frame, rect, color, 2)
        }

        console.display(8, "Position: $position")
        console.display(9, "Highest Color: $prevColor")

        return frame
    }

    private fun colorInRect(rect: Mat): Int {
        return Core.mean(rect).`val`[0].toInt()
    }

    /**
     * This function takes the RGB frame, converts to YCrCb,
     * and extracts the Cb channel to the cb variable
     */
    private var yCrCb = Mat()
    private var cb = Mat()
    private fun inputToCb(input: Mat?): Mat {
        Imgproc.cvtColor(input, yCrCb, Imgproc.COLOR_RGB2YCrCb)
        Core.extractChannel(yCrCb, cb, 1)
        return cb
    }
}