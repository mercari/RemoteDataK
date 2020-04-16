package com.mercari.remotedata.android.sample.ui.main

import android.os.Parcelable
import com.mercari.remotedata.android.ErrorKind
import com.mercari.remotedata.android.RemoteData
import com.mercari.remotedata.android.RemoteData.Initial
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MainViewState(
        val remoteText: RemoteData<String, ErrorKind> = Initial
) : Parcelable
