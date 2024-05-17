package dev.hrach.navigation.modalsheet

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowManager
import androidx.activity.BackEventCompat
import androidx.activity.ComponentDialog
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewRootForInspector
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.view.WindowCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ModalSheetDialog(
	onPredictiveBack: suspend (Flow<BackEventCompat>) -> Unit,
	securePolicy: SecureFlagPolicy,
	content: @Composable () -> Unit,
) {
	val view = LocalView.current
	val density = LocalDensity.current
	val layoutDirection = LocalLayoutDirection.current
	val composition = rememberCompositionContext()
	val currentContent by rememberUpdatedState(content)
	val dialogId = rememberSaveable { UUID.randomUUID() }
	val dialog = remember(view, density) {
		ModalSheetDialogLayout.ModalSheetDialogWrapper(
			onPredictiveBack,
			view,
			securePolicy,
			layoutDirection,
			density,
			dialogId,
		).apply {
			setContent(composition) {
				Box(
					Modifier.semantics { dialog() },
				) {
					currentContent()
				}
			}
		}
	}
	DisposableEffect(dialog) {
		dialog.show()
		onDispose {
			dialog.dismiss()
			dialog.disposeComposition()
		}
	}
	SideEffect {
		dialog.updateParameters(
			onPredictiveBack = onPredictiveBack,
			securePolicy = securePolicy,
			layoutDirection = layoutDirection,
		)
	}
}

