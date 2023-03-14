tasks.register("uploadLandingPage") {
    dependsOn(rootProject.tasks.named("setupCredentials"))
    doLast {
        uploadLandingPage()
    }
}