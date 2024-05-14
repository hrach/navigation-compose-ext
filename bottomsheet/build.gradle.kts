@file:Suppress("UnstableApiUsage")

plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlinx.binary-compatibility-validator")
	id("com.vanniktech.maven.publish.base")
	id("org.jmailen.kotlinter")
}

android {
	namespace = "dev.hrach.navigation.bottomsheet"

	compileSdk = libs.versions.compileSdk.get().toInt()

	defaultConfig {
		minSdk = libs.versions.minSdk.get().toInt()
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildFeatures {
		compose = true
		buildConfig = false
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}

	composeOptions {
		kotlinCompilerExtensionVersion = libs.compose.compiler.get().version
	}

	kotlinOptions {
		freeCompilerArgs = freeCompilerArgs.toMutableList().apply {
			add("-Xexplicit-api=strict")
		}.toList()
	}

	lint {
		disable.add("GradleDependency")
		abortOnError = true
		warningsAsErrors = true
	}
}

kotlinter {
	reporters = arrayOf("json")
}

dependencies {
	implementation(libs.appcompat)
	implementation(libs.navigation.compose)
	implementation(libs.compose.material3)

	testImplementation(libs.junit)
}
