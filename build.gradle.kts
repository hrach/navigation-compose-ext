import java.util.Properties
import kotlin.math.sign
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
	id("org.jetbrains.kotlin.android") version "1.9.23" apply false
	id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" apply false
	id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.14.0" apply false
	id("org.jmailen.kotlinter") version "4.3.0" apply false
	id("com.android.application") version "8.3.0" apply false
	id("com.vanniktech.maven.publish") version "0.27.0" apply false
	id("com.gradleup.nmcp") version "0.0.7"
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
