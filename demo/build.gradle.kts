@file:Suppress("UnstableApiUsage")

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.plugin.compose")
	id("org.jetbrains.kotlin.plugin.serialization")
	id("org.jmailen.kotlinter")
}

android {
	namespace = "dev.hrach.navigation.demo"

	compileSdk = libs.versions.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "dev.hrach.navigation.demo"
		minSdk = 26
		targetSdk = 35
		versionName = "1.0.0"
		versionCode = 1
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildFeatures {
		compose = true
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}

	buildFeatures {
		compose = true
		buildConfig = false
		aidl = false
		renderScript = false
		resValues = false
		shaders = false
	}

	kotlinOptions {
		freeCompilerArgs = freeCompilerArgs.toMutableList().apply {
			add("-Xexplicit-api=strict")
		}.toList()
	}

	lint {
		disable.add("GradleDependency")
		disable.add("OldTargetApi")
		abortOnError = true
		warningsAsErrors = true
	}
}

kotlinter {
	reporters = arrayOf("json")
}

dependencies {
	implementation(projects.bottomsheet)
	implementation(projects.modalsheet)
	implementation(projects.results)

	implementation(libs.kotlin.serialization.core)
	implementation(libs.compose.material3)
	implementation(libs.navigation.compose)
}
