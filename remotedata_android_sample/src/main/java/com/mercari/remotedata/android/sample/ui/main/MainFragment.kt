package com.mercari.remotedata.android.sample.ui.main

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.mercari.remotedata.android.RemoteData.Failure
import com.mercari.remotedata.android.RemoteData.Initial
import com.mercari.remotedata.android.RemoteData.Loading
import com.mercari.remotedata.android.RemoteData.Success
import com.mercari.remotedata.android.sample.R
import com.mercari.remotedata.android.sample.databinding.MainFragmentBinding

class MainFragment : Fragment(R.layout.main_fragment) {

    private val viewModel: MainViewModel by viewModels {
        SavedStateViewModelFactory(requireContext().applicationContext as Application, this)
    }
    private var binding: MainFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MainFragmentBinding.bind(view)
        binding?.buttonStartLoading?.setOnClickListener {
            viewModel.loadText()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.state
                .map { state -> state.remoteText }
                .distinctUntilChanged()
                .observe(viewLifecycleOwner, Observer { remoteText ->
                    when (remoteText) {
                        is Initial -> {
                            binding?.message?.text = "Not loaded yet."
                        }
                        is Loading -> {
                            binding?.message?.text = "Loading..."
                        }
                        is Success -> {
                            binding?.message?.text = remoteText.value
                        }
                        is Failure -> {
                            binding?.message?.text = remoteText.error.javaClass.name
                        }
                    }
                })
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
