package io.github.herrherklotz.chameleon.x.ev3


// Source: https://github.com/LLeddy/J4EV3/tree/master/J4EV3/src/com/j4ev3/core
object Ev3Motor {
    // motor ports
    private var PORT_A = 0x01.toByte()
    private var PORT_B = 0x02.toByte()
    private var PORT_C = 0x04.toByte()
    private var PORT_D = 0x08.toByte()
    private var PORT_ALL = 0x0F.toByte()


    fun setPolarity(ports: Byte, polarity: Int): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorSetPolarity(ports, polarity)
        return c
    }


    fun stepAtPower(ports: Byte, power: Int, rampUp: Int, continueRun: Int, rampDown: Int, brake:
    Boolean): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorStepAtPower(ports, power, rampUp, continueRun, rampDown, brake)
        c.motorStartMotor(ports)
        return c
    }


    fun stepAtSpeed(ports: Byte, speed: Int, rampUp: Int, continueRun: Int, rampDown: Int, brake:
    Boolean): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorStepAtSpeed(ports, speed, rampUp, continueRun, rampDown, brake)
        c.motorStartMotor(ports)
        return c
    }


    fun stepSync(ports: Byte, speed: Int, turn: Int, step: Int, brake: Boolean): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorStepSync(ports, speed, turn, step, brake)
        c.motorStartMotor(ports)
        return c
    }


    fun stopMotor(ports: Byte, brake: Boolean): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorStopMotor(ports, brake)
        return c
    }


    fun timeAtPower(ports: Byte, power: Int, msRampUp: Int, msContinueRun: Int, msRampDown: Int,
                    brake: Boolean): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorTimeAtPower(ports, power, msRampUp, msContinueRun, msRampDown, brake)
        c.motorStartMotor(ports)
        return c
    }


    fun timeAtSpeed(ports: Byte, speed: Int, msRampUp: Int, msContinueRun: Int, msRampDown: Int,
                    brake: Boolean): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorTimeAtSpeed(ports, speed, msRampUp, msContinueRun, msRampDown, brake)
        c.motorStartMotor(ports)
        return c
    }


    fun timeSync(ports: Byte, speed: Int, turn: Int, msTime: Int, brake: Boolean): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorTimeSync(ports, speed, turn, msTime, brake)
        c.motorStartMotor(ports)
        return c
    }


    fun turnAtPower(ports: Byte, power: Int): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorTurnAtPower(ports, power)
        c.motorStartMotor(ports)
        return c
    }


    fun turnAtSpeed(ports: Byte, speed: Int): Ev3Command {
        val c = Ev3Command(Code._DIRECT_COMMAND_NO_REPLY)
        c.motorTurnAtSpeed(ports, speed)
        c.motorStartMotor(ports)
        return c
    }
}