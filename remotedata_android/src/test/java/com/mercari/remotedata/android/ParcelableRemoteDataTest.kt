package com.mercari.remotedata.android

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.IllegalStateException
import java.lang.RuntimeException

@RunWith(RobolectricTestRunner::class)
class ParcelableRemoteDataTest {
    @Test
    fun putAndGetInitial() {
        // Given: a value to be stored in the Bundle
        val value: RemoteData.Initial = RemoteData.Initial
        val marshalled: ByteArray = marshall(value)

        // When: Recover from marshalled byte array
        val restored: RemoteData.Initial = unmarshall(marshalled)

        // Then: Restored value should be the same as the initial value
        restored shouldBeEqualTo value
    }

    @Test
    fun putAndGetLoading_primitives() {
        // Given: a value to be stored in the Bundle
        val value: RemoteData.Loading<Int> = RemoteData.Loading(progress = 50, totalUnits = 100)
        val marshalled: ByteArray = marshall(value)

        // When: Store it to the Bundle
        val restored: RemoteData.Loading<Int> = unmarshall(marshalled)

        // Then: Restored value should be the same as the initial value
        restored shouldBeEqualTo value
    }

    @Test
    fun putAndGetLoading_strings() {
        // Given: a value to be stored in the Bundle
        val value: RemoteData.Loading<String> = RemoteData.Loading(progress = 50, totalUnits = 100)
        val marshalled: ByteArray = marshall(value)

        // When: Store it to the Bundle
        val restored: RemoteData.Loading<String> = unmarshall(marshalled)

        // Then: Restored value should be the same as the initial value
        restored shouldBeEqualTo value
    }

    @Test
    fun putAndGetLoading_parcelables() {
        // Given: a value to be stored in the Bundle
        val value: RemoteData.Loading<SampleParcelable> = RemoteData.Loading(progress = 50, totalUnits = 100)
        val marshalled: ByteArray = marshall(value)

        // When: Store it to the Bundle
        val restored: RemoteData.Loading<SampleParcelable> = unmarshall(marshalled)

        // Then: Restored value should be the same as the initial value
        restored shouldBeEqualTo value
    }

    @Test
    fun putAndGetSuccess_primitives() {
        // Given: a value to be stored in the Bundle
        val value: RemoteData.Success<Int> = RemoteData.Success(100)
        val marshalled: ByteArray = marshall(value)

        // When: Store it to the Bundle
        val restored: RemoteData.Success<Int> = unmarshall(marshalled)

        // Then: Restored value should be the same as the initial value
        restored shouldBeEqualTo value
    }

    @Test
    fun putAndGetSuccess_strings() {
        // Given: a value to be stored in the Bundle
        val value: RemoteData.Success<String> = RemoteData.Success("aaa")
        val marshalled: ByteArray = marshall(value)

        // When: Store it to the Bundle
        val restored: RemoteData.Success<String> = unmarshall(marshalled)

        // Then: Restored value should be the same as the initial value
        restored shouldBeEqualTo value
    }

    @Test
    fun putAndGetSuccess_parcelables() {
        // Given: a value to be stored in the Bundle
        val value: RemoteData.Success<SampleParcelable> = RemoteData.Success(SampleParcelable("aaa"))
        val marshalled: ByteArray = marshall(value)

        // When: Store it to the Bundle
        val restored: RemoteData.Success<SampleParcelable> = unmarshall(marshalled)

        // Then: Restored value should be the same as the initial value
        restored shouldBeEqualTo value
    }

    @Test
    fun putAndGetFailure() {
        // Given: a value to be stored in the Bundle
        val value: RemoteData.Failure<ErrorKind> = RemoteData.Failure(NetworkError())
        val marshalled: ByteArray = marshall(value)

        // When: Store it to the Bundle
        val restored: RemoteData.Failure<ErrorKind> = unmarshall(marshalled)

        // Then: Restored value should be the same as the initial value
        restored.error.shouldBeInstanceOf<NetworkError>()
    }

    @Test(expected = RuntimeException::class)
    fun putAndGetSuccess_nonParcelable() {
        // Given: a value to be stored in the Bundle
        val value: RemoteData.Success<NonParcelable> = RemoteData.Success(NonParcelable("aaa"))
        val marshalled: ByteArray = marshall(value) // throws RuntimeException at this point

        // When: Store it to the Bundle
        val restored: RemoteData.Success<NonParcelable> = unmarshall(marshalled)

        // Then: Restored value should be the same as the initial value
        restored shouldBeEqualTo value
    }

    private fun marshall(value: RemoteData<Any, ErrorKind>): ByteArray = Parcel.obtain().apply {
        writeParcelable(value, 0)
    }.marshall()

    private inline fun <reified T : RemoteData<Any, ErrorKind>> unmarshall(byteArray: ByteArray): T = Parcel.obtain().let {
        it.unmarshall(byteArray, 0, byteArray.size)
        it.setDataPosition(0)
        it.readParcelable<T>(T::class.java.classLoader) ?: throw IllegalStateException("could not unmarshall the ")
    }
}

@Parcelize
data class SampleParcelable(
        val text: String
) : Parcelable

data class NonParcelable(
        val text: String
)

@Parcelize
class NetworkError : ErrorKind
