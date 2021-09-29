//package sdkAbstraction
//
//import com.qualcomm.robotcore.eventloop.opmode.OpMode
//import com.qualcomm.robotcore.hardware.Gamepad
//import kotlinx.coroutines.*
//import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryImpl
//
//abstract class SdkAbstractionContinous: OpMode(), SdkListener {
//
//    var telemetry = telemetry
//
//    override fun init(): Unit = runBlocking  {
//        onInitPressed()
//        launch { gamepadListners() }
//    }
//
//    override fun init_loop() {}
//
//    override fun start() {}
//
//    override fun loop() {
//        onStartPressed()
//    }
//
//    override fun stop() {
//        onStopPressed()
//    }
//
//    fun stopOpMode() {
//        requestOpModeStop()
//    }
//
////    GAMEPAD
//    private val gamePad1 = GamePadListener()
//    private val gamePad2 = GamePadListener()
//
//    override fun gamepadListners() {
//        while (true) {
//            gamePad1.update(gamepad1)
//            gamePad2.update(gamepad2)
//        }
//    }
//
//}
//
//
//abstract class SdkAbstractionLinear: OpMode() {
//
//    override fun init() {
//        onInit()
//    }
//
//    override fun init_loop() {}
//
//    override fun start(): Unit = runBlocking  {
//        launch { onStart() }
//    }
//
//    override fun loop() {}
//
//    override fun stop() {
//        onStop()
//    }
//
//    abstract fun onInit()
//    abstract fun onStart()
//    abstract fun onStop()
//}
//
//class Button {
//    private var value = false
//    fun update(newVal: Boolean) {
//        if (newVal && !value) {
//            value = true
//            pressed()
//        }
//        if (!newVal && value) {
//            value = false
//            released()
//        }
//    }
//
//    lateinit var pressed: () -> Unit
//    lateinit var released: () -> Unit
//}
//
//class GamePadListener {
//    val a = Button()
//
//    fun update(gamepad: Gamepad) {
//        a.update(gamepad.a)
//        TODO("Add all the buttons")
//    }
//
//}
//
//interface SdkListener {
//    fun gamepadListners()
//    fun onInitPressed()
//    fun onStartPressed()
//    fun onStopPressed()
//}