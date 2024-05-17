package com.ping.app.ui.ui.feature.map

import android.app.AlertDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.naver.maps.geometry.LatLng
import com.ping.app.PingApplication
import com.ping.app.R
import com.ping.app.data.model.Gathering
import com.ping.app.databinding.DialogPingAddBinding
import com.ping.app.databinding.FragmentPingAddPostBinding
import com.ping.app.ui.base.BaseBottomSheetDialogFragment
import com.ping.app.ui.presentation.map.PingMapViewModel
import com.ping.app.ui.ui.util.Map.USER_POSITION_LAT
import com.ping.app.ui.ui.util.Map.USER_POSITION_LNG
import com.ping.app.ui.ui.util.easyToast
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

private const val TAG = "PingAddPostFragment_싸피"

class PingAddPostFragment :
    BaseBottomSheetDialogFragment<FragmentPingAddPostBinding, PingMapViewModel>(
        R.layout.fragment_ping_add_post
    ) {
    override val viewModel: PingMapViewModel by activityViewModels()
    
    private val pingMapInstance = PingApplication.pingMapRepo
    private val loginRepoImpl = PingApplication.loginRepo
    private lateinit var gatheringTime: String
    private lateinit var uid: String
    
    override fun initView(savedInstanceState: Bundle?) {
        val pingPosition = LatLng(
            requireArguments().getDouble(USER_POSITION_LAT),
            requireArguments().getDouble(USER_POSITION_LNG)
        )
        val symbol = requireArguments().getString("symbol")
        binding.apply {
            lifecycleScope.launch {
                uid = loginRepoImpl.getAccessToken()
            }
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
                val title = addPostEtWhere.text.toString()
                val content = addPostEtWhat.text.toString()
                if (::gatheringTime.isInitialized && title.isNotEmpty() && content.isNotEmpty()) {
                    
                    if (binding.addPostCode.visibility == View.VISIBLE) {
                        // 코드가 있을때만 참여가능
                        val enterCode = binding.addPostCode.text.toString()
                        if (enterCode.isNotEmpty()) {
                            pingMapInstance.sendPingInfo(
                                Gathering(
                                    uid = uid,
                                    uuid = UUID.randomUUID().toString(),
                                    enterCode = enterCode,
                                    gatheringTime = gatheringTime,
                                    title = title,
                                    content = content,
                                    longitude = pingPosition.longitude,
                                    latitude = pingPosition.latitude
                                )
                            )
                            dismiss()
                        } else {
                            binding.root.context.easyToast(getString(R.string.blank_et))
                        }
                    } else {
                        // 모두 참여가능
                        pingMapInstance.sendPingInfo(
                            Gathering(
                                uid = uid,
                                uuid = UUID.randomUUID().toString(),
                                enterCode = "",
                                gatheringTime = gatheringTime,
                                title = title,
                                content = content,
                                longitude = pingPosition.longitude,
                                latitude = pingPosition.latitude
                            )
                        )
                        dismiss()
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
        var targetDay = 0L
        val dialogBinding = DialogPingAddBinding.inflate(layoutInflater)
        dialogBinding.apply {
            val calendar = Calendar.getInstance()
            addPostDp.minDate = calendar.timeInMillis
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val max = Calendar.getInstance()
            max.set(year, month, calendar.get(Calendar.DAY_OF_MONTH) + 7)
            addPostDp.maxDate = max.timeInMillis
            
            // Initialize targetDay with the current date
            targetDay = calendar.timeInMillis
            
            addPostDp.setOnDateChangeListener { view, yy, mm, dd ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(yy, mm, dd)
                targetDay = selectedCalendar.timeInMillis
            }
            
            addPostTp.apply {
                setOnTimeChangedListener { view, hourOfDay, minute ->
                    val currentCalendar = Calendar.getInstance()
                    val targetCalendar = Calendar.getInstance().apply {
                        timeInMillis = targetDay
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                    }
                    if (targetCalendar.before(currentCalendar)) {
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
                    val selectedCalendar = Calendar.getInstance().apply {
                        timeInMillis = targetDay
                        set(Calendar.HOUR_OF_DAY, dialogBinding.addPostTp.hour)
                        set(Calendar.MINUTE, dialogBinding.addPostTp.minute)
                    }
                    val formattedDateString =
                        "${SimpleDateFormat(format, Locale.KOREA).format(selectedCalendar.time)} " +
                            "${
                                if (selectedCalendar.get(Calendar.HOUR_OF_DAY) > 12) "오후 ${
                                    selectedCalendar.get(
                                        Calendar.HOUR_OF_DAY
                                    ) - 12
                                }" else "오전 ${selectedCalendar.get(Calendar.HOUR_OF_DAY)}"
                            }시 " +
                            "${selectedCalendar.get(Calendar.MINUTE)}분"
                    binding.addPostTv.text = formattedDateString
                    
                    // Save the selected date and time
                    gatheringTime = selectedCalendar.timeInMillis.toString()
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