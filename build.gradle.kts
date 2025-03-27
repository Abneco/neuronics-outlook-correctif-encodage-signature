import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.10"
    application
}

group = "be.neuronics"
version = "2025-1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib-jdk8
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.20")

    // logging
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.24.0")
    implementation("org.apache.logging.log4j:log4j-api:2.24.0")
    implementation("org.apache.logging.log4j:log4j-core:2.24.0")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

    // https://mvnrepository.com/artifact/org.fusesource.jansi/jansi
    implementation("org.fusesource.jansi:jansi:2.4.1")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}
kotlin {
    jvmToolchain(21)
}
application {
    mainClass.set("be.neuronics.correctif_signature_outlook.Main")
}
tasks.jar {
    // Définit la classe principale de l'application
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    // Inclut les dépendances du classpath d'exécution
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        // Exclut certains fichiers META-INF pour éviter les problèmes de sécurité ou de signature
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }

    // Stratégie pour gérer les fichiers en double dans l'archive
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Activation de l'extension Zip64 pour supporter plus de 65535 entrées
    isZip64 = true
}
