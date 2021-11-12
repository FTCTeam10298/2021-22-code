package us.brainstormz.minibot

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.telemetryWizard.TelemetryConsole

class TeamScoringElementDetector(val tseThreshold : Int, private val console: TelemetryConsole) {
    companion object {
        val blue = Scalar(0.0, 0.0, 255.0)
        val red = Scalar(225.0, 0.0, 0.0)
        val black = Scalar(255.0, 255.0, 255.0)
        val TRANSPARENT = Scalar(0.0, 255.0, 0.0)
    }

    data class Region(val rect: Rect, var cb: Mat?, var analysis: Int)
    enum class TSEPosition {
        One, Two, Three
    }

    val region1PointA = Point(290.0, 197.0)
    val region1PointB = Point(325.0, 222.0)
    private val region1 = Region(Rect(region1PointA, region1PointB), null, 0)

    val region2PointA = Point(290.0, 197.0)
    val region2PointB = Point(325.0, 222.0)
    private val region2 = Region(Rect(region2PointA, region2PointB), null, 0)

    val region3PointA = Point(290.0, 197.0)
    val region3PointB = Point(325.0, 222.0)
    private val region3 = Region(Rect(region3PointA, region3PointB), null, 0)

    var yCrCb = Mat()
    var cb = Mat()

    @Volatile // Volatile since accessed by OpMode thread w/o synchronization
    var position = TSEPosition.One

    fun init(firstFrame: Mat) {
//        initializes cb and yCrCb
        inputToCb(firstFrame)

//        creates the rect
        region1.cb = cb.submat(region1.rect)
        region2.cb = cb.submat(region2.rect)
        region3.cb = cb.submat(region3.rect)
    }

    fun processFrame(input: Mat): Mat {
//        convert input to cb color format
        inputToCb(input)

//        makes a rect on the screen
        Imgproc.rectangle(input, region1.rect, blue, 2)
        Imgproc.rectangle(input, region2.rect, black, 2)
        Imgproc.rectangle(input, region2.rect, red, 2)

//        finds the color in the rect
        region1.analysis = colorInRect(region1.cb!!)
        region2.analysis = colorInRect(region2.cb!!)
        region3.analysis = colorInRect(region3.cb!!)

//        interpreting analysis
        position = when {
            region1.analysis > tseThreshold -> {
                TSEPosition.One
            }
            region2.analysis > tseThreshold -> {
                TSEPosition.Two
            }
            region3.analysis > tseThreshold -> {
                TSEPosition.Three
            }
            else -> {
                TSEPosition.Three
            }
        }
        console.display(8, "Position: $position")
        console.display(9, "Threshold: $tseThreshold")

//        gives image to the phone
        return input
    }

    private fun colorInRect(rect: Mat): Int {
        return Core.mean(rect).`val`[0].toInt()
    }

    /**
     * This function takes the RGB frame, converts to YCrCb,
     * and extracts the Cb channel to the cb variable
     */
    private fun inputToCb(input: Mat?) {
        Imgproc.cvtColor(input, yCrCb, Imgproc.COLOR_RGB2YCrCb)
        Core.extractChannel(yCrCb, cb, 1)
    }
}