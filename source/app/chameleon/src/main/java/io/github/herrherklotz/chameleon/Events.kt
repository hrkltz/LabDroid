package io.github.herrherklotz.chameleon

import android.nfc.NfcAdapter
import android.speech.RecognitionListener
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject


val eventTorch: Subject<Boolean> = PublishSubject.create()

class EventGpsOnData {
    var locationRequest: LocationRequest? = null
    var locationCallback: LocationCallback? = null
}

val eventGpsOn: Subject<EventGpsOnData> = PublishSubject.create()
val eventGpsOff: Subject<LocationCallback> = PublishSubject.create()
val eventSttOn: Subject<Pair<RecognitionListener, String>> = PublishSubject.create()
val eventSttOff: Subject<RecognitionListener> = PublishSubject.create()
val eventNfcOn: Subject<NfcAdapter.ReaderCallback> = PublishSubject.create()
val eventNfcOff: Subject<NfcAdapter.ReaderCallback> = PublishSubject.create()
val eventStart: Subject<String> = PublishSubject.create()
val eventReadBattery: Subject<Unit> = PublishSubject.create()
val eventBatteryTechnology: Subject<Unit> = PublishSubject.create()
