package com.example.trivialarm.ui.screens

import android.text.Html
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.trivialarm.ui.viewmodel.TriviaUiState
import com.example.trivialarm.ui.viewmodel.TriviaViewModel

import androidx.compose.ui.res.stringResource
import com.example.trivialarm.R

@Composable
fun AlarmActiveScreen(
    viewModel: TriviaViewModel,
    onStopAlarm: () -> Unit
) {
    // Disable back button during alarm
    BackHandler(enabled = true) { }

    val state = viewModel.uiState

    LaunchedEffect(state) {
        if (state is TriviaUiState.Finished) {
            onStopAlarm()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.alarm_active),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when (state) {
                is TriviaUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    Text(stringResource(R.string.fetching_trivia), color = MaterialTheme.colorScheme.onPrimary)
                }
                is TriviaUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.errorContainer,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.fetchQuestions() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
                is TriviaUiState.Success -> {
                    Text(
                        text = stringResource(R.string.correct_answers, state.correctCount),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val decodedQuestion = Html.fromHtml(
                                state.questions[state.currentQuestionIndex].question,
                                Html.FROM_HTML_MODE_LEGACY
                            ).toString()
                            
                            Text(
                                text = decodedQuestion,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            
                            state.shuffledOptions.forEach { option ->
                                val decodedOption = Html.fromHtml(option, Html.FROM_HTML_MODE_LEGACY).toString()
                                Button(
                                    onClick = { viewModel.submitAnswer(option) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text(
                                        text = decodedOption,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                is TriviaUiState.Finished -> {
                    Text(
                        text = stringResource(R.string.well_done),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
