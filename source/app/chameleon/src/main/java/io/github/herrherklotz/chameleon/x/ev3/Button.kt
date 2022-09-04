package io.github.herrherklotz.chameleon.x.ev3

// Source: https://github.com/LLeddy/J4EV3/tree/master/J4EV3/src/com/j4ev3/core
object Button {
    private var NO_BUTTON = 0x00.toByte()
    private var UP_BUTTON = 0x01.toByte()
    private var ENTER_BUTTON = 0x02.toByte()
    private var DOWN_BUTTON = 0x03.toByte()
    private var RIGHT_BUTTON = 0x04.toByte()
    private var LEFT_BUTTON = 0x05.toByte()
    private var BACK_BUTTON = 0x06.toByte()
    private var ANY_BUTTON = 0x07.toByte()
    private var BUTTONTYPES = 0x08.toByte()


    fun buttonPressed(button: Int): Ev3Command? {
        if (button < 1 || 8 < button)
            return null

        val command = Ev3Command(Code._DIRECT_COMMAND_REPLY, 1, 0)
        command.buttonButtonPressed(button.toByte())
        return command
    }
}