// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo.repsy.io/mvn/joreilly/confetti/Confetti/shared-kmmbridge/0.8.9/shared-kmmbridge-0.8.9.zip"
let remoteKotlinChecksum = "2f5a68fc8cabc0780e0eb4de360ef25e4a64f62dba5f9049e42097d9f992506c"
let packageName = "ConfettiKit"
// END KMMBRIDGE BLOCK

let package = Package(
    name: packageName,
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: packageName,
            targets: [packageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: packageName,
            url: remoteKotlinUrl,
            checksum: remoteKotlinChecksum
        )
        ,
    ]
)