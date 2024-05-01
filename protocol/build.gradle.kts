plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    maven { setUrl("https://cache-redirector.jetbrains.com/maven-central") }
}

dependencies {
    val rdgenVersion = libs.plugins.rdgen.map { it.version }
    implementation(rdgenVersion.map { "com.jetbrains.rd:rd-gen:$it" })
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(
        project(
            mapOf(
                "path" to ":",
                "configuration" to "riderModel"
            )
        )
    )
}
