package io.github.herrherklotz.chameleon

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import io.github.herrherklotz.chameleon.web.WebServer
import io.github.herrherklotz.chameleon.websocket.WebSocketServer
import io.github.herrherklotz.chameleon.x.backend.ChameleonSystem
import java.io.IOException


class MainService: Service() {
    companion object {
        var context: Context? = null
    }


    private var mWakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var mHTTPServer: WebServer? = null
    private var mWSServer: WebSocketServer? = null

    val mBinder: IBinder = LocalBinder()


    inner class LocalBinder: Binder() {
        fun getService() : MainService? {
            return this@MainService
        }

       /* internal// Return this instance of MainService so clients can call
        // public methods
        val service: MainService
            get() = this@MainService*/
    }


    override fun onBind(intent: Intent): IBinder? {
        return mBinder;
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startService()

        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }


    override fun onCreate() {
        super.onCreate()

        context = this

        startForeground(1, createNotification())
    }


    override fun onDestroy() {
        super.onDestroy()

        stopForeground(true)
        stopSelf()

        ChameleonSystem.projectName = null

        if (this.mWSServer != null)
            this.mWSServer!!.stop()

        if (this.mHTTPServer != null)
            this.mHTTPServer!!.stop()

        if (mWakeLock != null && mWakeLock!!.isHeld)
            mWakeLock!!.release()

        isServiceStarted = false

        context = null; // TODO check.. could be source for a new error
    }


    @SuppressLint("WakelockTimeout")
    private fun startService() {
        if (isServiceStarted)
            return

        isServiceStarted = true

        this.mHTTPServer = WebServer()

        try {
            this.mHTTPServer!!.start()
        } catch (ignore: IOException) { }

        this.mWSServer = WebSocketServer()

        // we need this lock so our service gets not affected by Doze Mode
        mWakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Chameleon::ServiceLock")
                    .apply {
                        acquire()
                    }
        }

        //FileInterface2.load("/Project/template/")
        //Storage.readDirectory("/")
    }


    private fun createNotification(): Notification {
        val notificationChannelId = "io.github.herrherklotz.chameleon.MainServiceChannel"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        val channel = NotificationChannel(
                notificationChannelId,
                "LabDroid Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
        ).let {
            it.description = "LabDroid Service channel"
            it
        }
        notificationManager.createNotificationChannel(channel)

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java)
            .let {
            notificationIntent -> PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        val builder: Notification.Builder = Notification.Builder(this,
                notificationChannelId)

        val lWifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lIp = lWifiManager.connectionInfo.ipAddress // get current IP
        val lIpAsString = String.format("%d.%d.%d.%d", lIp and 0xff, lIp shr 8 and 0xff,
                lIp shr 16 and 0xff, lIp shr 24 and 0xff)

        return builder
                .setContentTitle("LabDroid Service")
                .setContentText("http://${lIpAsString}:${WebServer.PORT}")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setTicker("Service started")
                .build()
    }
}