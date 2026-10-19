// The landing page is a Vite project (see package.json). pnpm must be on the PATH; it picks the
// exact version to run from the `packageManager` field.
val pnpmInstall = tasks.register("pnpmInstall", Exec::class.java) {
    inputs.files("package.json", "pnpm-lock.yaml")
    outputs.dir("node_modules")
    commandLine("pnpm", "install", "--frozen-lockfile")
}

val buildLandingPage = tasks.register("buildLandingPage", Exec::class.java) {
    dependsOn(pnpmInstall)
    inputs.files("package.json", "pnpm-lock.yaml", "vite.config.ts", "tsconfig.json")
    inputs.dir("public")
    inputs.dir("graphiql")
    outputs.dir("dist")
    commandLine("pnpm", "run", "build")
}

tasks.register("uploadLandingPage") {
    dependsOn(buildLandingPage)
    val baseDir = file("dist")
    val serviceAccountJson = provider { gcpServiceAccountJson }
    doLast {
        uploadLandingPage(baseDir, serviceAccountJson.get())
    }
}
