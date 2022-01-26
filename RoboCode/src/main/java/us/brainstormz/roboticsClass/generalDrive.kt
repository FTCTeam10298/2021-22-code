package us.brainstormz.roboticsClass

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import us.brainstormz.hardwareClasses.HardwareClass
import us.brainstormz.hardwareClasses.SmartLynxModule

@TeleOp(name= "General Drive", group= "Tests")
class GeneralDrive: OpMode() {

    val hardware = GeneralHardware()
    val movement = TankMove(hardware)
    var direction = 1
    var prevButton = false

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun start() {
//        movement.moveTime(2000, 0.5, 1.0)
    }

    override fun loop() {

//        Direction switcher
        if (gamepad1.x && !prevButton) {
            prevButton = true
            direction *= -1
        } else if (!gamepad1.x)
            prevButton = false

//        Drive movement
        val leftPower = gamepad1.left_stick_y.toDouble()
        val rightPower = gamepad1.right_stick_y.toDouble()

        movement.movePower(leftPower * direction, rightPower * direction)

//        aux ctrl
        hardware.aux.power = gamepad1.right_trigger.toDouble() + -gamepad1.left_trigger.toDouble()
    }
}

class TankMove(private val hardware: GeneralHardware) {

    fun movePower(leftPower: Double, rightPower: Double) {
        hardware.leftDrive.power = leftPower
        hardware.rightDrive.power = rightPower
    }

    fun moveTime(ms: Int, leftPower: Double, rightPower: Double) {
        val targetTime = System.currentTimeMillis() + ms

        var currentTime = System.currentTimeMillis()
        while (currentTime < targetTime) {

            movePower(leftPower, rightPower)
            currentTime = System.currentTimeMillis()
        }
        movePower(0.0, 0.0)
    }
}


class GeneralHardware: HardwareClass {
    override lateinit var hwMap: HardwareMap

    lateinit var leftDrive: DcMotor
    lateinit var rightDrive: DcMotor

    lateinit var aux: DcMotor

    lateinit var allHubs: List<LynxModule>
    private lateinit var smartLynxModuleCtrl: SmartLynxModule

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

        leftDrive = hwMap["0"] as DcMotor
        rightDrive = hwMap["1"] as DcMotor

        aux = hwMap["3"] as DcMotor
//        allHubs = hwMap.getAll(LynxModule::class.java)
//        smartLynxModuleCtrl = SmartLynxModule(allHubs[0])
//
//        leftDrive = smartLynxModuleCtrl.getMotor(0) as DcMotor
//        rightDrive = smartLynxModuleCtrl.getMotor(1) as DcMotor
//
//        aux = smartLynxModuleCtrl.getMotor(3) as DcMotor
    }

}
