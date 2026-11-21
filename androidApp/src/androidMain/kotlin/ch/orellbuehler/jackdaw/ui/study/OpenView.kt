package ch.orellbuehler.jackdaw.ui.study

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ch.orellbuehler.jackdaw.R
import ch.orellbuehler.jackdaw.study.OpenExercise
import ch.orellbuehler.jackdaw.study.SessionState
import ch.orellbuehler.jackdaw.ui.components.RichTextView
import ch.orellbuehler.jackdaw.ui.theme.cardSurface

@Composable
fun OpenView(
    exercise: OpenExercise,
    question: SessionState.Question?,
    feedback: SessionState.Feedback?,
    step: Int,
    actions: StudyActions,
) {
    val ticked = rememberStringList(step) { emptyList() }
    val haptics = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    ExerciseFrame(
        hint = question?.hint,
        isRetry = question?.isRetry == true,
        bottom = {
            if (feedback == null) {
                BottomPanel(Tone.Neutral) { PrimaryAction(stringResource(R.string.open_reveal), onClick = actions.reveal) }
            } else {
                GradePanel(suggested = feedback.suggestedGrade(ticked.size), onGrade = actions.selfGrade)
            }
        },
    ) {
        if (feedback == null) {
            PromptText(exercise.question)
        } else {
            RichTextView(exercise.question, style = MaterialTheme.typography.titleMedium, color = scheme.onSurfaceVariant)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(scheme.primaryContainer)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    stringResource(R.string.open_model_answer),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                if (feedback.correctAnswer.isEmpty) {
                    Text(
                        stringResource(R.string.open_no_answer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onPrimaryContainer,
                    )
                } else {
                    RichTextView(feedback.correctAnswer, style = MaterialTheme.typography.bodyLarge, color = scheme.onPrimaryContainer)
                }
            }
            if (feedback.keyPoints.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.open_key_points), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.open_tick), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
                feedback.keyPoints.forEachIndexed { index, point ->
                    val key = index.toString()
                    val checked = key in ticked
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.cardSurface)
                            .toggleable(checked, role = Role.Checkbox) {
                                haptics.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                                if (it) ticked.add(key) else ticked.remove(key)
                            }.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Checkbox(checked, onCheckedChange = null, modifier = Modifier.padding(8.dp))
                        RichTextView(
                            point,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                        )
                    }
                }
            }
            ExplanationCard(feedback.explanation)
        }
    }
}
