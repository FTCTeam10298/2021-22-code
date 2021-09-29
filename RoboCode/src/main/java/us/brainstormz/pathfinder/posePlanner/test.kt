package posePlanner

import locationTracking.PositionAndRotation
import java.awt.*
import java.lang.Thread.sleep
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random


fun wrapAngle(n: Double): Double =
    if (n < 0.0)
        n + 2* PI
    else
        n


fun main() {
//    val poly = Poly(PositionAndRotation(1.0, 1.0), PositionAndRotation(10.0, 10.0), PositionAndRotation(5.0,5.0))
//    println(poly.getLines())

//    val test = IntersectTest()
//
//    test.init(test)
//    test.runStuff()
//    test.post()

    val ultimateTest = UltimateTest()
    ultimateTest.init(ultimateTest)
    ultimateTest.runStuff()
    ultimateTest.findPath()
    ultimateTest.post()

//    val bezierTest = BezierTest()
//    bezierTest.init(bezierTest)
//    bezierTest.runstuff()
//    bezierTest.post()
}


class BezierTest: PaintComponent() {
    val obsGen = ObstructionGen(listOf())
//        BezierCurve(listOf(Point3D(20.0, 20.0, 0.0), Point3D(25.0, 30.0, 0.0), Point3D(30.0, 30.0, 0.0), Point3D(35.0, 20.0, 0.0), Point3D(40.0, 20.0, 0.0)))

    val scaling = 30

    fun generateCurve(curve: BezierCurve): List<Point3D> {
        var curvePoints = mutableListOf<Point3D>()

        for (i in (0..100000)) {
            val adjustedI = i * 0.00001
            curvePoints.add(curve.calculatePoint(adjustedI))
        }
        return curvePoints
    }

    fun runstuff() {
        val buttonsPanel = JPanel()

        val runButton = JButton("Run")

        buttonsPanel.add(runButton)
        testFrame.contentPane.add(buttonsPanel, BorderLayout.SOUTH)

        runButton.addActionListener {
            graphics.color = background
            graphics.clearRect(0, 0, width, height)
            paintComponent(graphics)
        }
    }

    override fun draw(g: Graphics) {
        val ctrls = mutableListOf<Point3D>()
        for (i in (1..4)) {
            ctrls.addAll(obsGen.randomObstruction().poly.points.map{ Point3D(it.x, it.y, it.r) })
        }

        val curve = BezierCurve(ctrls)

        val curvePoints = generateCurve(curve)

        val g2 = g as Graphics2D
        g2.stroke = BasicStroke(5f)

        g.color = Color.GREEN
        curvePoints.forEach { i ->
            val current = i * scaling.toDouble()
            val nextPoint = if (i != curvePoints.last()) {
                curvePoints[curvePoints.indexOf(i) + 1] * scaling.toDouble()
            }else {
                current
            }

            g.drawLine(
                current.x.toInt(),
                current.y.toInt(),
                nextPoint.x.toInt(),
                nextPoint.y.toInt()
            )
        }

        print("\n")
        g.color = Color.red
        curve.ctrlPoints.forEach { i ->
            val current = i * scaling.toDouble()

            g.fillOval((current.x).toInt(),
                (current.y).toInt(),
                5,
                5)
        }

    }
}

class IntersectTest: PaintComponent() {

