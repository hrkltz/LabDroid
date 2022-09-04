$sleep(2000)

while (true) {
    $log(JSON.stringify($request(0, 3)))
    $sleep(500)
}
$sleep(1000)