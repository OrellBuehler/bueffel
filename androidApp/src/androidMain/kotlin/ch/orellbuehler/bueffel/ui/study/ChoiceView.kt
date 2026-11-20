package ch.orellbuehler.bueffel.ui.study

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ch.orellbuehler.bueffel.R
import ch.orellbuehler.bueffel.format.RichText
import ch.orellbuehler.bueffel.study.ChoiceAnswer
import ch.orellbuehler.bueffel.study.ChoiceExercise
import ch.orellbuehler.bueffel.study.SessionState
import ch.orellbuehler.bueffel.ui.components.RichTextView
import ch.orellbuehler.bueffel.ui.theme.bueffel

private enum class OptionState {
    Idle,
    Selected,
    Correct,
    Missed,
    Wrong,
    Dimmed,
}

@Composable
fun ChoiceView(
    exercise: ChoiceExercise,
    question: SessionState.Question?,
    feedback: SessionState.Feedback?,
    step: Int,
    actions: StudyActions,
) {
    val selected = rememberStringList(step) { emptyList() }
    val haptics = LocalHapticFeedback.current
    val results = feedback?.choiceResults.orEmpty().associateBy { it.id }
    ExerciseFrame(
        hint = question?.hint,
        isRetry = question?.isRetry == true,
        bottom = {
            if (feedback == null) {
                BottomPanel(Tone.Neutral) {
                    PrimaryAction(stringResource(R.string.choice_check), enabled = selected.isNotEmpty()) {
                        actions.submit(ChoiceAnswer(selected.toList()))
                    }
                }
            } else {
                VerdictPanel(feedback, actions.next)
            }
        },
    ) {
        PromptText(exercise.question)
        Text(
            stringResource(if (exercise.multiSelect) R.string.choice_multi else R.string.choice_single),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            exercise.options.forEach { option ->
                val result = results[option.id]
                val state =
                    when {
                        feedback == null -> if (option.id in selected) OptionState.Selected else OptionState.Idle
                        result == null -> OptionState.Dimmed
                        result.correct && result.selected -> OptionState.Correct
                        result.correct -> OptionState.Missed
                        result.selected -> OptionState.Wrong
                        else -> OptionState.Dimmed
                    }
                OptionCard(
                    text = option.text,
                    state = state,
                    multi = exercise.multiSelect,
                    explanation = result?.takeIf { feedback != null }?.explanation,
                    enabled = feedback == null,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        if (exercise.multiSelect) {
                            if (option.id in selected) selected.remove(option.id) else selected.add(option.id)
                        } else {
                            selected.clear()
                            selected.add(option.id)
                        }
                    },
                )
            }
        }
        if (feedback != null) ExplanationCard(feedback.explanation)
    }
}

@Composable
private fun OptionCard(
    text: RichText,
    state: OptionState,
    multi: Boolean,
    explanation: RichText?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val success = MaterialTheme.bueffel
    val container by animateColorAsState(
        when (state) {
            OptionState.Idle -> scheme.surfaceContainerLowest
            OptionState.Selected -> scheme.primaryContainer
            OptionState.Correct -> success.successContainer
            OptionState.Missed -> scheme.surfaceContainerLowest
            OptionState.Wrong -> scheme.errorContainer
            OptionState.Dimmed -> scheme.surfaceContainerLow
        },
        label = "option-container",
    )
    val borderColor =
        when (state) {
            OptionState.Idle -> scheme.outlineVariant
            OptionState.Selected -> scheme.primary
            OptionState.Correct, OptionState.Missed -> success.success
            OptionState.Wrong -> scheme.error
            OptionState.Dimmed -> scheme.outlineVariant.copy(alpha = 0.5f)
        }
    val content =
        when (state) {
            OptionState.Correct -> success.onSuccessContainer
            OptionState.Wrong -> scheme.onErrorContainer
            OptionState.Selected -> scheme.onPrimaryContainer
            OptionState.Dimmed -> scheme.onSurface.copy(alpha = 0.6f)
            else -> scheme.onSurface
        }
    val icon: ImageVector
    val iconTint: Color
    when (state) {
        OptionState.Correct -> {
            icon = Icons.Rounded.CheckCircle
            iconTint = success.success
        }
        OptionState.Missed -> {
            icon = Icons.Rounded.CheckCircle
            iconTint = success.success
        }
        OptionState.Wrong -> {
            icon = Icons.Rounded.Cancel
            iconTint = scheme.error
        }
        OptionState.Selected -> {
            icon = if (multi) Icons.Rounded.CheckBox else Icons.Rounded.RadioButtonChecked
            iconTint = scheme.primary
        }
        else -> {
            icon = if (multi) Icons.Rounded.CheckBoxOutlineBlank else Icons.Rounded.RadioButtonUnchecked
            iconTint = scheme.outline
        }
    }
    val selectedForSemantics = state == OptionState.Selected || state == OptionState.Correct || state == OptionState.Wrong
    val semanticsModifier =
        if (multi) {
            Modifier.toggleable(selectedForSemantics, enabled = enabled, role = Role.Checkbox, onValueChange = { onClick() })
        } else {
            Modifier.selectable(selectedForSemantics, enabled = enabled, role = Role.RadioButton, onClick = onClick)
        }
    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize().then(semanticsModifier),
        shape = RoundedCornerShape(20.dp),
        color = container,
        border = BorderStroke(if (state == OptionState.Idle || state == OptionState.Dimmed) 1.dp else 2.dp, borderColor),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(icon, null, Modifier.size(24.dp), tint = iconTint)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RichTextView(text, style = MaterialTheme.typography.bodyLarge, color = content)
                if (explanation != null && !explanation.isEmpty) {
                    RichTextView(explanation, style = MaterialTheme.typography.bodySmall, color = content.copy(alpha = 0.85f))
                }
            }
        }
    }
}
