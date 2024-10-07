package com.lf.magpie_bridge

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lf.magpie_bridge.databinding.FragmentNativeClientBinding

class NativeClientFragment : Fragment() {
    companion object {
        private const val TAG = "NativeClientFragment"
    }

    private val sharedVM by lazy {
        ViewModelProvider(requireActivity()).get(SharedVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNativeClientBinding.inflate(inflater, container, false)
        binding.sendBtn.setOnClickListener {
            val cmdStr = binding.cmdEt.text.toString()
            val argsStr = binding.argsEt.text.toString()

            val bridgeType =
                binding.bridgeTypeRg.findViewById<RadioButton>(binding.bridgeTypeRg.checkedRadioButtonId).text.toString()
            val value = Triple(bridgeType, TestCommandN2J.Loop.name, argsStr)
            Log.d(TAG, "value=$value sharedVM=$sharedVM")
            sharedVM.message.value = value
        }
        Log.d(TAG, "sharedVM=$sharedVM")
        sharedVM.loopBack.observe(viewLifecycleOwner) {
            binding.displayTv.text = it
        }
        return binding.root
    }

}