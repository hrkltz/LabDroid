$sleep(500)

const count = 1000

let start = $now()
for (let i = 0; i < count; i++) {
    let a = $receive(0)
}
let dt = ($now() - start)/1000000000

$log("dt: " + (dt))
$log("Hz: " + (count/dt))