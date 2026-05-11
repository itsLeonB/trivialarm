package com.example.trivialarm.ui.screens

import android.text.Html
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.trivialarm.R
import com.example.trivialarm.ui.components.AnimatedAnswerCard
import com.example.trivialarm.ui.components.AnimatedQuestionContainer
import com.example.trivialarm.ui.components.PressableButton
import com.example.trivialarm.ui.components.PulsingAlarmBackground
import com.example.trivialarm.ui.viewmodel.AnswerFeedbackState
import com.example.trivialarm.ui.viewmodel.TriviaUiState
import com.example.trivialarm.ui.viewmodel.TriviaViewModel

@Composable
fun AlarmActiveScreen(
    alarmId: Int,
    viewModel: TriviaViewModel,
    onStopAlarm: () -> Unit
) {
    // Disable back button during alarm
    BackHandler(enabled = true) { }

    val state = viewModel.uiState

    LaunchedEffect(alarmId) {
        viewModel.initialize(alarmId)
    }

    LaunchedEffect(state) {
        if (state is TriviaUiState.Finished) {
            onStopAlarm()
        }
    }

    PulsingAlarmBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = stringResource(R.string.alarm_active),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            when (state) {
                is TriviaUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.fetching_trivia),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                is TriviaUiState.Error -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.errorContainer,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PressableButton(onClick = { viewModel.retry() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
                is TriviaUiState.Success -> {
                    TriviaContent(state, viewModel)
                }
                is TriviaUiState.Finished -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.well_done),
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.TriviaContent(
    state: TriviaUiState.Success,
    viewModel: TriviaViewModel
) {
    val progress = (state.correctCount.toFloat() / state.goalCount.toFloat()).coerceIn(0f, 1f)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Progress: ${state.correctCount} / ${state.goalCount}",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }

    Spacer(modifier = Modifier.height(48.dp))

    AnimatedQuestionContainer(targetState = state.currentQuestionIndex) { index ->
        val question = state.questions[index]
        val decodedQuestion = Html.fromHtml(question.question, Html.FROM_HTML_MODE_LEGACY).toString()
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = decodedQuestion,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                state.shuffledOptions.forEach { option ->
                    val decodedOption = Html.fromHtml(option, Html.FROM_HTML_MODE_LEGACY).toString()
                    val isSelected = state.selectedAnswer == option
                    val isInteractionDisabled = state.feedbackState != AnswerFeedbackState.IDLE

                    AnimatedAnswerCard(
                        text = decodedOption,
                        state = state.feedbackState,
                        isSelected = isSelected,
                        enabled = !isInteractionDisabled,
                        onClick = { viewModel.submitAnswer(option) },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}
