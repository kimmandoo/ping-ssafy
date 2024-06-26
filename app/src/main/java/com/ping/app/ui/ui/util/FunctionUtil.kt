package com.ping.app.ui.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.ping.app.R
import kotlin.math.pow
import kotlin.math.roundToInt

fun Boolean.doOnTrue(func: () -> Unit): Boolean {
    if (this) {
        func()
    }
    return this
}

fun Boolean.doOnFalse(func: () -> Unit): Boolean {
    if (this.not()) {
        func()
    }
    return this
}

fun round(number: Double, scale: Int): Double {
    val factor = 10.0.pow(scale.toDouble())
    return (number * factor).roundToInt() / factor
}

fun findFinality(lastChar: Char): String {
    return when (val jongsung = lastChar.code) {
        in 0xD7A3 downTo 0xAC00 -> {
            if ((jongsung - 0xAC00) % 28 > 0) "이"
            else "가"
        }
        
        else -> {
            "가"
        }
    }
}

fun Context.easyToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun NaverMap.init() {
    apply {
        minZoom = 14.0
        maxZoom = 18.0
        cameraPosition = CameraPosition(
            LatLng(
                this.cameraPosition.target.latitude,
                this.cameraPosition.target.longitude
            ), 16.0
        )
        uiSettings.apply {
            isZoomControlEnabled = false
            logoGravity = Gravity.START and Gravity.TOP
            setLogoMargin(20, 20, 0, 0)
        }
    }
}

fun NaverMap.withMarker(marker: Marker, view: View? = null) {
    addOnCameraChangeListener { type, animated ->
        marker.position = LatLng(
            // 현재 보이는 네이버맵의 정중앙 가운데로 마커 이동
            this@withMarker.cameraPosition.target.latitude,
            this@withMarker.cameraPosition.target.longitude
        )
        marker.captionText = ""
        view?.visibility = View.GONE
    }
    
    // 카메라의 움직임 종료에 대한 이벤트 리스너 인터페이스.
    addOnCameraIdleListener {
        view?.visibility = View.VISIBLE
        marker.apply {
            position = LatLng(
                this@withMarker.cameraPosition.target.latitude,
                this@withMarker.cameraPosition.target.longitude
            )
            captionText = ""
        }
    }
}

fun Marker.init(map: NaverMap) {
    apply {
        position = LatLng(
            map.cameraPosition.target.latitude,
            map.cameraPosition.target.longitude
        )
        icon = OverlayImage.fromResource(R.drawable.map_point_wave_svgrepo_com)
        iconTintColor = Color.RED
    }
}

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.init(
    map: NaverMap,
    marker: Marker,
    locationBtn: View? = null,
) {
    lastLocation.addOnSuccessListener { location: Location ->
        map.locationOverlay.run {
            isVisible = true
            position = LatLng(location.latitude, location.longitude)
        }
        
        locationBtn?.setOnClickListener {
            val resetCamera =
                CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude))
            map.moveCamera(resetCamera.animate(CameraAnimation.Easing))
        }
        val cameraUpdate = CameraUpdate.scrollTo(
            LatLng(
                location.latitude,
                location.longitude
            )
        )
        map.moveCamera(cameraUpdate)
        
        marker.position = LatLng(
            map.cameraPosition.target.latitude,
            map.cameraPosition.target.longitude
        )
    }
}