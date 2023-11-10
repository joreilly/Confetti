Thanks for contributing to Confetti 🎊!


Confetti is a conference app to experiment with the latest Kotlin technologies and libraries, share best practices and have fun!

Contributions are very welcome.

If you have a feature you want to implement or found a bug, feel free to [open an issue](https://github.com/joreilly/Confetti/issues/new) and we can discuss how to best implement/fix it.

If you don't know where to start, you can also take a look at the list of [good first issues](https://github.com/joreilly/Confetti/labels/good%20first%20issue) for good places to get started with the project.

Documentation, UX, design, data contributions are very welcome. 

## Adding a conference

Conference data is hosted in Google Cloud Datastore. 

To add a conference, add a new `dev.johnoreilly.confetti.backend.datastore./ConferenceId` and an import function. You can use [this pull request](https://github.com/joreilly/Confetti/pull/982/files) as an example.

Once this is done and the pull request is merged, you can trigger the import by issuing a POST to `https://confetti-app.dev/update/$conferenceId`:

```shell
# update devfestwarsaw2023 conference
curl --data "" https://confetti-app.dev/update/devfestwarsaw2023
```

The data should start appearing in the app shortly after. You can double check by using the [GraphQL sandbox](https://confetti-app.dev/sandbox/index.html?explorerURLState=N4IgJg9gxgrgtgUwHYBcQC4TADpIAR5QRIBmCATslAgM5474F4CWYuBAvrhyB0A).

If you see a conference missing in the list and don't know where to start with the PR, feel free [to open an issue](https://github.com/joreilly/Confetti/issues/new) and we'll look into it.

## Dev environment

At the time of writing, in order to build the Confetti app, you'll need:

* JDK 17+, you can get one with [Homebrew](https://formulae.brew.sh/cask/zulu) on macOS or with you favorite package manager.
* Android Studio Electric Eel or higher. 
* Xcode 14.2+ for the iOS app

To checkout the sources you need to install git-lfs:

* Download git-lfs on your system: `brew install git-lfs`
* install git-lfs in your workspace folder: `git lfs install`

Now you can clone the Github repository: `git clone https://github.com/joreilly/Confetti`

## Mobile & watch apps
For Android, open the root project in Android Studio. Useful tasks:

* `./gradlew :androidApp:assembleDebug` 
* `./gradlew :wearApp:assembleDebug` 

For iOS, open the `iosApp/iosApp.xcodeproj` in Xcode and run the app.

## API docs 

API docs are available at [backend/README.md](backend/README.md)

## Backend

You can run the backend locally with the following task:

* `./gradle :backend:service-graphql:bootRun` 

When running `bootRun`, you will only be able to use the "test" conference as access to the real data requires additional credentials.


Happy hacking!





