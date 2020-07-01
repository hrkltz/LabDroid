/* Project: Dice v1
 * Script: Dice
 * Inputs:
 *   0 <-> Accelerometer.Data
 */
 
// Function to generate a random number from 1 to 6.
function roll() {
  return 1+Math.floor(Math.random() * Math.floor(6))
}


let force = 0.0
let rolled = ""


while(true) {
    const accel = $in(0)
    
    force += Math.abs(accel.x) + Math.abs(accel.y)
    
    if (force > 100.0) {
        // Keep the dice on the floor
        force = 90.0
    } else if (force > 30.0) {
        // The dice is rolling
        force *= 0.6
        rolled += roll() + " "
    }
    else if (force > 10) {
        // The dice wiggles out
        force *= 0.6 
    } else {
        if (rolled.length > 0) {
            // If the dice was rolled, then generate the final result
            $log(rolled + "*" + roll() + "*")
            rolled = ""
            
            // Give the dice some time to cool down
            $sleep(1000)
        }
        
        force = 0.0
    }
}
