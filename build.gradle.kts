import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
	id("org.jetbrains.kotlin.android") version "2.0.21" apply false
	id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
	id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
	id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.16.3" apply false
	id("org.jmailen.kotlinter") version "5.0.1" apply false
	id("com.android.application") version "8.8.2" apply false
	id("com.vanniktech.maven.publish") version "0.30.0" apply false
	id("com.gradleup.nmcp") version "0.2.1"
}

subprojects {
	tasks.withType<KotlinJvmCompile>().configureEach {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_1_8)
			allWarningsAsErrors.set(true)
		}
	}

	val signingPropsFile = rootProject.file("release/signing.properties")
	if (signingPropsFile.exists()) {
		val localProperties = Properties()
		with(signingPropsFile.inputStream()) {
			localProperties.load(this)
		}
		localProperties.forEach { key, value ->
			if (key == "signing.secretKeyRingFile") {
				project.ext.set(key as String, rootProject.file(value).absolutePath)
			} else {
				project.ext.set(key as String, value)
			}
		}
	}
}

nmcp {
	publishAggregation {
		project(":bottomsheet")
		project(":modalsheet")
		project(":results")

		val signingPropsFile = rootProject.file("release/signing.properties")
		if (!signingPropsFile.exists()) return@publishAggregation

		val localProperties = Properties()
		with(signingPropsFile.inputStream()) {
			localProperties.load(this)
		}

		username = localProperties.getProperty("centralSonatypeUsername")
		password = localProperties.getProperty("centralSonatypePassword")
		publicationType = "AUTOMATIC"
	}
}
