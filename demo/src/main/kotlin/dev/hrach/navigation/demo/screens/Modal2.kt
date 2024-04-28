package dev.hrach.navigation.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun Modal2() {
	Column(
		Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface)
			.windowInsetsPadding(WindowInsets.systemBars),
	) {
		Text("Modal 2")
	}
}