    fun runStuff() {
        val buttonsPanel = JPanel()

        val runButton = JButton("Run")

        buttonsPanel.add(runButton)
        testFrame.contentPane.add(buttonsPanel, BorderLayout.SOUTH)

        runButton.addActionListener {
            graphics.color = background
            graphics.clearRect(0, 0, width, height)
            paintComponent(graphics)
        }
    }
    override fun draw(g: Graphics) {
        println("\nNew Run\n")
        val pose = PosePlanner()
        val obsGen = ObstructionGen(listOf())

        val line = Line(PositionAndRotation(0.0, 0.0), PositionAndRotation(30.0, 30.0))
        val obstructions = listOf(obsGen.randomObstruction(), obsGen.randomObstruction(), obsGen.randomObstruction(), obsGen.randomObstruction())
//            Obstruction(Poly(PositionAndRotation(10.0, 10.0),
//                                    PositionAndRotation(10.0, 20.0),
//                                    PositionAndRotation(20.0, 20.0),
//                                    PositionAndRotation(20.0, 10.0)))
        val result = obstructions.fold<Obstruction, Pair<Obstruction, Pair<PositionAndRotation, Line>>?>(null){ acc: Pair<Obstruction, Pair<PositionAndRotation, Line>>?, it ->
            val assd = it.poly.intersection(line)

            if (assd != null && ( acc == null || assd!!.first <= acc!!.second.first))
                it to assd!!
            else
                acc

        }

        print("\n")
        println("obs ${result?.first?.poly?.points}")
        println("intersect ${result?.second?.first}")
        println("edge ${result?.second?.second}")

        val scaling = 30.0


        obstructions.forEach {
            g.color = Color.red
            val obsPoints = it.poly.points
            val poly = Polygon(obsPoints.map{ (it.x * scaling).toInt() }.toIntArray(),
                obsPoints.map{ (it.y * scaling).toInt() }.toIntArray(),
                obsPoints.size
            )
            g.drawPolygon(poly)
        }


        val obs = result?.first ?: Obstruction()
        g.color = Color.blue
        val obsPoints = obs.poly.points
        val poly = Polygon(obsPoints.map{ (it.x * scaling).toInt() }.toIntArray(),
            obsPoints.map{ (it.y * scaling).toInt() }.toIntArray(),
            obsPoints.size
        )
        g.drawPolygon(poly)

        g.color = Color.BLUE
        val p1 = line.start * scaling
        val p2 = line.end * scaling
        g.drawLine(
            p1.x.toInt(),
            p1.y.toInt(),
            p2.x.toInt(),
            p2.y.toInt()
        )

        g.color = Color.black
        if (result != null){
            val current = result.second.first * scaling

            g.fillOval((current.x).toInt(),
                (current.y).toInt(),
                5,
                5)
        }
    }

}


class ObstructionGen(private var obstructions: List<Obstruction>) {

    private fun containsPoint(poly: Poly, p: PositionAndRotation): Boolean {
        val polygon = poly.points

        val centeroid = poly.centroid()

        val min = polygon.minBy { centeroid.distance(it) }!!
        val max = polygon.maxBy { centeroid.distance(it) }!!


        for (element in polygon) {
            val q = element
            min.x = min(q.x, min.x)
            max.x = max(q.x, max.x)
            min.y = min(q.y, min.y)
            max.y = max(q.y, max.y)
        }

        return !(p.x < min.x || p.x > max.x || p.y < min.y || p.y > max.y)
    }

    fun randomObstruction(): Obstruction {
        var points = mutableListOf<PositionAndRotation>()

        val start = PositionAndRotation(Random.nextDouble(6.0), Random.nextDouble(6.0))
        for (i in 0..Random.nextInt(3, 5)) {

            val new = PositionAndRotation(Random.nextDouble(4.0), Random.nextDouble(4.0))

            points.add(start + new)
        }

        val center = Poly(points[0], points[1], points[2], *points.subList(3, points.size).toTypedArray()).centroid()

        val validPoints = points.toMutableList()
        obstructions.forEach { obs ->
            points.forEach{
                if (containsPoint(obs.poly, it))
                    validPoints.remove(it)
            }
        }

        points = validPoints.sortedByDescending{ center.direction(it) }.toMutableList()

        when (points.size) {
            2 -> {
                points.add(randomObstruction().poly.points.first())
            }
            1 -> {
                val newPoints = randomObstruction().poly.points
                points.add(newPoints.first())
                points.add(newPoints.last())
            }
            0 -> {
                points.addAll(randomObstruction().poly.points)
            }
        }


        return Obstruction(Poly(points[0], points[1], points[2], *points.subList(3, points.size).toTypedArray()))
    }
}

class UltimateTest: PaintComponent() {

