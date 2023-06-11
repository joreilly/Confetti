// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://maven.pkg.github.com/joreilly/Confetti/Confetti/shared-kmmbridge/0.8.4/shared-kmmbridge-0.8.4.zip"
let remoteKotlinChecksum = "e7fdafdb1fcc4420f2a371f1637c28962268dee8ba34f5df6a7c0152e4cae359"
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