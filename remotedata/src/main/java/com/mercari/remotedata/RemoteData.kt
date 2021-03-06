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

    object Initial : RemoteData<Nothing, Nothing>(), Incomplete

    class Loading<V : Any> @JvmOverloads constructor(
            progress: Int? = null,
            val totalUnits: Int = 100
    ) : RemoteData<V, Nothing>(), Incomplete {

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
    }

    class Success<out V : Any>(val value: V) : RemoteData<V, Nothing>(), Complete {

        override fun component1(): V = value

        override fun get(): V = value

        override fun equals(other: Any?): Boolean =
                if (other === this) true
                else {
                    other is Success<*> && other.value == value
                }

        override fun hashCode(): Int = javaClass.hashCode() * 31 + value.hashCode()
    }

    class Failure<out E : Exception>(val error: E) : RemoteData<Nothing, E>(), Complete {

        override fun component2(): E = error

        override fun get() = throw error

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
            RemoteData.Initial -> RemoteData.Initial
            is RemoteData.Loading -> RemoteData.Loading(progress)
            is RemoteData.Success -> RemoteData.Success(transform(value))
            is RemoteData.Failure -> RemoteData.Failure(transformError(error))
        }

fun <V : Any, E : Exception, U : Any> RemoteData<V, E>.flatMap(
        transform: (V) -> RemoteData<U, E>
): RemoteData<U, E> =
        when (this) {
            RemoteData.Initial -> RemoteData.Initial
            is RemoteData.Loading -> RemoteData.Loading(progress)
            is RemoteData.Success -> transform(value)
            is RemoteData.Failure -> this
        }

fun <V : Any, E : Exception, EE : Exception> RemoteData<V, E>.flatMapError(
        transform: (E) -> RemoteData<V, EE>
): RemoteData<V, EE> =
        when (this) {
            RemoteData.Initial -> RemoteData.Initial
            is RemoteData.Loading -> this
            is RemoteData.Success -> this
            is RemoteData.Failure -> transform(error)
        }

fun <U : Any, V : Any, E : Exception> RemoteData<U, E>.fanout(anotherRemoteData: RemoteData<V, E>): RemoteData<Pair<U, V>, E> =
        fanout(anotherRemoteData, ::Pair)

fun <U : Any, V : Any, T : Any, E : Exception> RemoteData<U, E>.fanout(anotherRemoteData: RemoteData<V, E>, transform: (U, V) -> T) =
        flatMap { outer -> anotherRemoteData.map { inner -> transform(outer, inner) } }
