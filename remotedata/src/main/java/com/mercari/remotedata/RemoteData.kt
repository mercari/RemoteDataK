package com.mercari.remotedata

sealed class RemoteData<out V : Any, out E : Exception> {

    /**
     * This allows extension functions over the companion object for each RemoteData
     */
    @Suppress("RemoveEmptyClassBody")
    companion object {}

    open operator fun component1(): V? = null
    open operator fun component2(): E? = null

    open fun get(): V? = null

    object NotAsked : RemoteData<Nothing, Nothing>()

    class Loading<V : Any>(val progress: Progress = Progress.Indeterminate) : RemoteData<V, Nothing>() {

        sealed class Progress {
            class Determinate(percentage: Int) : Progress() {
                var percentage : Int = percentage
                set(value) {
                    if (value in 0..100) {
                        if (value <= field) {
                            field = value
                        } else throw IllegalArgumentException("percentage should not decrease")
                    } else throw IllegalArgumentException("percentage should be between 0 and 100")
                }

                override fun equals(other: Any?): Boolean =
                        if (other === this) true
                        else {
                            other is Determinate && other.percentage == percentage
                        }

                override fun hashCode(): Int = javaClass.hashCode() * 31 + percentage.hashCode()
            }

            object Indeterminate : Progress()
        }

        constructor(percentage: Int) : this(Progress.Determinate(percentage))

        override fun equals(other: Any?): Boolean =
                if (other === this) true
                else {
                    other is Loading<*> && other.progress == progress
                }

        override fun hashCode(): Int = javaClass.hashCode() * 31 + progress.hashCode()
    }

    class Success<out V : Any>(val value: V) : RemoteData<V, Nothing>() {

        override fun component1(): V = value

        override fun get(): V = value

        override fun equals(other: Any?): Boolean =
                if (other === this) true
                else {
                    other is Success<*> && other.value == value
                }

        override fun hashCode(): Int = javaClass.hashCode() * 31 + value.hashCode()
    }

    class Failure<out E : Exception>(val error: E) : RemoteData<Nothing, E>() {

        override fun component2(): E = error

        override fun get() = throw error

        override fun equals(other: Any?): Boolean =
                if (other === this) true
                else {
                    other is Failure<*> && other.error == error
                }

        override fun hashCode(): Int = javaClass.hashCode() * 31 + error.hashCode()
    }

    val isNotAsked
        get() = this is NotAsked

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
        mapBoth(transform, { it })

fun <V : Any, E : Exception, EE : Exception> RemoteData<V, E>.mapError(
        transform: (E) -> EE
): RemoteData<V, EE> =
        mapBoth({ it }, transform)

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
            is RemoteData.Loading -> RemoteData.Loading(progress)
            is RemoteData.Success -> RemoteData.Success(transform(value))
            is RemoteData.Failure -> RemoteData.Failure(transformError(error))
        }

fun <V : Any, E : Exception, U : Any> RemoteData<V, E>.flatMap(
        transform: (V) -> RemoteData<U, E>
): RemoteData<U, E> =
        when (this) {
            RemoteData.NotAsked -> RemoteData.NotAsked
            is RemoteData.Loading -> RemoteData.Loading(progress)
            is RemoteData.Success -> transform(value)
            is RemoteData.Failure -> this
        }

fun <V : Any, E : Exception, EE : Exception> RemoteData<V, E>.flatMapError(
        transform: (E) -> RemoteData<V, EE>
): RemoteData<V, EE> =
        when (this) {
            RemoteData.NotAsked -> RemoteData.NotAsked
            is RemoteData.Loading -> RemoteData.Loading(progress)
            is RemoteData.Success -> this
            is RemoteData.Failure -> transform(error)
        }
