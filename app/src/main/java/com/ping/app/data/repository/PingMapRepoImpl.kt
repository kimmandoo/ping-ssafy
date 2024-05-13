package com.ping.app.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.ping.app.domain.dao.PingMapRepo
import com.ping.app.domain.model.Gathering
import java.util.Locale

private const val TAG = "PingMapRepoImpl_싸피"
class PingMapRepoImpl private constructor(context: Context) : PingMapRepo {
    private val appContext: Context = context
    
    override fun sendPingInfo(data: Gathering) {
        Log.d(TAG, "sendPingInfo: $data")
    }
    
    override fun requestAddress(lat: Double, lng: Double): String {
        val geoCoder = Geocoder(appContext, Locale.KOREA)
        var addressResult = "주소를 가져 올 수 없습니다."
        runCatching {
            geoCoder.getFromLocation(lat, lng, 1) as ArrayList<Address>
        }.onSuccess { response ->
            if (response.size > 0) {
                val currentLocationAddress = response[0].getAddressLine(0)
                    .toString()
                addressResult = currentLocationAddress
            }
        }.onFailure {
            Log.d("requestAddress", "error: ${it.stackTrace}")
        }
        
        return addressResult
    }
    
    companion object {
        private var instance: PingMapRepoImpl? = null
        fun initialize(context: Context): PingMapRepoImpl {
            if (instance == null) {
                synchronized(PingMapRepoImpl::class.java) {
                    if (instance == null) {
                        instance = PingMapRepoImpl(context)
                    }
                }
            }
            return instance!!
        }
        
        fun getInstance(): PingMapRepoImpl {
            return instance!!
        }
    }
}