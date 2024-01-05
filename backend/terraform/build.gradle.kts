val file = layout.buildDirectory.file("service-account.json").get().asFile

val createGcpCredentials = tasks.register("createGcpCredentials") {
    doLast {
        file.parentFile.mkdirs()
        file.writeText(gcpServiceAccountJson)
    }
}
tasks.register("apply", Exec::class.java) {
    dependsOn(createGcpCredentials)
    environment("GOOGLE_APPLICATION_CREDENTIALS", file.absolutePath)
    commandLine("terraform", "apply", "-auto-approve")
}