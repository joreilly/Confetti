// swift-tools-version:5.3
import PackageDescription

// BEGIN KMMBRIDGE VARIABLES BLOCK (do not edit)
let remoteKotlinUrl = "https://maven.pkg.github.com/joreilly/Confetti/Confetti/shared-kmmbridge/0.9.7/shared-kmmbridge-0.9.7.zip"
let remoteKotlinChecksum = "ed0b6e9ae1a0018e41ead73d0036bd9f3e90adc34efd085a47a391c2f31d32f5"
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