Extensions for Navigation Compose
=================================

[![CI Build](https://img.shields.io/github/actions/workflow/status/hrach/navigation-compose/build.yml?branch=main)](https://github.com/hrach/navigation-compose/actions/workflows/build.yml)
[![GitHub release](https://img.shields.io/github/v/release/hrach/navigation-compose)](https://github.com/hrach/navigation-compose/releases)

See `demo` module.

Use Maven Central and these dependencies:

```kotlin
dependencies {
	implementation("dev.hrach.navigation:bottomsheet:<version>")
	implementation("dev.hrach.navigation:modalsheet:<version>")
}
```

Components:

- **BottomSheet** - Connects the official Material 3 BottomSheet with Jetpack Navigation.
- **ModalSheet** - A custom destination type for Jetpack Navigation that brings fullscreen content with modal animation.

Quick setup:

```kotlin
val modalSheetNavigator = remember { ModalSheetNavigator() }
val bottomSheetNavigator = remember { BottomSheetNavigator() }
val navController = rememberNavController(modalSheetNavigator, bottomSheetNavigator)

NavHost(
    navController = navController,
    startDestination = Destinations.Home,
) {
    modalSheet<Destinations.Modal> { Modal1(navController) }
    bottomSheet<Destinations.BottomSheet> { BottomSheet(navController) }
}
ModalSheetHost(modalSheetNavigator)
BottomSheetHost(bottomSheetNavigator)
```
