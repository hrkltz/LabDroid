package io.github.herrherklotz.chameleon.x.backend.runtime.component.com


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class BluetoothBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                    BluetoothAdapter.STATE_OFF -> {
                        LogElement.info("Bluetooth", "OFF")
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        LogElement.info("Bluetooth", "TURNING OFF")
                    }
                    BluetoothAdapter.STATE_ON -> {
                        LogElement.info("Bluetooth", "ON")
                    }
                }
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                LogElement.info("Bluetooth", "Disconnected")

                GlobalScope.launch(Dispatchers.IO) {
                    ChameleonSystem.projectName = null
                }
            }
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                LogElement.info("Bluetooth", "Connected")}
        }
    }
}