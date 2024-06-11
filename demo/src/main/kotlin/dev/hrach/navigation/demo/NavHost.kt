package dev.hrach.navigation.demo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.hrach.navigation.bottomsheet.BottomSheetHost
import dev.hrach.navigation.bottomsheet.BottomSheetNavigator
import dev.hrach.navigation.bottomsheet.bottomSheet
import dev.hrach.navigation.demo.screens.BottomSheet
import dev.hrach.navigation.demo.screens.Home
import dev.hrach.navigation.demo.screens.List
import dev.hrach.navigation.demo.screens.Modal1
import dev.hrach.navigation.demo.screens.Modal2
import dev.hrach.navigation.demo.screens.Profile
import dev.hrach.navigation.modalsheet.ModalSheetHost
import dev.hrach.navigation.modalsheet.ModalSheetNavigator
import dev.hrach.navigation.modalsheet.modalSheet
import java.time.LocalDate
import kotlin.reflect.typeOf

@Composable
internal fun NavHost(
	navController: NavHostController,
	modalSheetNavigator: ModalSheetNavigator,
	bottomSheetNavigator: BottomSheetNavigator,
) {
	NavHost(
		navController = navController,
		startDestination = Destinations.Home,
	) {
		composable<Destinations.Home> { Home(navController) }
		composable<Destinations.List> { List() }
		composable<Destinations.Profile> { Profile() }
		modalSheet<Destinations.Modal1> { Modal1(navController) }
		modalSheet<Destinations.Modal2> { Modal2() }
		bottomSheet<Destinations.BottomSheet> { BottomSheet(navController) }
//		composable<Destinations.IssueNested>(
//			typeMap = mapOf(
//				typeOf<Destinations.IssueNested.Nested>() to NestedNavType()
//			)
//		) {  }
		composable<Destinations.IssueEnum>(
			typeMap = mapOf(
				typeOf<Destinations.IssueEnum.Priority>() to NavType.EnumType(Destinations.IssueEnum.Priority::class.java)
			)
		) {  }
//		composable<Destinations.IssueEncoding>(
//			typeMap = mapOf(
//				typeOf<Destinations.IssueEncoding.Nested>() to Destinations.NestedNavType(),
//			)
//		) {  }
//		composable<Destinations.IssueObjectThenClass> {}
//		composable<Destinations.IssueExternalType>(
//			typeMap = mapOf(
//				typeOf<LocalDate>() to Destinations.CustomLocalDateNavType()
//			)
//		) {}
//		composable<Destinations.IssueEmptyString>() {}
	}
	ModalSheetHost(modalSheetNavigator)
	BottomSheetHost(bottomSheetNavigator)
}
