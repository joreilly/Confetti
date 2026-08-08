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

### android deployInternalTestAndroid

```sh
[bundle exec] fastlane android deployInternalTestAndroid
```



### android deployInternalTestWear

```sh
[bundle exec] fastlane android deployInternalTestWear
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


## iOS

### ios createApp

```sh
[bundle exec] fastlane ios createApp
```

One-time: register the bundle id and create the App Store Connect app record

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Build a Release archive and upload it to TestFlight

### ios buildStatus

```sh
[bundle exec] fastlane ios buildStatus
```

Show the processing state of the most recent TestFlight builds

### ios uploadMetadata

```sh
[bundle exec] fastlane ios uploadMetadata
```

Upload the App Store listing text (fastlane/metadata/ios) to App Store Connect — no binary, no screenshots

### ios uploadScreenshots

```sh
[bundle exec] fastlane ios uploadScreenshots
```

Upload the App Store screenshots (fastlane/metadata/ios/screenshots/<locale>) to App Store Connect

### ios release

```sh
[bundle exec] fastlane ios release
```

Submit the latest TestFlight build + metadata to the App Store for review

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
