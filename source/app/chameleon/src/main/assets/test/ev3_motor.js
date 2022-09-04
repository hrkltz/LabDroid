$sleep(2000)

const motor = {
    "function": "turnAtPower",
    "port": 0x01,
    "power": 25
}

$out(0, motor)
$sleep(1000)

motor.port = 0x02
$out(0, motor)
$sleep(1000)

motor.port = 0x04
$out(0, motor)
$sleep(1000)

motor.port = 0x08
$out(0, motor)
$sleep(1000)

motor.port = 0x01|0x02|0x04|0x08
motor.power = 0
$out(0, motor)
$sleep(1000)


motor.port = 0x01|0x04
motor.power = 50
$out(0, motor)
$sleep(1000)


motor.port = 0x01|0x02|0x04|0x08
motor.power = 0
$out(0, motor)

$sleep(1000)