$sleep(1000)

const kp = 40
const ki = 1
const kd = 1

const motor = {
    "function": "turnAtPower",
    "port": 0x01|0x02,
    "power": 0
}

let oldP = 0
let dt = 0
let start = $now()
let end = $now()
let dE = 0
let preError = 0

while (true) {
    dt = ((end - start)/1000000000)
    $log("dt: " + dt)

    start = $now()

    const accel = $receive(0).z

    const error = 0 - accel
    const P = kp * error

    dE += error * dt
    const I = ki * dE


    const derivative = (error - preError)/dt
    const D = kd * derivative;

    //$log("P: " + Math.round(P))
    //$log("I: " + Math.round(I))
    //$log("D: " + Math.round(D))
    //$log("PID: " + Math.round(P+I+D))

    motor["power"] = Math.round(P+I+D)

    if (motor["power"] > 100) motor["power"] = 100
    else if (motor["power"] < -100) motor["power"] = -100

    if (P != oldP) {
        $log("P: " + motor["power"])
        $out(0, motor)
        oldP = P+I
    }

    preError = error
    $sleep(3)
    end = $now()
}