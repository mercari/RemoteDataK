package com.mercari.remotedata

import org.amshove.kluent.should
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqual
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
                value shouldEqual 42
                get() shouldEqual 42
            }
        }

        it("equality") {
            val sameSuccess = RemoteData.Success(42)
            remoteData.hashCode() shouldEqual sameSuccess.hashCode()
            remoteData shouldEqual sameSuccess

            val anotherSuccess = RemoteData.Success(43)
            remoteData.hashCode() shouldNotEqual anotherSuccess.hashCode()
            remoteData shouldNotEqual anotherSuccess
        }

        it("reports Success") {
            remoteData.run {
                isSuccess shouldEqual true
                isFailure shouldEqual false
                isLoading shouldEqual false
                isNotAsked shouldEqual false
            }
        }

        it("destructures data correctly") {
            val (value, error) = remoteData
            value shouldEqual 42
            error.shouldBeNull()
        }

        it("maps to new type correctly") {
            val mappedRm = remoteData.map { it > 0 }
            val value = mappedRm.get()
            value shouldEqual true
        }

        it("maps both to new types correctly") {
            val (value, error) = remoteData.mapBoth({ it + it }, { NullPointerException() })

            value shouldEqual 84
            error.shouldBeNull()
        }

        it("flatMaps to new type correctly") {
            val (value, error) = remoteData.flatMap { RemoteData.Success(it * it) }
            value shouldEqual 42 * 42
            error.shouldBeNull()
        }

        it("will not flatMapError to new type") {
            val anotherRm = RemoteData.Failure(NullPointerException())
            val (value, error) = (remoteData as RemoteData<Int, Exception>).flatMapError { anotherRm }

            value shouldEqual 42
            error.shouldBeNull()
        }

        it("getOrElse return value correctly") {
            val value = remoteData.getOrElse(40)
            value shouldEqual 42
        }
    }

    describe("failure remote data") {
        val data = IllegalStateException("Not Available")
        val remoteData = RemoteData.Failure(data)

        it("gets exception correctly") {
            remoteData.error.shouldBeInstanceOf<IllegalStateException>()

            val t = { remoteData.get() }
            t shouldThrow (IllegalStateException::class)
        }

        it("equality") {
            val sameFailure = RemoteData.Failure(data) // Exception has to be the same reference!!
            remoteData.hashCode() shouldEqual sameFailure.hashCode()
            remoteData shouldEqual sameFailure

            val sameFailureDifferentException = RemoteData.Failure(IllegalStateException("Not Available"))
            remoteData.hashCode() shouldNotEqual sameFailureDifferentException.hashCode()
            remoteData shouldNotEqual sameFailureDifferentException

            val anotherFailure = RemoteData.Failure(NullPointerException())
            remoteData.hashCode() shouldNotEqual anotherFailure.hashCode()
            remoteData shouldNotEqual anotherFailure
        }

        it("reports failure") {
            remoteData.run {
                isSuccess shouldEqual false
                isFailure shouldEqual true
                isLoading shouldEqual false
                isNotAsked shouldEqual false
            }
        }

        it("destructures error correctly") {
            val (value, error) = remoteData
            value.shouldBeNull()
            error.message shouldEqual "Not Available"
        }

        it("will mapError to new type correctly") {
            val (value, error) = remoteData.mapError { RuntimeException(it.message) }
            value.shouldBeNull()
            error!!.shouldBeInstanceOf<RuntimeException>()
            error.message shouldBe "Not Available"
        }

        it("will not flatMap to new type") {
            val anotherRm = RemoteData.Success(42)
            val (value, error) = remoteData.flatMap { anotherRm }
            value.shouldBeNull()
            error!!.shouldBeInstanceOf<IllegalStateException>()
            error.message shouldBe "Not Available"
        }

        it("will flatMapError to new type correctly") {
            val anotherRm = RemoteData.Failure(IllegalArgumentException("Another"))
            val (value, error) = remoteData.flatMapError { anotherRm }
            value.shouldBeNull()
            error!!.shouldBeInstanceOf<IllegalArgumentException>()
            error.message shouldBe "Another"
        }

        it("maps both to new types correctly") {
            val (value, error) = remoteData.mapBoth({ 42 }, { NullPointerException() })
            value.shouldBeNull()
            error!!.shouldBeInstanceOf<NullPointerException>()
        }

        it("getOrElse return default value supplied correctly") {
            val value = remoteData.getOrElse(40)
            value shouldEqual 40
        }
    }

    describe("notAsked remote data") {
        val remoteData = RemoteData.NotAsked

        it("gets null") {
            remoteData.get().shouldBeNull()
        }

        it("equal to another") {
            val anotherNotAsked = RemoteData.NotAsked
            remoteData.hashCode() shouldEqual anotherNotAsked.hashCode()
            remoteData shouldEqual anotherNotAsked
        }

        it("reports notAsked") {
            remoteData.run {
                isSuccess shouldEqual false
                isFailure shouldEqual false
                isLoading shouldEqual false
                isNotAsked shouldEqual true
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
            val (value, error) = remoteData.mapBoth({ it }, { NullPointerException() })
            value.shouldBeNull()
            error.shouldBeNull()
        }
    }

    describe("loading remote data") {
        val rmInt = RemoteData.Loading<Int>()
        val rmString = RemoteData.Loading<String>()

        it("gets null") {
            rmInt.get().shouldBeNull()
            rmString.get().shouldBeNull()
        }

        it("equality") {
            val sameLoading = RemoteData.Loading<Int>()
            rmInt.hashCode() shouldEqual sameLoading.hashCode()
            rmInt shouldEqual sameLoading
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
            rmInt.isSuccess shouldEqual false
            rmInt.isFailure shouldEqual false
            rmString.isLoading shouldEqual true
            rmString.isNotAsked shouldEqual false
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
            val (value, error) = rmInt.mapError { NullPointerException() }
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
            val (value, error) = rmString.mapBoth({ it.count() }, { NullPointerException() })
            value.shouldBeNull()
            error.shouldBeNull()
        }
    }
})
