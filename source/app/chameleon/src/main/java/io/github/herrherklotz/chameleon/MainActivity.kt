package io.github.herrherklotz.chameleon

import android.Manifest
import android.R.attr
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.os.*
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import io.github.herrherklotz.chameleon.MainService.Companion.context
import io.github.herrherklotz.chameleon.fragments.WebFragment
import io.github.herrherklotz.chameleon.helper.LogElement
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem2
import io.github.herrherklotz.chameleon.x.backend.runtime.component.hw.Battery
import io.github.herrherklotz.chameleon.x.utils.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity: AppCompatActivity() {
    private var mService: MainService? = null
    private var mServiceStarted = false
    /**
     * Callbacks for service binding, passed to bindService()
     */
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(pClassName: ComponentName, pService: IBinder) {
            val lLocalBinder = pService as MainService.LocalBinder // cast the
            // IBinder and get MyService instance
            mService = lLocalBinder.getService()
        }

        override fun onServiceDisconnected(pClassName: ComponentName) {
            // TODO something like this? Toast.makeText(getApplicationContext(), "WARNING: Some
            //  functions requires that Make will be running in foreground!", Toast.LENGTH_SHORT).
            //  show();
        }
    }
    private var mDrawerLayout: DrawerLayout? = null
    private var mNavController: NavController? = null
    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mPreview: Preview? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    @SuppressLint("CheckResult")
    override fun onCreate(pSavedInstanceState: Bundle?) {
        super.onCreate(pSavedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        // Set up ActionBar
        setSupportActionBar(findViewById(R.id.toolbar))
        mNavController = Navigation.findNavController(this, R.id.nav_fragment)
        val lNavigationView = findViewById<NavigationView>(R.id.navigation_view)
        NavigationUI.setupWithNavController(lNavigationView, mNavController!!)
        NavigationUI.setupActionBarWithNavController(this, mNavController!!, mDrawerLayout)

        if (pSavedInstanceState == null) {
            val lIntent = Intent(this@MainActivity, MainService::class.java)
            startForegroundService(lIntent) // The system does not destroy the service when all
            // clients unbind! A call to stopSelf() or stopService() is required.
            bindService(lIntent, mServiceConnection, Context.BIND_IMPORTANT)
            mServiceStarted = true
        }

        eventSttOn.subscribe {
            runOnUiThread {
                if (mSpeechRecognizer == null) {
                    mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                }

                val lRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                lRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, it.second);
                mSpeechRecognizer!!.setRecognitionListener(it.first);

                mSpeechRecognizer!!.startListening(lRecognizerIntent)
            }
        }

        eventSttOff.subscribe {
            runOnUiThread {
                if (mSpeechRecognizer != null) {
                    //mSpeechRecognizer!!.stopListening()
                    mSpeechRecognizer!!.cancel()
                }
            }
        }

        eventTorch.subscribe {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                return@subscribe

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context!!)

            cameraProviderFuture.addListener(kotlinx.coroutines.Runnable {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                // Preview
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector
                        .LENS_FACING_BACK).build()

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()
                    // Bind use cases to camera
                    val camera = cameraProvider.bindToLifecycle(this, cameraSelector,
                            preview)

                    if (!camera.cameraInfo.hasFlashUnit()) {
                        LogElement.error("Torch", "Your camera has no flashlight!")
                        return@Runnable
                    }

                    camera.cameraControl.enableTorch(it)
                } catch (exc: Exception) {
                    LogElement.error("Torch", "Use case binding failed")
                }
            }, ContextCompat.getMainExecutor(context!!))
        }


        eventGpsOn.subscribe {
            runOnUiThread {
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                mFusedLocationProviderClient.requestLocationUpdates(it.locationRequest!!, it
                        .locationCallback!!, Looper.myLooper())
            }
        }


        eventGpsOff.subscribe {
            try {
                mFusedLocationProviderClient.removeLocationUpdates(it)
            } catch (ignored: SecurityException) { }
        }


        eventNfcOn.subscribe {
            val lNfcAdapter = NfcAdapter.getDefaultAdapter(this)

            if (lNfcAdapter != null) {
                val lBundle = Bundle();
                lBundle.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000);

                lNfcAdapter.enableReaderMode(this, it, NfcAdapter.FLAG_READER_NFC_A +
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, lBundle);
            }
        }

        eventNfcOff.subscribe {
            val lNfcAdapter = NfcAdapter.getDefaultAdapter(this)
            lNfcAdapter?.disableReaderMode(this)
        }

        eventStart.subscribe({
            mDrawerLayout!!.closeDrawers()

            runOnUiThread {
                val lBundle = bundleOf(Pair(WebFragment().ARG_FULLSCREEN, true))

                var url = "http://localhost:8080/screen/log.html"

                if (ChameleonSystem2.rProject!!.project.screen == "custom") {
                    if (Storage.exists("Project/${it}/Frontend/index.html"))
                        url = "http://localhost:8080/project/${it}/index.html"
                    else
                        LogElement.error(ChameleonSystem.projectName!!, "Couldn't load custom view!")
                } else if (ChameleonSystem2.rProject!!.project.screen == "black")
                    url = "http://localhost:8080/screen/black.html"

                lBundle.putString(WebFragment().ARG_SECTION_URL, url)

                mNavController!!.popBackStack(R.id.mainFragment, false)
                mNavController!!.navigate(R.id.webFragmentLog, lBundle)
            }
        }, {
            // Empty
        })


        eventReadBattery.subscribe {
            runOnUiThread {
                val lBatteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
                    context!!.registerReceiver(null, it)
                }

                GlobalScope.launch(Dispatchers.Default) {
                    val level: Int = lBatteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale: Int = lBatteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    level * 100 / scale.toFloat()

                    val health = lBatteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                    val plugged = lBatteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val status = lBatteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val temperature = lBatteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                    val voltage = lBatteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

                    Battery.mBroadcastChannels["Data"]!!.send(mapOf<String, Any>("health" to health,
                            "level" to level, "plugged" to plugged, "status" to status,
                            "temperature" to temperature, "voltage" to voltage))
                }
            }
        }

        eventBatteryTechnology.subscribe {
            runOnUiThread {
                val lBatteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let {
                    context!!.registerReceiver(null, it)
                }

                Battery.mPoolChannels["Technology"] = lBatteryStatus!!.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        NavigationUI.navigateUp(mNavController!!, mDrawerLayout)
        return true
    }


    public override fun onRestoreInstanceState(pSavedInstanceState: Bundle) {
        super.onRestoreInstanceState(pSavedInstanceState)
        mServiceStarted = pSavedInstanceState.getBoolean("serviceStarted")
        if (mServiceStarted) {
            val lIntent = Intent(this@MainActivity, MainService::class.java)
            bindService(lIntent, mServiceConnection, Context.BIND_IMPORTANT)
        }
    }


    override fun onSaveInstanceState(pSavedInstanceState: Bundle) {
        super.onSaveInstanceState(pSavedInstanceState)
        pSavedInstanceState.putBoolean("serviceStarted", mServiceStarted)
    }


    override fun onDestroy() {
        super.onDestroy()

        if (mServiceStarted) { // TODO this.mService.setCallbacks(null);
            unbindService(mServiceConnection) // MainService will be still alive because it was
            // created with startService()
            if (this.isFinishing) {
                val lIntent = Intent(this, MainService::class.java)
                stopService(lIntent) // Service shut down
            }
        }

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onBackPressed() {
        if (mNavController!!.currentDestination!!.label == getString(R.string.app_name)) {
            AlertDialog.Builder(this)
                    .setTitle("Exit LabDroid")
                    .setMessage("The LabDroid service will be stopped. Are you sure you want to " +
                            "quit the app?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes) { arg0, arg1 -> super@MainActivity
                            .onBackPressed() }.create().show()

        } else if ((mNavController!!.currentDestination!!.label == getString(R.string.log)) && (ChameleonSystem
                        .projectName != null)) {
            AlertDialog.Builder(this)
                    .setTitle("Stop ${ChameleonSystem.projectName}")
                    .setMessage("A project is currently being executed. Do you want to stop it?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes) { arg0, arg1 ->
                        GlobalScope.launch(Dispatchers.Default) {
                            ChameleonSystem.projectName = null
                        }
                        super.onBackPressed()
                    }.create().show()
        } else
            super.onBackPressed()
    }

    // Required to forward the result to the fragment
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    // Required to forward the result to the fragment
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}