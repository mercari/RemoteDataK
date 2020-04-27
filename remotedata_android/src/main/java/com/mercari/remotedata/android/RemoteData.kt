package com.mercari.remotedata.android

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

interface ErrorKind : Parcelable

sealed class RemoteData<out V : Any, out E : ErrorKind> : Parcelable {

    /**
     * This allows extension functions over the companion object for each RemoteData
     */
    @Suppress("RemoveEmptyClassBody")
    companion object {}

    open operator fun component1(): V? = null
    open operator fun component2(): E? = null

    open fun get(): V? = null

    @Parcelize
    object Initial : RemoteData<Nothing, Nothing>(), Incomplete

    class Loading<V : Any> @JvmOverloads constructor(
            progress: Int? = null,
            val totalUnits: Int = 100
    ) : RemoteData<V, Nothing>(), Incomplete {

        constructor(source: Parcel?) : this(
                progress = source?.readInt()?.let {
                    if (it == -1) {
                        null
                    } else {
                        it
                    }
                },
                totalUnits = source?.readInt() ?: 100
        )

        var progress: Int? = progress?.coerceTo(totalUnits)
            set(value) {
                field = value?.coerceTo(totalUnits)
            }

        private fun Int?.coerceTo(totalUnits: Int): Int = this?.coerceIn(0..totalUnits) ?: 0

        val isIndeterminateProgress: Boolean = progress == null

        override fun equals(other: Any?): Boolean =
                if (other === this) true
                else {
                    other is Loading<*> && other.progress == progress && other.totalUnits == totalUnits
                }

        override fun hashCode(): Int =
                (javaClass.hashCode() * 31 + progress?.plus(1).hashCode()) * 31 + totalUnits.hashCode()

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.apply {
                writeInt(progress ?: -1)
                writeInt(totalUnits)
            }
        }

        override fun describeContents(): Int = 0

        companion object {
            @JvmField
            val CREATOR: Creator<Loading<Parcelable>> = object : Creator<Loading<Parcelable>> {
                override fun createFromParcel(source: Parcel?): Loading<Parcelable> = Loading(source)
                override fun newArray(size: Int): Array<Loading<Parcelable>?> = arrayOfNulls(size)
            }
        }
    }

    class Success<out V : Any>(val value: V) : RemoteData<V, Nothing>(), Complete {

        @Suppress("UNCHECKED_CAST")
        private constructor(parcel: Parcel) : this(parcel.readValue(Thread.currentThread().contextClassLoader) as V)

        override fun component1(): V = value

        override fun get(): V = value

        override fun equals(other: Any?): Boolean =
                if (other === this) true
                else {
                    other is Success<*> && other.value == value
                }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.writeValue(value)
        }

        override fun describeContents(): Int = 0

        override fun hashCode(): Int = javaClass.hashCode() * 31 + value.hashCode()

        companion object {
            @JvmField
            val CREATOR: Creator<Success<Any>> = object : Creator<Success<Any>> {
                override fun createFromParcel(source: Parcel): Success<Any> = Success(source)

                override fun newArray(size: Int): Array<Success<Any>?> = arrayOfNulls(size)
            }
        }
    }

    @Parcelize
    class Failure<out E : ErrorKind>(val error: E) : RemoteData<Nothing, E>(), Complete {

        override fun component2(): E = error

        override fun get() = throw UnsupportedOperationException("no value to obtain")

        override fun equals(other: Any?): Boolean =
                if (other === this) true
                else {
                    other is Failure<*> && other.error == error
                }

        override fun hashCode(): Int = javaClass.hashCode() * 31 + error.hashCode()
    }

    interface Complete

    interface Incomplete

    val isInitial
        get() = this is Initial

    val isLoading
        get() = this is Loading

    val isSuccess
        get() = this is Success

    val isFailure
        get() = this is Failure

    val isComplete
        get() = this is Complete

    val isIncomplete
        get() = this is Incomplete
}

fun <V : Any, E : ErrorKind, U : Any> RemoteData<V, E>.map(
        transform: (V) -> U
): RemoteData<U, E> =
        mapBoth(transform, { it })

fun <V : Any, E : ErrorKind, EE : ErrorKind> RemoteData<V, E>.mapError(
        transform: (E) -> EE
): RemoteData<V, EE> =
        mapBoth({ it }, transform)

fun <V : Any, E : ErrorKind> RemoteData<V, E>.getOrElse(defaultValue: V) = when (this) {
    is RemoteData.Success -> value
    else -> defaultValue
}

internal inline fun <V : Any, E : ErrorKind, U : Any, EE : ErrorKind> RemoteData<V, E>.mapBoth(
        transform: (V) -> U,
        transformError: (E) -> EE
): RemoteData<U, EE> =
        when (this) {
            is RemoteData.Initial -> RemoteData.Initial
            is RemoteData.Loading -> RemoteData.Loading(progress)
            is RemoteData.Success -> RemoteData.Success(transform(value))
            is RemoteData.Failure -> RemoteData.Failure(transformError(error))
        }

fun <V : Any, E : ErrorKind, U : Any> RemoteData<V, E>.flatMap(
        transform: (V) -> RemoteData<U, E>
): RemoteData<U, E> =
        when (this) {
            is RemoteData.Initial -> RemoteData.Initial
            is RemoteData.Loading -> RemoteData.Loading(progress)
            is RemoteData.Success -> transform(value)
            is RemoteData.Failure -> this
        }

fun <V : Any, E : ErrorKind, EE : ErrorKind> RemoteData<V, E>.flatMapError(
        transform: (E) -> RemoteData<V, EE>
): RemoteData<V, EE> =
        when (this) {
            is RemoteData.Initial -> RemoteData.Initial
            is RemoteData.Loading -> this
            is RemoteData.Success -> this
            is RemoteData.Failure -> transform(error)
        }

fun <U : Any, V : Any, E : ErrorKind> RemoteData<U, E>.fanout(anotherRemoteData: RemoteData<V, E>): RemoteData<Pair<U, V>, E> =
        fanout(anotherRemoteData, ::Pair)

fun <U : Any, V : Any, T : Any, E : ErrorKind> RemoteData<U, E>.fanout(anotherRemoteData: RemoteData<V, E>, transform: (U, V) -> T): RemoteData<T, E> =
        flatMap { outer -> anotherRemoteData.map { inner -> transform(outer, inner) } }
