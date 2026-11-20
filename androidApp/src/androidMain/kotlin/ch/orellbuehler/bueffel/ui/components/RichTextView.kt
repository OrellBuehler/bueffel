package ch.orellbuehler.bueffel.ui.components

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import ch.orellbuehler.bueffel.format.RichText

fun RichText.toAnnotated(codeBackground: Color): AnnotatedString =
    buildAnnotatedString {
        spans.forEach { span ->
            val style =
                SpanStyle(
                    fontWeight = if (span.bold) FontWeight.Bold else null,
                    fontStyle = if (span.italic) FontStyle.Italic else null,
                    fontFamily = if (span.code) FontFamily.Monospace else null,
                    background = if (span.code) codeBackground else Color.Unspecified,
                )
            withStyle(style) { append(span.text) }
        }
    }

@Composable
fun rememberAnnotated(text: RichText): AnnotatedString {
    val background = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    return remember(text, background) { text.toAnnotated(background) }
}

@Composable
fun RichTextView(
    text: RichText,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    fontWeight: FontWeight? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
) {
    Text(
        text = rememberAnnotated(text),
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        inlineContent = inlineContent,
    )
}
