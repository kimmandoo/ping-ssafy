package com.ping.app.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

abstract class BaseFragment<B : ViewDataBinding, VM: ViewModel>(
    @LayoutRes private val layoutId: Int,
) : Fragment() {
    private var _binding: B? = null
    protected val binding get() = _binding!!
    protected abstract val viewModel: VM


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        initView(savedInstanceState)
    }

    protected abstract fun initView(savedInstanceState: Bundle?)

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
