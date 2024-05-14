package com.ping.app.ui.ui.feature.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.geometry.LatLng
import com.ping.app.PingApplication
import com.ping.app.R
import com.ping.app.data.model.Gathering
import com.ping.app.databinding.FragmentMainBinding
import com.ping.app.ui.base.BaseFragment
import com.ping.app.ui.feature.main.MainAdapter
import com.ping.app.ui.presentation.main.MainViewModel
import com.ping.app.ui.presentation.map.PingMapViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 툴바 (일단 나중에 하는 걸로)
 *
 * 마이 페이지? 같은
 * - 이룸
 * - 이메일
 *
 * 리사이클러뷰 (list adapter 작성 완)
 * MEETING TABLE에서 값을 가져 와야함
 * 해당 값은 ViewModel에 넣아서 observer로 관리
 *
 *
 */
private const val TAG = "MainFragment_싸피"
class MainFragment : BaseFragment<FragmentMainBinding, MainViewModel>(R.layout.fragment_main) {
    
    override val viewModel: MainViewModel by viewModels()
    private val mainInstance = PingApplication.mainRepo
    private val pingMapViewModel: PingMapViewModel by activityViewModels()
    override fun initView(savedInstanceState: Bundle?) {
        var lat = 0.0
        var lng = 0.0
        
        
        

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                 pingMapViewModel.userLocation.collectLatest { currentLocation ->
                    Log.d(TAG, "init@@@@@@@@@View: ${currentLocation}")
                    if (currentLocation != null) {
                        lat = currentLocation.latitude
                        lng = currentLocation.longitude

                        initMeetingList(lat, lng)
                    }
                }
            }
        }

        lifecycleScope.launch {
            initMeetingList(lat, lng)
        }
        val mainAdapter = MainAdapter(onMoveDetailedConfirmation = {
            Log.d(TAG, "initView: ${it}")
        })

        binding.mainFragRecyclerview.adapter = mainAdapter
        binding.mainFragRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        viewModel.meetingList.observe(viewLifecycleOwner) { it ->
            it?.let { mainAdapter.submitList(it) }
        }
        
        binding.mainFragFab.setOnClickListener {
            Log.d(TAG, "initView: add ${it}")
        }


        Log.d(TAG, "initView: ${viewModel.meetingList.value}")
//        Log.d(TAG, "initView:2222 ${MainRepoImpl.get().getMeetingTable(lat, lng)}")
    }

    private suspend fun initLatLng() : Pair<Double, Double>{
        val loadlatlng = CompletableDeferred<LatLng?>()

        var lat = 0.0
        var lng = 0.0
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pingMapViewModel.userLocation.collectLatest { currentLocation ->
                    Log.d(TAG, "init@@@@@@@@@View: ${currentLocation}")
                    if (currentLocation != null) {
                        lat = currentLocation.latitude
                        lng = currentLocation.longitude
                        loadlatlng.complete(currentLocation)
                    }
                }
            }
        }



        loadlatlng.await()


        Log.d(TAG, "initLatLng: ${lat} ${lng}")
        return Pair(lat, lng)
    }

    suspend fun initMeetingList(lat: Double, lng: Double){
        

        val updateList = CompletableDeferred<List<Gathering>>()
        var getGathering = listOf<Gathering>()
        lifecycleScope.launch {
            getGathering = mainInstance.getMeetingTable(lng, lat)
            updateList.complete(getGathering)
        }
        updateList.await()
        viewModel.updateMeetingList(getGathering)

    }




}