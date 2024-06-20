package com.fra.sec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.fra.sec.databinding.LayoutTipBinding

class HintDialog : DialogFragment() {
    lateinit var onAllow: () -> Unit
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = LayoutTipBinding.inflate(inflater)
        binding.tvAllow.setOnClickListener {
            onAllow.invoke()
            dismiss()
        }
        binding.ivButtonClose.setOnClickListener {
            dismiss()
        }
        return binding.root
    }
}