    private val posePlanner = PosePlanner()
    private val start = PositionAndRotation(0.0, 0.0, 0.0)
    private val end = PositionAndRotation(10.0, 10.0, 0.0)
    private var obstructions: List<Obstruction> =
//        listOf(Obstruction(Poly(PositionAndRotation(6.519797831033332, 7.727695766206712, 0.0), PositionAndRotation(3.808070865924204, 5.0588240052818865, 0.0), PositionAndRotation(4.055833477091567, 3.0009006746074265, 0.0))),
//               Obstruction(Poly(PositionAndRotation(2.895762025674081, 5.1004682898221, 0.0), PositionAndRotation(9.074475200744146, 5.2907985702886755, 0.0), PositionAndRotation(4.487713669422675, 3.6953173241440336, 0.0))),
//               Obstruction(Poly(PositionAndRotation(6.629677988177396, 9.479500916380966, 0.0), PositionAndRotation(8.302927274401915, 8.96710029016767, 0.0), PositionAndRotation(9.7812744203991, 7.538503451029122, 0.0))))
        listOf(Obstruction(Poly(PositionAndRotation(2.0, 3.0), PositionAndRotation(3.5, 3.0), PositionAndRotation(4.0, 8.0), PositionAndRotation(4.0, 6.0))),
                                                         Obstruction(Poly(PositionAndRotation(5.0, 4.0), PositionAndRotation(5.0, 2.0), PositionAndRotation(7.0, 2.0), PositionAndRotation(7.0, 4.0))),
                                                         Obstruction(Poly(PositionAndRotation(8.0, 7.0), PositionAndRotation(8.0, 9.0), PositionAndRotation(9.0, 9.0), PositionAndRotation(9.0, 7.0))))

    private val obsGen = ObstructionGen(obstructions)

    val scaling = 50.0

    private var points = listOf<PositionAndRotation>()
    private var neighbors = listOf<PositionAndRotation>()

    private var path = listOf<PositionAndRotation>()

    private fun runOnDedicatedThread(fn:()->Unit): Thread {

        val thread = object:Thread(){
            override fun run() {
                fn()
            }
        }
        thread.start()
        return thread
    }

    fun runStuff() {
        val buttonsPanel = JPanel()

        val runButton = JButton("Run")
        val printButton = JButton("Print")

        buttonsPanel.add(runButton)
        buttonsPanel.add(printButton)
        testFrame.contentPane.add(buttonsPanel, BorderLayout.SOUTH)

        runButton.addActionListener {
            points = listOf()
            path = listOf()
            obstructions = listOf(obsGen.randomObstruction(), obsGen.randomObstruction(), obsGen.randomObstruction())
            findPath()
            graphics.color = background
            graphics.clearRect(0, 0, width, height)
            paintComponent(graphics)
        }
        printButton.addActionListener {
            val printable = obstructions.fold("listOf("){ acc, it ->
                acc + it.codeString()
            }.toString().dropLast(2).plus(")")

            println(printable)
        }
    }

    fun findPath()  {

        val thread = runOnDedicatedThread {
            posePlanner.obstructions = obstructions
            path = posePlanner.generatePath(start, end)
            println("Planner finished")
        }
        println(path)
        sleep(100)
        thread.stop()
        println("\n \nPath Stopped")

        points = posePlanner.aStar.allPoints
        neighbors = posePlanner.hitPoints
    }

