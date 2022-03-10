package us.brainstormz.rataTony

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvCameraRotation
import posePlanner.BezierCurve
import posePlanner.Point2D
import posePlanner.Point3D
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.lankyKong.LankyKongHardware
import us.brainstormz.openCvAbstraction.OpenCvAbstraction

object DriveCurves {
    var ctrlPoint = Point2D( 0.4, 0.6)
    val curve = BezierCurve(listOf(Point2D(1.0, 1.0), ctrlPoint, Point2D(0.0, 0.0)))
}

@TeleOp(name="DriveTuner", group="B")
class DriveTuner: OpMode() {

    val hardware = RataTonyHardware()
    val robot = MecanumDriveTrain(hardware)

    val opencv = OpenCvAbstraction(this)

    override fun init() {
        hardware.init(hardwareMap)

        opencv.init(hardwareMap)
        opencv.cameraName = hardware.cameraName
        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_RIGHT
        opencv.start()
        opencv.onNewFrame(::processFrame)
    }

    override fun loop() {
        val increment = 1
        val point = DriveCurves.ctrlPoint
        DriveCurves.ctrlPoint = when {
            gamepad1.dpad_up -> Point2D(point.x, point.y + increment)
            gamepad1.dpad_down -> Point2D(point.x, point.y + increment)
            gamepad1.dpad_right -> Point2D(point.x, point.y + increment)
            gamepad1.dpad_left -> Point2D(point.x, point.y + increment)
            else -> point
        }

        val yInput = -gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = -gamepad1.right_stick_x.toDouble()

        val ySign = posOrNeg(yInput)
        val xSign = posOrNeg(xInput)
        val rSign = posOrNeg(rInput)

        val y = DriveCurves.curve.calculatePoint(yInput).x * ySign
        val x = DriveCurves.curve.calculatePoint(xInput).x * xSign
        val r = DriveCurves.curve.calculatePoint(rInput).x * rSign

        robot.driveSetPower(
            (y + x - r),
            (y - x + r),
            (y - x - r),
            (y + x + r)
        )
    }

    fun processFrame(frame: Mat): Mat {
        val scale = 1.0
        Imgproc.rectangle(frame, Rect(Point(0.0, 0.0), frame.size()), Scalar(255.0, 0.0, 255.0), Imgproc.FILLED)

        val curve = visualizeCurve(DriveCurves.curve)
        curve.forEach {
            val current = it * scale
            val nextPoint = if (it != curve.last()) {
                curve[curve.indexOf(it) + 1] * scale
            }else {
                current
            }

            Imgproc.line(frame, Point(current.x, current.y), Point(nextPoint.x, nextPoint.y), Scalar(225.0, 0.0, 0.0))
        }

        return frame
    }

    fun visualizeCurve(curve: BezierCurve): List<Point2D> {
        val precision = 50
        val curvePoints = mutableListOf<Point2D>()

        for (i in (0..precision)) {
            val adjustedI = i * precision / (precision.toDouble() * precision.toDouble())
            curvePoints.add(curve.calculatePoint(adjustedI).toPoint2D())
        }
        return curvePoints
    }


    private fun posOrNeg(num: Double): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }
}


//class BezierTest: PaintComponent() {
//    val obsGen = ObstructionGen(listOf())
////        BezierCurve(listOf(Point3D(20.0, 20.0, 0.0), Point3D(25.0, 30.0, 0.0), Point3D(30.0, 30.0, 0.0), Point3D(35.0, 20.0, 0.0), Point3D(40.0, 20.0, 0.0)))
//
//    val scaling = 30
//
//    fun generateCurve(curve: BezierCurve): List<Point3D> {
//        var curvePoints = mutableListOf<Point3D>()
//
//        for (i in (0..100000)) {
//            val adjustedI = i * 0.00001
//            curvePoints.add(curve.calculatePoint(adjustedI))
//        }
//        return curvePoints
//    }
//
//    fun runstuff() {
//        val buttonsPanel = JPanel()
//
//        val runButton = JButton("Run")
//
//        buttonsPanel.add(runButton)
//        testFrame.contentPane.add(buttonsPanel, BorderLayout.SOUTH)
//
//        runButton.addActionListener {
//            graphics.color = background
//            graphics.clearRect(0, 0, width, height)
//            paintComponent(graphics)
//        }
//    }
//
//    override fun draw(g: Graphics) {
//        val ctrls = mutableListOf<Point3D>()
//        for (i in (1..4)) {
//            ctrls.addAll(obsGen.randomObstruction().poly.points.map{ Point3D(it.x, it.y, it.r) })
//        }
//
//        val curve = BezierCurve(ctrls)
//
//        val curvePoints = generateCurve(curve)
//
//        val g2 = g as Graphics2D
//        g2.stroke = BasicStroke(5f)
//
//        g.color = Color.GREEN
//        curvePoints.forEach { i ->
//            val current = i * scaling.toDouble()
//            val nextPoint = if (i != curvePoints.last()) {
//                curvePoints[curvePoints.indexOf(i) + 1] * scaling.toDouble()
//            }else {
//                current
//            }
//
//            g.drawLine(
//                current.x.toInt(),
//                current.y.toInt(),
//                nextPoint.x.toInt(),
//                nextPoint.y.toInt()
//            )
//        }
//
//        print("\n")
//        g.color = Color.red
//        curve.ctrlPoints.forEach { i ->
//            val current = i * scaling.toDouble()
//
//            g.fillOval((current.x).toInt(),
//                (current.y).toInt(),
//                5,
//                5)
//        }
//
//    }
//}