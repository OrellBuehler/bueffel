package ch.orellbuehler.jackdaw.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.orellbuehler.jackdaw.R
import ch.orellbuehler.jackdaw.study.SessionState
import ch.orellbuehler.jackdaw.study.TrueFalseAnswer
import ch.orellbuehler.jackdaw.study.TrueFalseExercise
import ch.orellbuehler.jackdaw.ui.theme.jackdaw

@Composable
fun TrueFalseView(
    exercise: TrueFalseExercise,
    question: SessionState.Question?,
    feedback: SessionState.Feedback?,
    actions: StudyActions,
) {
    val haptics = LocalHapticFeedback.current
    val success = MaterialTheme.jackdaw
    val scheme = MaterialTheme.colorScheme
    ExerciseFrame(
        hint = question?.hint,
        isRetry = question?.isRetry == true,
        bottom = {
            if (feedback == null) {
                BottomPanel(Tone.Neutral) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnswerButton(
                            label = stringResource(R.string.tf_true),
                            icon = Icons.Rounded.Check,
                            container = success.successContainer,
                            content = success.onSuccessContainer,
                            modifier = Modifier.weight(1f),
                        ) {
                            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            actions.submit(TrueFalseAnswer(true))
                        }
                        AnswerButton(
                            label = stringResource(R.string.tf_false),
                            icon = Icons.Rounded.Close,
                            container = scheme.errorContainer,
                            content = scheme.onErrorContainer,
                            modifier = Modifier.weight(1f),
                        ) {
                            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            actions.submit(TrueFalseAnswer(false))
                        }
                    }
                }
            } else {
                VerdictPanel(feedback, actions.next)
            }
        },
    ) {
        PromptText(exercise.statement)
        if (feedback != null) {
            Text(
                stringResource(if (feedback.statementIsTrue) R.string.tf_is_true else R.string.tf_is_false),
                style = MaterialTheme.typography.titleMedium,
                color = if (feedback.statementIsTrue) success.success else scheme.error,
            )
            ExplanationCard(feedback.explanation)
        }
    }
}

@Composable
private fun AnswerButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
    ) {
        Icon(icon, null, Modifier.size(24.dp))
        androidx.compose.foundation.layout
            .Spacer(Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, maxLines = 1)
    }
}
