plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.23.7")
}

kotlin {
    jvmToolchain(17)
}
