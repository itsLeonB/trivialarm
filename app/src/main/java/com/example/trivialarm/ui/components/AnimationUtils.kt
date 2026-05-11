package com.example.trivialarm.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.bounceClick(interactionSource: MutableInteractionSource) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceClick"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.shake(enabled: Boolean) = composed {
    if (!enabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val translation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeTranslation"
    )

    this.graphicsLayer {
        translationX = translation
    }
}
