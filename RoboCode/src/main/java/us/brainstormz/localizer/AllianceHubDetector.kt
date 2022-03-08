package us.brainstormz.localizer

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.random.Random

class AllianceHubDetector(private val console: TelemetryConsole){

    private val font = Imgproc.FONT_HERSHEY_COMPLEX

    public var displayMode: String = "frame"

    class NamedVar(val name: String, var value: Double)

    class ColorRange(val L_H: NamedVar, val L_S: NamedVar, val L_V: NamedVar, val U_H: NamedVar, val U_S: NamedVar, val U_V: NamedVar)

    val goalColor = ColorRange(
        L_H = NamedVar("Low Hue", 120.0),
        L_S = NamedVar("Low Saturation", 115.0),
        L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 120.0),
        U_H = NamedVar("Uppper Hue", 255.0),
        U_S = NamedVar("Upper Saturation", 255.0),
        U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0))

    var x = 0.0
    var y = 0.0

    private val hsv = Mat()
    private val maskA = Mat()
    private val maskB = Mat()
    private val kernel = Mat(5, 5, CvType.CV_8U)

    private val edges = Mat()

    fun processFrame(frame: Mat): Mat {
        Imgproc.Canny(frame, edges, 100.0, 200.0)
        val contours:MutableList<MatOfPoint> = mutableListOf<MatOfPoint>()
        Imgproc.findContours(edges, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

//        fun convert(matOfPoint2f: MatOfPoint2f): MatOfPoint {
//            val foo = MatOfPoint()
//            matOfPoint2f.convertTo(foo, CvType.CV_32S)
//            return foo
//        }
        frame.setTo(Scalar(255.0, 255.0, 255.0))
//Stu's Questions: How many contours detected --> hub? Of contours, how differ from everything & how? Random colors?

        data class ShapeStuff(
            val area:Double,
            val aspect:Double,
            val points:Array<Point>)

        fun computeShape(contour:MatOfPoint):ShapeStuff {
            val pointsArray = contour.toArray()

            val xValues = pointsArray.map { it.x }
            val yValues = pointsArray.map { it.y }

            val minX = xValues.minOrNull()!!
            val minY = yValues.minOrNull()!!

            val maxX = xValues.maxOrNull()!!
            val maxY = yValues.maxOrNull()!!

            val width = maxX - minX
            val height = maxY - minY
            val area = width * height
            val aspect = width / height


            return ShapeStuff(
                area = area,
                aspect = aspect,
                points = pointsArray
            )
        }

        var cntIndex = 0
        val countoursToDraw = contours.filter{contour ->
            val shape = computeShape(contour)

            shape.area > 20 && shape.points.size > 3 && shape.aspect < 1.3
        }
        println("we're finding ${countoursToDraw.size} contours")
        countoursToDraw.forEachIndexed { index, cnt ->
            val shape = computeShape(cnt)
            println("Item $index is: ${shape}")
            val point = shape.points.first()
//
            Imgproc.drawContours(frame, contours, cntIndex, Scalar(Random.nextDouble(0.0, 255.0), Random.nextDouble(0.0, 255.0), Random.nextDouble(0.0, 255.0)), 3)


            Imgproc.putText(frame, "${index}", point, font, .25, Scalar(22.0, 100.0, 100.0))

            cntIndex++
        }
//        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV)
//
//        val lower = Scalar(goalColor.L_H.value, goalColor.L_S.value, goalColor.L_V.value)
//        val upper = Scalar(goalColor.U_H.value, goalColor.U_S.value, goalColor.U_V.value)
//        Core.inRange(hsv, lower, upper, maskA)
//
//        Imgproc.erode(maskA, maskB, kernel)
//
//        val contours = mutableListOf<MatOfPoint>()
//        Imgproc.findContours(maskB, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
//
//        //  for cnt in contours:
//        contours.forEach { cnt ->
//
//            val area = Imgproc.contourArea(cnt)
//
//            val points = MatOfPoint2f()
//
//
//            fun convert(src: MatOfPoint): MatOfPoint2f {
//                val dst = MatOfPoint2f()
//                src.convertTo(dst, CvType.CV_32F)
//                return dst
//            }
//
//            val cnt2f = convert(cnt)
//            Imgproc.approxPolyDP(cnt2f, points, 0.02 * Imgproc.arcLength(cnt2f, true), true)
//
//            val point = points.toList()[0]
//
//            if (area > 400) {
//
//                fun convert(matOfPoint2f: MatOfPoint2f): MatOfPoint {
//                    val foo = MatOfPoint()
//                    matOfPoint2f.convertTo(foo, CvType.CV_32S)
//                    return foo
//                }
//
//                Imgproc.drawContours(frame, mutableListOf(convert(points)), 0, Scalar(0.0, 0.0, 0.0), 5)
//                if (points.toArray().size > 2){
//                    console.display(14, "${points.toArray().size}")
//                }
//                when (points.toArray().size) {
//                    3 -> {
//                        Imgproc.putText(frame, "triangle", Point(point.x, point.y), font, 1.0, Scalar(22.0, 100.0, 100.0))
//                    }
//                    4 -> {
//                        Imgproc.putText(frame, "rectangle", Point(point.x, point.y), font, 1.0, Scalar(22.0, 100.0, 100.0))
//                    }
//                    in 11..19 -> {
//                        Imgproc.putText(frame, "circle", Point(point.x, point.y), font, 1.0, Scalar(22.0, 100.0, 100.0))
//                    }
//                    6-> {
//                        Imgproc.putText(frame, "goalCandidate", Point(point.x, point.y), font, 0.05, Scalar(22.0, 100.0, 100.0))
//
//                        val pointsArray = points.toArray()
//
//                        val xValues = pointsArray.map{it.x}
//                        val yValues = pointsArray.map{it.y}
//
//                        val minX = xValues.minOrNull()!!
//                        val minY = yValues.minOrNull()!!
//
//                        val maxX = xValues.maxOrNull()!!
//                        val maxY = yValues.maxOrNull()!!
//
//                        val width = maxX - minX
//                        val height = maxY - minY
//                        val area = width * height
//                        val aspect = width / height
//
//                        if (aspect > 1.3) {
//                            x = point.x
//                            y = point.y
//                            Imgproc.putText(frame, "goal", Point(point.x, point.y), font, 1.5, Scalar(22.0, 100.0, 100.0))
//                        }
//
//
//
//
//                        console.display(5, "width $width")
//                        console.display(6, "Last known goal position: $x, $y")
//                        console.display(7, "My God, THE FALSE POSITIVES are filled with stars!: $height")
//                        console.display(8, "there can only be one: $area")
//                        console.display(9, "Aspects are bright: $aspect")
//
//
//                    }
//                }
//            }
//        }
//        console.display(1,"Still alive")
//        return when (displayMode) {
//            "frame" -> frame
//            "kernel" -> kernel
//            "mask" -> maskB
//            else -> frame
//        }
        return frame
    }
}