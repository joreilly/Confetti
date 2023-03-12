tasks.register("uploadLandingPage") {
    dependsOn(rootProject.tasks.named("setupGoogleServices"))
    doLast {
        uploadLandingPage()
    }
}