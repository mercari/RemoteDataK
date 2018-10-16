package com.mercari.remotedata

sealed class RemoteData<out V : Any, out E : Exception> {

    companion object {}

    open operator fun component1(): V? = null
    open operator fun component2(): E? = null

    open fun get(): V? = null

    object NotAsked : RemoteData<Nothing, Nothing>()

    class Loading<V : Any> : RemoteData<V, Nothing>() {
        override fun equals(other: Any?): Boolean = other is Loading<*>

        override fun hashCode(): Int = javaClass.hashCode()
    }

    class Success<out V : Any>(val value: V) : RemoteData<V, Nothing>() {

        override fun component1(): V = value

        override fun get(): V = value
    }

    class Failure<out E : Exception>(val error: E) : RemoteData<Nothing, E>() {

        override fun component2(): E = error

        override fun get() = throw error
    }

    val isNotAsked
        get() = this === NotAsked

    val isLoading
        get() = this is Loading

    val isSuccess
        get() = this is Success

    val isFailure
        get() = this is Failure
}

fun <V : Any, E : Exception, U : Any> RemoteData<V, E>.map(
        transform: (V) -> U
): RemoteData<U, E> =
        mapBoth({ transform(it) }, { it })

fun <V : Any, E : Exception, EE : Exception> RemoteData<V, E>.mapError(
        transform: (E) -> EE
): RemoteData<V, EE> =
        mapBoth({ it }, { transform(it) })

fun <V : Any, E : Exception> RemoteData<V, E>.getOrElse(defaultValue: V) = when (this) {
    is RemoteData.Success -> value
    else -> defaultValue
}

internal inline fun <V : Any, E : Exception, U : Any, EE : Exception> RemoteData<V, E>.mapBoth(
        transform: (V) -> U,
        transformError: (E) -> EE
): RemoteData<U, EE> =
        when (this) {
            RemoteData.NotAsked -> RemoteData.NotAsked
            is RemoteData.Loading -> RemoteData.Loading()
            is RemoteData.Success -> RemoteData.Success(transform(value))
            is RemoteData.Failure -> RemoteData.Failure(transformError(error))
        }

fun <V : Any, E : Exception, U : Any> RemoteData<V, E>.flatMap(
        transform: (V) -> RemoteData<U, E>
): RemoteData<U, E> =
        when (this) {
            RemoteData.NotAsked -> RemoteData.NotAsked
            is RemoteData.Loading -> RemoteData.Loading()
            is RemoteData.Success -> transform(value)
            is RemoteData.Failure -> this
        }

fun <V : Any, E : Exception, EE : Exception> RemoteData<V, E>.flatMapError(
        transform: (E) -> RemoteData<V, EE>
): RemoteData<V, EE> =
        when (this) {
            RemoteData.NotAsked -> RemoteData.NotAsked
            is RemoteData.Loading -> RemoteData.Loading()
            is RemoteData.Success -> this
            is RemoteData.Failure -> transform(error)
        }
