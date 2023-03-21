Thanks for contributing to Confetti 🎊!


Confetti is a conference app to experiment with the latest Kotlin technologies and libraries, share best practices and have fun!

Contributions are very welcome.

If you have a feature you want to implement or found a bug, feel free to [open an issue](https://github.com/joreilly/Confetti/issues/new) and we can discuss how to best implement/fix it.

If you don't know where to start, you can also take a look at the list of [good first issues](https://github.com/joreilly/Confetti/labels/good%20first%20issue) for good places to get started with the project.

Documentation, UX, design, data contributions are very welcome. 

If you see a conference missing in the list, please let us know by [opening an issue](https://github.com/joreilly/Confetti/issues/new).

## Dev environment

At the time of writing, in order to build the Confetti app, you'll need:

* JDK 17+, you can get one with [Homebrew](https://formulae.brew.sh/cask/zulu) on macOS or with you favorite package manager.
* Android Studio Electric Eel or higher. 
* Xcode 14.2+ for the iOS app

Open the root project in Android Studio. Useful tasks:

* `./gradlew :androidApp:assembleDebug` 
* `./gradlew :wearApp:assembleDebug` 
* `./gradle :backend:service-graphql:bootRun` 

When running `bootRun`, you will only be able to use the "test" conference as access to the real data requires additional credentials.

Happy hacking!





