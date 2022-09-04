package io.github.herrherklotz.chameleon.x.backend.runtime.component.hw

import io.github.herrherklotz.chameleon.EventGpsOnData
import io.github.herrherklotz.chameleon.x.extensions.IComponent
import io.github.herrherklotz.chameleon.eventGpsOff
import io.github.herrherklotz.chameleon.eventGpsOn
import io.github.herrherklotz.chameleon.x.backend.system.project.component.hw.GpsData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import io.github.herrherklotz.chameleon.helper.LogElement
import java.util.concurrent.TimeUnit


object Gps : IComponent, GpsData() {
    private var baseConfig: GpsData = GpsData()
    private var mLocationRequest: LocationRequest? = null
    private var mLocationCallback: LocationCallback? = null

    @ExperimentalCoroutinesApi
    override val mBroadcastChannels: ConcurrentMap<String, BroadcastChannel<Any>> =
            ConcurrentHashMap(mapOf("Data" to BroadcastChannel<Any>(1)))
    override val mChannels: ConcurrentMap<String, Channel<Any>> = ConcurrentHashMap(emptyMap())
    override val mPoolChannels: ConcurrentMap<String, Any?> = ConcurrentHashMap(emptyMap())
    override var mRunning: Boolean = false


    override fun off() {
        if (!mRunning)
            return

        if (mLocationCallback != null)
            eventGpsOff.onNext(mLocationCallback!!)

        mLocationCallback = null
        mLocationRequest = null

        LogElement.info("Gps", "Off")

        mRunning = false
    }


    @Throws(Exception::class)
    override fun on(data: Any?) {
        if (mRunning)
            return

        mRunning = true

        Gps.minTime = baseConfig.minTime
        Gps.minDistance = baseConfig.minDistance

        when (data) {
            is Map<*,*> -> {
                if (data.containsKey("minTime"))
                    Gps.minTime = (data["minTime"] as Int).toLong()
                if (data.containsKey("minDistance"))
                    Gps.minDistance = data["minDistance"] as Float
            }
            is GpsData -> {
                Gps.minTime = data.minTime
                Gps.minDistance = data.minDistance
            }
        }

        val lBroadcastChannelData = mBroadcastChannels["Data"]

        mLocationRequest = LocationRequest().apply {
            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = TimeUnit.SECONDS.toMillis(minTime)

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = TimeUnit.SECONDS.toMillis(minTime)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            // maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            // Set the minimum displacement between location updates in meters
            smallestDisplacement = minDistance

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                if (locationResult.lastLocation == null)
                    return

                val location = locationResult.lastLocation!!

                // TODO: I guess runBlocking makes here no different but avoid a nother thread
                GlobalScope.launch(Dispatchers.Default) {
                    lBroadcastChannelData!!.send(mapOf<String, Any>("accuracy" to location
                            .accuracy, "altitude" to location.altitude, "bearing" to location
                            .bearing, "latitude" to location.latitude, "longitude" to location
                            .longitude, "speed" to location.speed))
                }
            }
        }

        val eventGpsOnData = EventGpsOnData()
        eventGpsOnData.locationCallback = mLocationCallback
        eventGpsOnData.locationRequest = mLocationRequest

        eventGpsOn.onNext(eventGpsOnData)

        LogElement.info("Gps", "On")
    }
}