// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://repo.repsy.io/mvn/joreilly/confetti/Confetti/shared-kmmbridge/0.8.5/shared-kmmbridge-0.8.5.zip"
let remoteKotlinChecksum = "8bab10063985f52928de5d64d2b72b20af28570d2a445bad040740397aee14eb"
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