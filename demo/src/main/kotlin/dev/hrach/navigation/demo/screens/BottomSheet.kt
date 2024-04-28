package dev.hrach.navigation.demo.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
internal fun BottomSheet(navController: NavController) {
	Column(Modifier.padding(horizontal = 16.dp)) {
		Text("This is a bottomsheet")
		var value by rememberSaveable { mutableStateOf("") }
		OutlinedTextField(value = value, onValueChange = { value = it })
		OutlinedButton(onClick = { navController.popBackStack() }, Modifier.fillMaxWidth()) {
			Text("Close")
		}
	}
}
