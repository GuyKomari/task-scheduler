import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.10"
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"
  kotlin("plugin.serialization") version "1.6.10"
}

group = "com"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.3.0"
val junitJupiterVersion = "5.8.2"

val mainVerticleName = "com.task_scheduler.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  // vertx
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-health-check")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-redis-client")
  implementation(kotlin("stdlib-jdk8"))

  // kotlin
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

  // logging
  implementation("org.apache.logging.log4j:log4j-core:2.17.2")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
  implementation("io.github.microutils:kotlin-logging:2.1.21")
  implementation("com.lmax:disruptor:3.4.4")

  // https://groups.google.com/g/vertx/c/Jny5enxwWhM
  implementation("io.netty:netty-resolver-dns-native-macos:4.1.72.Final:osx-x86_64")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("io.vertx:vertx-web-client")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "11"

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf(
    "run",
    mainVerticleName,
    "--launcher-class=$launcherClassName"
  )
}