    override fun draw(g: Graphics) {

//        g.color = Color.green
//        g.drawLine(start.x.toInt(), start.y.toInt(), end.x.toInt() * scaling.toInt(), end.y.toInt() * scaling.toInt())

        g.color = Color.red
        obstructions.forEach { obstruction ->
            val obs = obstruction.poly.points

            val poly = Polygon(obs.map{ (it.x * scaling).toInt() }.toIntArray(),
                               obs.map{ (it.y * scaling).toInt() }.toIntArray(),
                               obs.size)

            g.drawPolygon(poly)
        }


        g.color = Color.blue
        neighbors.forEach {
            val current = it * scaling

            g.fillOval((current.x).toInt(),
                (current.y).toInt(),
                5,
                5)
        }

//        println(path.size)
//        println(path)
        path.forEach {
            val current = it * scaling

            g.color = Color.black

            g.fillOval(current.x.toInt(),
                       current.y.toInt(),
                       7,
                       7)

            g.color = Color.BLUE
            if (it != path.last()) {
                val nextPoint = path[path.indexOf(it) + 1] * scaling

//                println("current= $current")
//                println("next= $nextPoint")
                g.drawLine(
                    current.x.toInt(),
                    current.y.toInt(),
                    nextPoint.x.toInt(),
                    nextPoint.y.toInt()
                )
            }
        }


        g.color = Color.black
        val hitPoint = start * scaling

        g.fillOval((hitPoint.x).toInt(),
                   (hitPoint.y).toInt(),
                   (5).toInt(),
                   (5).toInt())

        g.color = Color.black
        val adjust = end * scaling

        g.fillOval((adjust.x).toInt(),
                    (adjust.y).toInt(),
                    (5).toInt(),
                    (5).toInt())

//        for (i in 0..5) {
//
//            val it = neighbors[i]
//
//
//            g.color = Color.blue
//            it.second.forEach {
//                val current = it * scaling
//
//                g.fillOval((current.x).toInt(),
//                    (current.y).toInt(),
//                    (5+i*1.5).toInt(),
//                    (5+i*1.5).toInt())
//            }
//
//            g.color = Color.black
//            val hitPoint = it.first * scaling
//
//            g.fillOval((hitPoint.x).toInt(),
//                       (hitPoint.y).toInt(),
//                       (5+i*1.5).toInt(),
//                       (5+i*1.5).toInt())
//
//        }

        g.color = Color.pink
        points.forEach {
            val current = it * scaling

            g.fillOval((current.x).toInt(),
                (current.y).toInt(),
                5,
                5)
        }

    }


}

abstract class PaintComponent : JComponent() {
    val testFrame = JFrame()

    abstract fun draw(g: Graphics)

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        draw(g)
    }

    fun init(comp: PaintComponent) {
        println("Init")
        testFrame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        comp.preferredSize = Dimension(600, 600)
        testFrame.contentPane.add(comp, BorderLayout.CENTER)
    }

    fun post() {
        testFrame.pack()
        testFrame.isVisible = true
        println("Show Frame")
    }
}



class BezierCurveTest(private val comp: PaintComponent) {
//    fun main() {
////        val bezierCurve = interpolate(listOf(Point3D(0.0, 0.0, 0.0),
////                                             Point3D(2.7757000000000005, 7.182100000000002, 2.7757000000000005),
////                                             Point3D(4.163200000000002, 10.665600000000001, 4.163200000000002),
////                                             Point3D(5.2477, 11.766099999999998, 5.2477),
////                                             Point3D(6.1072000000000015, 10.945599999999999, 6.1072000000000015),
////                                             Point3D(6.8125, 8.8125, 6.8125),
////                                             Point3D(7.427199999999999, 6.121600000000001, 7.427199999999999),
////                                             Point3D(8.0077, 3.7741000000000002, 8.0077),
////                                             Point3D(8.6032, 2.8175999999999988, 8.6032),
////                                             Point3D(9.255700000000001, 4.446099999999998, 9.255700000000001),
////                                             Point3D(10.0, 9.999999999999993, 10.0)),
////                                             10.0)
//        val bezierCurve = BezierCurve(listOf(Point3D(1.0, 0.0, 0.0),
//                                             Point3D(6.0, 21.0, 0.0),
//                                             Point3D(7.0, 1.0, 0.0),
//                                             Point3D(8.0, 5.0, 0.0),
//                                             Point3D(10.0, 11.0, 0.0)))
//
//        var pointList = listOf(Point3D())
//        val step = 0.1
//
//        var t = 0.1
//        while (t < 1) {
//
//            pointList += bezierCurve.calculatePoint(t)
//
//            t += step
//        }
//
//
//
//        var prevPoint = Point3D()
//
//        pointList.forEach {
//            val current = it * 40.0
//
//            comp.addLine((current.x).toInt(),
//                         (current.y).toInt(),
//                         (prevPoint.x).toInt(),
//                         (prevPoint.y).toInt())
//
//            prevPoint = current
//        }
//
//        val asdf = Point3D(2.0, 0.5, 3.0) * 20.0
//        println("$asdf")
////        println(pointList)
//    }
}
