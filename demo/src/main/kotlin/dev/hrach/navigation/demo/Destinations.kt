package dev.hrach.navigation.demo

import kotlinx.serialization.Serializable

internal object Destinations {
	@Serializable
	data object Home

	@Serializable
	data object List

	@Serializable
	data object Profile

	@Serializable
	data object Modal1

	@Serializable
	data object Modal2

	@Serializable
	data object BottomSheet {
		@Serializable
		data class Result(
			val id: Int,
		)
	}
}
