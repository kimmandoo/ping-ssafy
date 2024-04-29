package com.ping.app.presentation.ui.feature.map

import android.os.Bundle
import androidx.annotation.UiThread
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import com.ping.app.R
import com.ping.app.databinding.FragmentMapBinding
import com.ping.app.presentation.base.BaseFragment


class MapFragment : BaseFragment<FragmentMapBinding, MapViewModel>(R.layout.fragment_map),
    OnMapReadyCallback {
    override val viewModel: MapViewModel by viewModels()
    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource
    
    override fun initView(savedInstanceState: Bundle?) {
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }
    
    @UiThread
    override fun onMapReady(map: NaverMap) {
        // 이 화면은 일정을 누르면 나올것이기 때문에 객체로 넘어오는 lat, lng값을 지도의 초기 위치로 잡고, 마커를 띄운다.
        naverMap = map
        naverMap.apply {
            // 카메라 보이는 범위 조정하기 (대한민국 남서쪽 끝 ~ 북동쪽 끝(독도포함)) LatLng(33.0041, 124.6865), LatLng(38.6340, 131.8662)
            // 넘어오는 좌표기준으로 반경 1km로 보이는 범위 제한하기
            // 계산식 필요함...
            extent = LatLngBounds(LatLng(33.0041, 124.6865), LatLng(38.6340, 131.8662))
            minZoom = 8.0
            maxZoom = 18.0
            uiSettings.apply {
                // zoom 버튼 제거하기
                isZoomControlEnabled = false
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
    
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    
}