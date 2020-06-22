package com.mercari.remotedata.android.sample.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mercari.remotedata.android.ErrorKind
import com.mercari.remotedata.android.RemoteData.Failure
import com.mercari.remotedata.android.RemoteData.Loading
import com.mercari.remotedata.android.RemoteData.Success
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit

class MainViewModel(
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val stateMutation: MutableLiveData<MainViewState> = MutableLiveData(
            savedStateHandle[STATE_MAIN_VIEW] ?: MainViewState()
    )
    val state: LiveData<MainViewState> = stateMutation
    private val disposables: CompositeDisposable = CompositeDisposable()

    fun loadText() {
        disposables.add(
            Observable.just("Hello, World!")
                    .delay(3, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        stateMutation.nextState { state ->
                            state.copy(remoteText = Loading())
                        }
                    }.subscribe({ text ->
                        stateMutation.nextState { state ->
                            state.copy(remoteText = Success(text))
                        }
                    }, { exp ->
                        stateMutation.nextState { state ->
                            state.copy(remoteText = Failure(exp.toErrorKind()))
                        }
                    })
        )
    }

    fun onSaveState() {
        savedStateHandle[STATE_MAIN_VIEW] = state.value
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    companion object {
        private const val STATE_MAIN_VIEW = "MainViewModel.STATE_MAIN_VIEW"
    }
}

private fun MutableLiveData<MainViewState>.nextState(mapper: (MainViewState) -> MainViewState) {
    value?.let {
        value = mapper.invoke(it)
    }
}

@Parcelize
class IllegalState : ErrorKind
@Parcelize
class UnknownError : ErrorKind

private fun Throwable.toErrorKind(): ErrorKind = when (this) {
    is IllegalStateException -> IllegalState()
    else -> UnknownError()
}
