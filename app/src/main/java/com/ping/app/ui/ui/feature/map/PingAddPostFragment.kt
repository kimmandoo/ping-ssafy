package com.ping.app.ui.ui.feature.map

import android.app.AlertDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import com.naver.maps.geometry.LatLng
import com.ping.app.PingApplication
import com.ping.app.R
import com.ping.app.data.model.Gathering
import com.ping.app.databinding.DialogPingAddBinding
import com.ping.app.databinding.FragmentPingAddPostBinding
import com.ping.app.ui.base.BaseBottomSheetDialogFragment
import com.ping.app.ui.presentation.MainActivityViewModel
import com.ping.app.ui.presentation.map.PingMapViewModel
import com.ping.app.ui.ui.util.Map.USER_POSITION_LAT
import com.ping.app.ui.ui.util.Map.USER_POSITION_LNG
import com.ping.app.ui.ui.util.easyToast
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val TAG = "PingAddPostFragment_싸피"

class PingAddPostFragment :
    BaseBottomSheetDialogFragment<FragmentPingAddPostBinding, PingMapViewModel>(
        R.layout.fragment_ping_add_post
    ) {
    override val viewModel: PingMapViewModel by activityViewModels()
    
    /** mainActivityViewModel 분리 해야함
     */
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val pingMapInstance = PingApplication.pingMapRepo
    private lateinit var gatheringTime: String
    
    override fun initView(savedInstanceState: Bundle?) {
        val pingPosition = LatLng(
            requireArguments().getDouble(USER_POSITION_LAT),
            requireArguments().getDouble(USER_POSITION_LNG)
        )
        val symbol = requireArguments().getString("symbol")
        binding.apply {
            if (symbol.toString().isNotEmpty()) {
                addPostEtWhere.setText(symbol)
            }
            addPostTvAddress.text =
                pingMapInstance.requestAddress(pingPosition.latitude, pingPosition.longitude)
            addPostIvDialog.setOnClickListener {
                addDateDialog()
            }
            pingAddRg.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.ping_add_post_cb_all -> {
                        binding.addPostCode.visibility = View.GONE
                    }
                    
                    R.id.ping_add_post_cb_friend -> {
                        binding.addPostCode.visibility = View.VISIBLE
                    }
                }
            }
            
            addPostBtnSend.setOnClickListener {
                Log.d(TAG, "addPostBtnSend: ${mainActivityViewModel.userUid.value.toString()}")
                val title = addPostEtWhere.text.toString()
                val content = addPostEtWhat.text.toString()
                if (::gatheringTime.isInitialized && title.isNotEmpty() && content.isNotEmpty()) {
                    
                    if (binding.addPostCode.visibility == View.VISIBLE) {
                        // 코드가 있을때만 참여가능
                        val enterCode = binding.addPostCode.text.toString()
                        if (enterCode.isNotEmpty()) {
                            pingMapInstance.sendPingInfo(
                                Gathering(
                                    uid = mainActivityViewModel.userUid.value.toString(),
                                    uuid = UUID.randomUUID().toString(),
                                    enterCode = enterCode,
                                    gatheringTime = gatheringTime,
                                    title = title,
                                    content = content,
                                    longitude = pingPosition.longitude,
                                    latitude = pingPosition.latitude
                                )
                            )
                        } else {
                            binding.root.context.easyToast(getString(R.string.blank_et))
                        }
                    } else {
                        // 모두 참여가능
                        pingMapInstance.sendPingInfo(
                            Gathering(
                                uid = mainActivityViewModel.userUid.value.toString(),
                                uuid = UUID.randomUUID().toString(),
                                enterCode = "",
                                gatheringTime = gatheringTime,
                                title = title,
                                content = content,
                                longitude = pingPosition.longitude,
                                latitude = pingPosition.latitude
                            )
                        )
                    }
                } else {
                    binding.root.context.easyToast(getString(R.string.blank_et))
                }
            }
        }
    }
    
    /**
     *  calendar, timepicker에서 값을 받아와 확인버튼을 누르면 parent view에 값을 넣어준다.
     */
    private fun addDateDialog() {
        // dialog 띄운다.
        val dialogBinding = DialogPingAddBinding.inflate(layoutInflater)
        dialogBinding.apply {
            val calendar = Calendar.getInstance()
            addPostDp.minDate = calendar.timeInMillis
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val max = Calendar.getInstance()
            max.set(year, month, calendar.get(Calendar.DAY_OF_MONTH) + 7)
            addPostDp.maxDate = max.timeInMillis
            var targetDay = 0L
            addPostDp.setOnDateChangeListener { view, yy, mm, dd ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(yy, mm, dd)
                val currentCalendar = Calendar.getInstance()
                val differenceInMillis =
                    selectedCalendar.timeInMillis - currentCalendar.timeInMillis
                
                targetDay = TimeUnit.MILLISECONDS.toDays(differenceInMillis)
            }
            addPostTp.apply {
                setOnTimeChangedListener { view, hourOfDay, minute ->
                    val currentCalendar = Calendar.getInstance()
                    if ((targetDay * 24 + hourOfDay) * 60 + minute < currentCalendar.get(Calendar.HOUR_OF_DAY) * 60 + currentCalendar.get(
                            Calendar.MINUTE
                        )
                    ) {
                        context.easyToast("선택할 수 없는 시간입니다.")
                        this.hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
                        this.minute = currentCalendar.get(Calendar.MINUTE)
                    }
                }
            }
        }
        
        val dialog =
            AlertDialog.Builder(binding.root.context).setView(dialogBinding.root)
                .setNegativeButton("취소") { dialog, which ->
                    dialog.dismiss()
                }.setPositiveButton("확인") { dialog, which ->
                    val format = "M월 d일"
                    val h = dialogBinding.addPostTp.hour
                    val hour = if (h > 12) {
                        "오후 ${h - 12}"
                    } else {
                        "오전 $h"
                    }
                    val currentTime = Calendar.getInstance().time
                    val formattedDateString = "${
                        SimpleDateFormat(format, Locale.KOREA).format(currentTime)
                    } ${hour}시 ${dialogBinding.addPostTp.minute}분"
                    gatheringTime =
                        (currentTime.time + dialogBinding.addPostTp.hour * 60 * 60 + dialogBinding.addPostTp.minute * 60).toString()
                    binding.addPostTv.text = formattedDateString
                }.create()
        dialog.window!!.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.bg_white_radius_20,
                null
            )
        )
        dialog.show()
    }
}