Extensions for Navigation Compose
=================================

[![CI Build](https://img.shields.io/github/actions/workflow/status/hrach/navigation-compose-ext/build.yml?branch=main)](https://github.com/hrach/navigation-compose-ext/actions/workflows/build.yml)
[![GitHub release](https://img.shields.io/github/v/release/hrach/navigation-compose-ext)](https://github.com/hrach/navigation-compose-ext/releases)

See `demo` module.

Use Maven Central and these dependencies:

```kotlin
dependencies {
	implementation("dev.hrach.navigation:bottomsheet:<version>")
	implementation("dev.hrach.navigation:modalsheet:<version>")
	implementation("dev.hrach.navigation:results:<version>")
}
```

Components:

- **BottomSheet** - Connects the official Material 3 BottomSheet with Jetpack Navigation.
- **ModalSheet** - A custom destination type for Jetpack Navigation that brings fullscreen content with modal animation.
- **Results** - Passing a result simply between destinations.

Quick setup:

```kotlin
val modalSheetNavigator = remember { ModalSheetNavigator() }
val bottomSheetNavigator = remember { BottomSheetNavigator() }
val navController = rememberNavController(modalSheetNavigator, bottomSheetNavigator)

NavHost(
	navController = navController,
	startDestination = Destinations.Home,
) {
	composable<Destinations.Home> { Home(navController) }
	modalSheet<Destinations.Modal> { Modal(navController) }
	bottomSheet<Destinations.BottomSheet> { BottomSheet(navController) }
}
ModalSheetHost(modalSheetNavigator)
BottomSheetHost(bottomSheetNavigator)
```

Results sharing:

```kotlin
object Destinations {
	@Serializable
	data object BottomSheet {
		@Serializable
		data class Result(
			val id: Int,
		)
	}
}

@Composable
fun Home(navController: NavController) {
	NavigationResultEffect<Destinations.BottomSheet.Result>(
		backStackEntry = remember(navController) { navController.getBackStackEntry<Destinations.Home>() },
		navController = navController,
	) { result ->
		// process result -
	}
}

@Composable
fun BottomSheet(navController: NavController) {
	OutlineButton(onClick = { navController.setResult(Destinations.BottomSheet.Result(42)) }) {
		Text("Close")
	}
}
```
