package us.brainstormz.roboticsClass

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import us.brainstormz.hardwareClasses.HardwareClass

@TeleOp
class GeneralDrive: OpMode() {

    val hardware = GeneralHardware()
    val movement = TankMove(hardware)

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun start() {
        movement.moveTime(2000, 0.5, 1.0)
    }

    override fun loop() {

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

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

        leftDrive = hwMap["left"] as DcMotor
        rightDrive = hwMap["right"] as DcMotor

        aux = hwMap["aux"] as DcMotor
    }

}
