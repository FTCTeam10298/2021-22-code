package us.brainstormz.rattatoni

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
//        TSEPosition.One to Rect(Point(300.0, 197.0), Point(335.0, 222.0)),
//        TSEPosition.Two to Rect(Point(200.0, 197.0), Point(235.0, 222.0)),
//        TSEPosition.Three to Rect(Point(100.0, 197.0), Point(125.0, 222.0))
        TSEPosition.One to Rect(Point(100.0, 240.0), Point(0.0, 100.0)),
        TSEPosition.Two to Rect(Point(210.0, 100.0), Point(110.0, 240.0)),
        TSEPosition.Three to Rect(Point(220.0, 100.0), Point(240.0, 240.0))
//        TSEPosition.One to Rect(Point(1000.0, 197.0), Point(1135.0, 222.0)),
//        TSEPosition.Two to Rect(Point(200.0, 197.0), Point(235.0, 222.0)),
//        TSEPosition.Three to Rect(Point(0.0, 0.0), Point(0.0, 0.0))
    )

    private lateinit var submats: List<Pair<TSEPosition, Mat>>

    private val colors = listOf(
        TSEPosition.One to blue,
        TSEPosition.Two to black,
        TSEPosition.Three to red
    )

//    @Volatile // Volatile since accessed by OpMode thread w/o synchronization
    var position = TSEPosition.One

    fun init(frame: Mat): Mat {
        val cbFrame = inputToCb(frame)

        submats = regions.map {
            it.first to cbFrame.submat(it.second)
        }

        return frame
    }

    fun processFrame(frame: Mat): Mat {

        var result = TSEPosition.Three
        var prevColor = 0
        submats.forEach {
            val color = colorInRect(it.second)
            if (color > prevColor) {
                prevColor = color
                result = it.first
            }
        }

        position = result

        colors.forEach {
            val rect = regions.toMap()[it.first]
            Imgproc.rectangle(frame, rect, it.second, 2)
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
    private fun inputToCb(input: Mat?): Mat {
        var yCrCb = Mat()
        Imgproc.cvtColor(input, yCrCb, Imgproc.COLOR_RGB2YCrCb)
        var cb = Mat()
        Core.extractChannel(yCrCb, cb, 1)
        return cb
    }
}