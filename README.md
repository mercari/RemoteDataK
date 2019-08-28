# RemoteData for Kotlin

[![jcenter](https://api.bintray.com/packages/mercari-inc/maven/remotedatak/images/download.svg)](https://bintray.com/mercari-inc/maven/remotedatak/_latestVersion) 
[![Build Status](https://circleci.com/gh/mercari/RemoteDataK.svg?style=svg)](https://circleci.com/gh/mercari/RemoteDataK)
[![codecov](https://codecov.io/gh/mercari/RemoteData/branch/master/graph/badge.svg)](https://codecov.io/gh/mercari/RemoteData)

Algebraic data type (ADT) to represent the state of data that is loading from/to remote sources/destinations

## Setup

```kotlin
dependencies {
  repositories {
    jcenter()
  }
}

implementation("com.mercari.remotedata:remotedata:<latest-version>")
```

## About

RemoteData is useful to represent the state of data, when it is loading from/to a remote source/destination.

Using RemoteData is pretty straightforward, it is however meant to be used in a functional style.

RemoteData works nicely with [RxJava](https://github.com/ReactiveX/RxJava) and [RxRedux](https://github.com/mercari/RxRedux), however it can be used independently.

This is done through 4 types: `Initial`, `Loading`, `Success` and `Failure`.

- `Initial` represents the initial state of the data before any progress has been made.

- `Loading` represents the intermediary loading state of data.

- `Success` represents the state where the loading is completed, and holds the resulting value of the data.

- `Failure` represents the state where the loading failed, and holds information about the reason of failure with an Exception. 

In cases where the data size is known, you may find the properties `progress` and `totalUnits` of the `Loading` type useful.
The value of `progress` is always between `0` and `totalUnits` (default of `100` for percentages). 

`Initial` and `Loading` are `Incomplete`, whereas `Success` and `Failure` are `Complete`.

## Usage

A common use case for RemoteData would be mapping it into a UI transition or component state.

For example, when making a network request to fetch data, most UIs have a progress indicator and finally display the result.

- Declare your data where you need it, where V is the type of data value and E the type of Exception/Error.

```kotlin
var data: RemoteData<V, E> = Initial

```

- Once the data starts to load, transition to `Loading` with the optional setting of a `progress` and `totalUnits`.

```kotlin
data = Loading()
```

If `progress` is used, simply update it when required.

```kotlin
data.progress = data.progress?.let { it + delta }
```

- Once the loading is complete successfully, transition to `Success` along with the actual data.

```kotlin
data = Success(value)
```

- Otherwise, transition to `Failure` with a proper `Exception`.

```kotlin
data = Failure(Exception("error"))
```

- Behaviour based on the data state can then be mapped neatly, wherever the data is processed.
```kotlin
when (data) {
    is Initial -> initialize()
    is Loading -> progress(data.progress)
    is Success -> success(data.value) 
    is Failure -> failure(data.error)
 }
```

Or with the other conveniences:

```kotlin
when {
    data.isIncomplete -> showProgress()
    data.isComplete -> hideProgress()
 }
```

### Higher oder functions

A few higher order functions are also provided for convenience such as:

- map
- mapError
- mapBoth
- getOrElse
- flatMap
- flatMapError
- fanout

For examples, take a look at some of the comprehensive [tests](https://github.com/mercari/RemoteData/blob/master/remotedata/src/test/java/com/mercari/remotedata/RemoteDataTest.kt).

## Contribution

Please read the CLA below carefully before submitting your contribution.

https://www.mercari.com/cla/

## License

Copyright 2018-2019 Mercari, Inc.

Licensed under the MIT License. 
