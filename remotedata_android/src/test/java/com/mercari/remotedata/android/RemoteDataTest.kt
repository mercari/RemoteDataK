package com.mercari.remotedata.android

import kotlinx.android.parcel.Parcelize
import org.amshove.kluent.should
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeEqualTo
import org.amshove.kluent.shouldNotEqual
import org.amshove.kluent.shouldThrow
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class RemoteDataTest : Spek({

    describe("success remote data") {
        val data = 42
        val remoteData = RemoteData.Success(data)

        it("extracts value data correctly") {
            remoteData.run {
                value shouldBeEqualTo 42
                get() shouldBeEqualTo 42
            }
        }

        it("equality") {
            val sameSuccess = RemoteData.Success(42)
            remoteData.hashCode() shouldBeEqualTo sameSuccess.hashCode()
            remoteData shouldBeEqualTo sameSuccess

            val anotherSuccess = RemoteData.Success(43)
            remoteData.hashCode() shouldNotBeEqualTo anotherSuccess.hashCode()
            remoteData shouldNotBeEqualTo anotherSuccess
        }

        it("completeness") {
            remoteData.isComplete shouldBeEqualTo true
            remoteData.isIncomplete shouldBeEqualTo false
        }

        it("reports Success") {
            remoteData.run {
                isSuccess shouldBeEqualTo true
                isFailure shouldBeEqualTo false
                isLoading shouldBeEqualTo false
                isInitial shouldBeEqualTo false
            }
        }

        it("destructures data correctly") {
            val (value, error) = remoteData
            value shouldBeEqualTo 42
            error.shouldBeNull()
        }

        it("maps to new type correctly") {
            val mappedRm = remoteData.map { it > 0 }
            val value = mappedRm.get()
            value shouldBeEqualTo true
        }

        it("maps both to new types correctly") {
            val (value, error) = remoteData.mapBoth({ it + it }, { RuntimeError() })

            value shouldBeEqualTo 84
            error.shouldBeNull()
        }

        it("flatMaps to new type correctly") {
            val (value, error) = remoteData.flatMap { RemoteData.Success(it * it) }
            value shouldBeEqualTo 42 * 42
            error.shouldBeNull()
        }

        it("will not flatMapError to new type") {
            val anotherRm = RemoteData.Failure(RuntimeError())
            val (value, error) = (remoteData as RemoteData<Int, ErrorKind>).flatMapError { anotherRm }

            value shouldBeEqualTo 42
            error.shouldBeNull()
        }

        it("getOrElse returns value correctly") {
            val value = remoteData.getOrElse(40)
            value shouldBeEqualTo 42
        }

        it("fanout returns a pair of values") {
            val anotherRm = RemoteData.Success(28)

            val (value, error) = remoteData.fanout(anotherRm)

            value!!.first shouldBeEqualTo 42
            value.second shouldBeEqualTo 28
            error.shouldBeNull()
        }

        data class Foo(val value1: Int, val value2: String)

        it("fanout multiple rm with a custom transform block") {
            val anotherRm = RemoteData.Success(28)

            val fanout = remoteData.fanout(anotherRm) { one, two -> Foo(one, two.toString()) }

            fanout.get()!!.value1 shouldBeEqualTo 42
            fanout.get()!!.value2 shouldBeEqualTo "28"
        }
    }

    describe("failure remote data") {
        val data = IllegalStateError("Not Available")
        val remoteData = RemoteData.Failure(data)

        it("gets exception correctly") {
            remoteData.error.shouldBeInstanceOf<IllegalStateError>()

            val t = { remoteData.get() }
            t shouldThrow (UnsupportedOperationException::class)
        }

        it("equality") {
            val sameFailure = RemoteData.Failure(data) // Exception has to be the same reference!!
            remoteData.hashCode() shouldBeEqualTo sameFailure.hashCode()
            remoteData shouldBeEqualTo sameFailure

            val sameFailureDifferentException = RemoteData.Failure(IllegalStateError("Not Available"))
            remoteData.hashCode() shouldNotBeEqualTo sameFailureDifferentException.hashCode()
            remoteData shouldNotBeEqualTo sameFailureDifferentException

            val anotherFailure = RemoteData.Failure(NullArgumentError())
            remoteData.hashCode() shouldNotBeEqualTo anotherFailure.hashCode()
            remoteData shouldNotBeEqualTo anotherFailure
        }

        it("completeness") {
            remoteData.isComplete shouldBeEqualTo true
            remoteData.isIncomplete shouldBeEqualTo false
        }

        it("reports failure") {
            remoteData.run {
                isSuccess shouldBeEqualTo false
                isFailure shouldBeEqualTo true
                isLoading shouldBeEqualTo false
                isInitial shouldBeEqualTo false
            }
        }

        it("destructures error correctly") {
            val (value, error) = remoteData
            value.shouldBeNull()
            error.message shouldBeEqualTo "Not Available"
        }

        it("will mapError to new type correctly") {
            val (value, error) = remoteData.mapError { RuntimeError("Not Available") }
            value.shouldBeNull()
            error!!.shouldBeInstanceOf<RuntimeError>()
            error.message shouldBe "Not Available"
        }

        it("will not flatMap to new type") {
            val anotherRm = RemoteData.Success(42)
            val (value, error) = remoteData.flatMap { anotherRm }
            value.shouldBeNull()
            error!!.shouldBeInstanceOf<IllegalStateError>()
            error.message shouldBe "Not Available"
        }

        it("will flatMapError to new type correctly") {
            val anotherRm = RemoteData.Failure(IllegalArgumentError("Another"))
            val (value, error) = remoteData.flatMapError { anotherRm }
            value.shouldBeNull()
            error!!.shouldBeInstanceOf<IllegalArgumentError>()
            error.message shouldBe "Another"
        }

        it("maps both to new types correctly") {
            val (value, error) = remoteData.mapBoth({ 42 }, { NullArgumentError() })
            value.shouldBeNull()
            error!!.shouldBeInstanceOf<NullArgumentError>()
        }

        it("getOrElse return default value supplied correctly") {
            val value = remoteData.getOrElse(40)
            value shouldBeEqualTo 40
        }

        it("fanout does not return a pair of values") {
            val anotherRm = RemoteData.Success(28)

            val (value, error) = remoteData.fanout(anotherRm)

            value.shouldBeNull()
            error!!.shouldBeInstanceOf<IllegalStateError>()
            error.message shouldBeEqualTo "Not Available"
        }
    }

    describe("initial remote data") {
        val remoteData = RemoteData.Initial

        it("gets null") {
            remoteData.get().shouldBeNull()
        }

        it("equality") {
            val anotherNotAsked = RemoteData.Initial
            remoteData.hashCode() shouldBeEqualTo anotherNotAsked.hashCode()
            remoteData shouldBeEqualTo anotherNotAsked
        }

        it("completeness") {
            remoteData.isComplete shouldBeEqualTo false
            remoteData.isIncomplete shouldBeEqualTo true
        }

        it("reports initial") {
            remoteData.run {
                isSuccess shouldBeEqualTo false
                isFailure shouldBeEqualTo false
                isLoading shouldBeEqualTo false
                isInitial shouldBeEqualTo true
            }
        }

        it("destructures none of them correctly") {
            val (value, error) = remoteData
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("will not map to new type") {
            val (value, error) = remoteData.map { 500 }
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("will not flatMap to new type") {
            val (value, error) = remoteData.flatMap { RemoteData.Success("Hello world") }
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("maps both to new types correctly") {
            val (value, error) = remoteData.mapBoth({ it }, { RuntimeError() })
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("fanout does not return a pair of values") {
            val anotherRm = RemoteData.Success(28)

            val fanout = remoteData.fanout(anotherRm)

            fanout shouldBe RemoteData.Initial
            fanout.get().shouldBeNull()
        }
    }

    describe("loading remote data") {
        val rmInt = RemoteData.Loading<Int>()
        val rmString = RemoteData.Loading<String>()

        val determinateRmBytes = RemoteData.Loading<ByteArray>(0)
        val indeterminateRmBytes = RemoteData.Loading<ByteArray>()

        val totalUnits = 1024
        val determinateRmBytesWithTotal = RemoteData.Loading<ByteArray>(0, totalUnits)

        it("gets null") {
            rmInt.get().shouldBeNull()
            rmString.get().shouldBeNull()
        }

        it("equality") {
            val sameLoading = RemoteData.Loading<Int>()
            rmInt.hashCode() shouldBeEqualTo sameLoading.hashCode()
            rmInt shouldBeEqualTo sameLoading

            val sameRmBytesDeterminate = RemoteData.Loading<ByteArray>(0)
            val otherDeterminateRmBytes = RemoteData.Loading<ByteArray>(10)
            val otherIndeterminateRmBytes = RemoteData.Loading<ByteArray>()

            determinateRmBytes.hashCode() shouldNotBeEqualTo indeterminateRmBytes.hashCode()
            determinateRmBytes shouldNotBeEqualTo indeterminateRmBytes

            determinateRmBytes.hashCode() shouldBeEqualTo sameRmBytesDeterminate.hashCode()
            determinateRmBytes shouldBeEqualTo sameRmBytesDeterminate

            determinateRmBytes.hashCode() shouldNotBeEqualTo otherDeterminateRmBytes.hashCode()
            determinateRmBytes shouldNotBeEqualTo otherDeterminateRmBytes

            determinateRmBytes.hashCode() shouldNotBeEqualTo determinateRmBytesWithTotal
            determinateRmBytes shouldNotBeEqualTo determinateRmBytesWithTotal

            indeterminateRmBytes.hashCode() shouldBeEqualTo otherIndeterminateRmBytes.hashCode()
            indeterminateRmBytes shouldBeEqualTo otherIndeterminateRmBytes
        }

        it("completeness") {
            rmInt.isComplete shouldBeEqualTo false
            rmInt.isIncomplete shouldBeEqualTo true
        }

        it("has no value at creation but the type is carried along properly") {
            rmInt.shouldBeInstanceOf<RemoteData.Loading<Int>>()
            rmString.shouldBeInstanceOf<RemoteData.Loading<String>>()

            should {
                val k = rmInt::class
                k.isInstance(RemoteData.Loading<Int>())
            }

            should {
                val k = rmString::class
                k.isInstance(RemoteData.Loading<String>())
            }
        }

        it("reports loading") {
            rmInt.run {
                isSuccess shouldBeEqualTo false
                isFailure shouldBeEqualTo false
                isInitial shouldBeEqualTo false
                isLoading shouldBeEqualTo true
            }
            rmString.run {
                isSuccess shouldBeEqualTo false
                isFailure shouldBeEqualTo false
                isInitial shouldBeEqualTo false
                isLoading shouldBeEqualTo true
            }
            determinateRmBytes.run {
                isSuccess shouldBeEqualTo false
                isFailure shouldBeEqualTo false
                isInitial shouldBeEqualTo false
                isLoading shouldBeEqualTo true
            }
            indeterminateRmBytes.run {
                isSuccess shouldBeEqualTo false
                isFailure shouldBeEqualTo false
                isInitial shouldBeEqualTo false
                isLoading shouldBeEqualTo true
            }
        }

        it("reports proper progress type") {
            determinateRmBytes.isIndeterminateProgress shouldBeEqualTo false
            indeterminateRmBytes.isIndeterminateProgress shouldBeEqualTo true
        }

        it("coerces progress properly") {

            RemoteData.Loading<ByteArray>(-1).progress shouldBeEqualTo 0
            RemoteData.Loading<ByteArray>(101).progress shouldBeEqualTo 100
            RemoteData.Loading<ByteArray>(1).progress shouldBeEqualTo 1
            RemoteData.Loading<ByteArray>(99).progress shouldBeEqualTo 99

            indeterminateRmBytes.progress.shouldBeNull()

            determinateRmBytes.run {
                progress = -1
                progress shouldBeEqualTo 0

                progress = 101
                progress shouldBeEqualTo 100

                progress = 1
                progress shouldBeEqualTo 1

                progress = 99
                progress shouldBeEqualTo 99
            }

            determinateRmBytesWithTotal.run {
                progress = -1
                progress shouldBeEqualTo 0

                progress = totalUnits + 1
                progress shouldBeEqualTo totalUnits

                progress = totalUnits - 1
                progress shouldBeEqualTo totalUnits - 1
            }
        }

        it("destructures none of them correctly") {
            val (value, error) = rmInt
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("will not map to new type") {
            val (value, error) = rmInt.map { 500 }
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("will not mapError to new type") {
            val (value, error) = rmInt.mapError { RuntimeError() }
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("will not flatMap to new type") {
            val (value, error) = rmString.flatMap { RemoteData.Success("Hello world") }
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("will not flatMapError to new type") {
            val (value, error) = rmString.flatMapError { RemoteData.Success("World Hello") }
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("maps both to new types correctly") {
            val (value, error) = rmString.mapBoth({ it.count() }, { RuntimeError() })
            value.shouldBeNull()
            error.shouldBeNull()
        }

        it("fanout does not return a pair of values") {
            val anotherRm = RemoteData.Success(28)

            val fanout = rmInt.fanout(anotherRm)

            fanout.shouldBeInstanceOf<RemoteData.Loading<*>>()

            val (value, error) = fanout

            value.shouldBeNull()
            error.shouldBeNull()
        }
    }
})

@Parcelize
class RuntimeError(
        val message: String = ""
) : ErrorKind

@Parcelize
class IllegalStateError(
        val message: String = ""
) : ErrorKind

@Parcelize
class IllegalArgumentError(
        val message: String = ""
) : ErrorKind

@Parcelize
class NullArgumentError(
        val message: String = ""
) : ErrorKind
