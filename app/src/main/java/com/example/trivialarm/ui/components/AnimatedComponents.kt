package com.example.trivialarm.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.trivialarm.ui.viewmodel.AnswerFeedbackState

@Composable
fun PulsingAlarmBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingBackground")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        color1.copy(alpha = alpha),
                        color2.copy(alpha = alpha * 0.8f)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun AnimatedAnswerCard(
    text: String,
    state: AnswerFeedbackState,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected && state == AnswerFeedbackState.CORRECT -> Color(0xFF4CAF50)
            isSelected && state == AnswerFeedbackState.WRONG -> MaterialTheme.colorScheme.error
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "backgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected && (state == AnswerFeedbackState.CORRECT || state == AnswerFeedbackState.WRONG) -> Color.White
            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        },
        label = "contentColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        label = "elevation"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .bounceClick(interactionSource)
            .shake(enabled = isSelected && state == AnswerFeedbackState.WRONG),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = isSelected && state == AnswerFeedbackState.CORRECT,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            AnimatedVisibility(
                visible = isSelected && state == AnswerFeedbackState.WRONG,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PressableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Button(
        onClick = onClick,
        modifier = modifier.bounceClick(interactionSource),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        content = content
    )
}

@Composable
fun AnimatedQuestionContainer(
    targetState: Int,
    content: @Composable (Int) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                slideOutHorizontally { width -> -width } + fadeOut()
            ).using(
                SizeTransform(clip = false)
            )
        },
        label = "questionTransition"
    ) { state ->
        content(state)
    }
}
