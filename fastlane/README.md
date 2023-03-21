fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Runs all the tests

### android deployAlpha

```sh
[bundle exec] fastlane android deployAlpha
```

Deploy app to play store alpha channel

### android deployInternalTest

```sh
[bundle exec] fastlane android deployInternalTest
```



### android buildApp

```sh
[bundle exec] fastlane android buildApp
```



### android buildWearApp

```sh
[bundle exec] fastlane android buildWearApp
```



### android buildAppApk

```sh
[bundle exec] fastlane android buildAppApk
```



### android promoteAppToProd

```sh
[bundle exec] fastlane android promoteAppToProd
```

Promote app from alpha to production in Play Store

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