// Fork of androidx.compose.ui.window.DialogLayout
// Additional parameters required for current predictive back implementation.
@Suppress("ViewConstructor")
private class ModalSheetDialogLayout(
	context: Context,
	override val window: Window,
	private var onPredictiveBack: suspend (Flow<BackEventCompat>) -> Unit,
) : AbstractComposeView(context), DialogWindowProvider {
	private var content: @Composable () -> Unit by mutableStateOf({})
	override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
		private set

	fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
		setParentCompositionContext(parent)
		this.content = content
		shouldCreateCompositionOnAttachedToWindow = true
		createComposition()
	}

	// Display width and height logic removed, size will always span fillMaxSize().
	@SuppressLint("NoCollectCallFound")
	@Composable
	override fun Content() {
		PredictiveBackHandler { onPredictiveBack(it) }
		content()
	}

	// Fork of androidx.compose.ui.window.DialogWrapper.
	// predictiveBackProgress and scope params added for predictive back implementation.
	// EdgeToEdgeFloatingDialogWindowTheme provided to allow theme to extend into status bar.
	class ModalSheetDialogWrapper(
		private var onPredictiveBack: suspend (Flow<BackEventCompat>) -> Unit,
		private val composeView: View,
		securePolicy: SecureFlagPolicy,
		layoutDirection: LayoutDirection,
		density: Density,
		dialogId: UUID,
	) : ComponentDialog(
		ContextThemeWrapper(
			composeView.context,
			R.style.EdgeToEdgeFloatingDialogWindowTheme,
		),
	), ViewRootForInspector {
		private val dialogLayout: ModalSheetDialogLayout

		// On systems older than Android S, there is a bug in the surface insets matrix math used by
		// elevation, so high values of maxSupportedElevation break accessibility services: b/232788477.
		private val maxSupportedElevation = 8.dp
		override val subCompositionView: AbstractComposeView get() = dialogLayout

		init {
			val window = window ?: error("Dialog has no window")
			window.requestFeature(Window.FEATURE_NO_TITLE)
			window.setBackgroundDrawableResource(android.R.color.transparent)
			WindowCompat.setDecorFitsSystemWindows(window, false)
			dialogLayout = ModalSheetDialogLayout(
				context,
				window,
				onPredictiveBack,
			).apply {
				// Set unique id for AbstractComposeView. This allows state restoration for the state
				// defined inside the Dialog via rememberSaveable()
				setTag(R.id.compose_view_saveable_id_tag, "Dialog:$dialogId")
				// Enable children to draw their shadow by not clipping them
				clipChildren = false
				// Allocate space for elevation
				with(density) { elevation = maxSupportedElevation.toPx() }
				// Simple outline to force window manager to allocate space for shadow.
				// Note that the outline affects clickable area for the dismiss listener. In case of
				// shapes like circle the area for dismiss might be to small (rectangular outline
				// consuming clicks outside of the circle).
				outlineProvider = object : ViewOutlineProvider() {
					override fun getOutline(view: View, result: Outline) {
						result.setRect(0, 0, view.width, view.height)
						// We set alpha to 0 to hide the view's shadow and let the composable to draw
						// its own shadow. This still enables us to get the extra space needed in the
						// surface.
						result.alpha = 0f
					}
				}
			}
			// Clipping logic removed because we are spanning edge to edge.
			setContentView(dialogLayout)
			dialogLayout.setViewTreeLifecycleOwner(composeView.findViewTreeLifecycleOwner())
			dialogLayout.setViewTreeViewModelStoreOwner(composeView.findViewTreeViewModelStoreOwner())
			dialogLayout.setViewTreeSavedStateRegistryOwner(
				composeView.findViewTreeSavedStateRegistryOwner(),
			)
			dialogLayout.setViewTreeOnBackPressedDispatcherOwner(this)
			// Initial setup
			updateParameters(onPredictiveBack, securePolicy, layoutDirection)
			WindowCompat.getInsetsController(window, window.decorView).apply {
				isAppearanceLightStatusBars = true
				isAppearanceLightNavigationBars = true
			}
		}

		private fun setLayoutDirection(layoutDirection: LayoutDirection) {
			dialogLayout.layoutDirection = when (layoutDirection) {
				LayoutDirection.Ltr -> android.util.LayoutDirection.LTR
				LayoutDirection.Rtl -> android.util.LayoutDirection.RTL
			}
		}

		fun setContent(parentComposition: CompositionContext, children: @Composable () -> Unit) {
			dialogLayout.setContent(parentComposition, children)
		}

		private fun setSecurePolicy(securePolicy: SecureFlagPolicy) {
			val secureFlagEnabled =
				securePolicy.shouldApplySecureFlag(composeView.isFlagSecureEnabled())
			window!!.setFlags(
				if (secureFlagEnabled) {
					WindowManager.LayoutParams.FLAG_SECURE
				} else {
					WindowManager.LayoutParams.FLAG_SECURE.inv()
				},
				WindowManager.LayoutParams.FLAG_SECURE,
			)
		}

		fun updateParameters(
			onPredictiveBack: suspend (Flow<BackEventCompat>) -> Unit,
			securePolicy: SecureFlagPolicy,
			layoutDirection: LayoutDirection,
		) {
			this.onPredictiveBack = onPredictiveBack
			setSecurePolicy(securePolicy)
			setLayoutDirection(layoutDirection)
			// Window flags to span parent window.
			window?.setLayout(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
			)
			window?.setSoftInputMode(
				if (Build.VERSION.SDK_INT >= 30) {
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
				} else {
					@Suppress("DEPRECATION")
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
				},
			)
		}

		fun disposeComposition() {
			dialogLayout.disposeComposition()
		}

		override fun cancel() {
			// Prevents the dialog from dismissing itself
			return
		}
	}
}

internal fun View.isFlagSecureEnabled(): Boolean {
	val windowParams = rootView.layoutParams as? WindowManager.LayoutParams
	if (windowParams != null) {
		return (windowParams.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
	}
	return false
}

// Taken from AndroidPopup.android.kt
private fun SecureFlagPolicy.shouldApplySecureFlag(isSecureFlagSetOnParent: Boolean): Boolean {
	return when (this) {
		SecureFlagPolicy.SecureOff -> false
		SecureFlagPolicy.SecureOn -> true
		SecureFlagPolicy.Inherit -> isSecureFlagSetOnParent
	}
}
