# Confetti 🎊

![kotlin-version](https://img.shields.io/badge/kotlin-1.8.20-orange)

[<img src="https://upload.wikimedia.org/wikipedia/commons/7/78/Google_Play_Store_badge_EN.svg"
     alt="Get it on Google Play Store"
     height="80">](https://play.google.com/store/apps/details?id=dev.johnoreilly.confetti)
[<img src="https://developer.apple.com/assets/elements/badges/download-on-the-app-store.svg"
     alt="Get it on App Store"
   height="80">](https://apps.apple.com/us/app/confetti/id1660211390)


KMM GraphQL project to allow viewing of conference information. Includes:


- SwiftUI iOS client
- Jetpack Compose Android client
- Compose for Desktop client (early version)
- Compose for Wear OS client - contributed by https://github.com/yschimke
- [Apollo GraphQL](https://github.com/apollographql/apollo-kotlin) based Kotlin Multiplatform shared code
- GraphQL backend


Related posts:
* [Swift/Kotlin ViewModel alignment in a Kotlin Multiplatform project](https://johnoreilly.dev/posts/swift-kotlin-viewmodel-kmm-comparison/)
* [Using KMM-ViewModel library to share VM between iOS and Android](https://johnoreilly.dev/posts/kmm-viewmodel/)
* [Consuming Compose for iOS in a SwiftUI application](https://johnoreilly.dev/posts/swiftui-compose-ios/)


### Building
Use Android Studio/IntelliJ to build/run Android client.
Requires XCode 14 or later for iOS client.

### Screenshots 

<img width="1054" alt="Screenshot 2022-10-19 at 13 44 57" src="https://user-images.githubusercontent.com/6302/196694566-20a8edc0-a120-4305-8ff2-d18d543f47a2.png">


![Screenshot_20230324_091534](https://user-images.githubusercontent.com/6302/227476725-edd577f5-2abd-4660-a777-decef84fbb9b.png)


<img width="453" alt="Screenshot 2023-01-18 at 18 52 58" src="https://user-images.githubusercontent.com/6302/213269228-50154ff5-a1c1-4da6-be26-1c4a1bf20337.png">

### API

The API is available at https://confetti-app.dev/graphiql
You can query the list of conferences with `conferences.id` and chose what conference to query by passing a `"conference"` HTTP header 
