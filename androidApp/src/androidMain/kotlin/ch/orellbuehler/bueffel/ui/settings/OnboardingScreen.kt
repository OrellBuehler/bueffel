package ch.orellbuehler.bueffel.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.orellbuehler.bueffel.R
import ch.orellbuehler.bueffel.ui.addpack.ImportViewModel
import ch.orellbuehler.bueffel.ui.addpack.SampleRow
import ch.orellbuehler.bueffel.ui.components.Mascot
import ch.orellbuehler.bueffel.ui.components.MascotMood
import ch.orellbuehler.bueffel.ui.theme.Amber
import ch.orellbuehler.bueffel.ui.theme.Violet
import kotlinx.coroutines.launch

private const val PAGES = 3

@Composable
fun OnboardingScreen(
    importViewModel: ImportViewModel,
    onFinish: () -> Unit,
) {
    val pager = rememberPagerState { PAGES }
    val scope = rememberCoroutineScope()
    val installed by importViewModel.installedIds.collectAsStateWithLifecycle()
    val last = pager.currentPage == PAGES - 1
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) {
                if (!last) {
                    TextButton(onClick = onFinish) { Text(stringResource(R.string.action_skip), maxLines = 1) }
                }
            }
        }
        HorizontalPager(pager, Modifier.weight(1f)) { page ->
            when (page) {
                0 ->
                    PageFrame(R.string.onb1_title, R.string.onb1_body) {
                        Box(
                            Modifier
                                .size(220.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Violet.copy(alpha = 0.16f), Amber.copy(alpha = 0.22f)))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Mascot(size = 150.dp, mood = MascotMood.Idle)
                        }
                    }
                1 ->
                    PageFrame(R.string.onb2_title, R.string.onb2_body) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Feature(Icons.Rounded.Lock, R.string.onb2_feature_device)
                            Feature(Icons.Rounded.WifiOff, R.string.onb2_feature_network)
                            Feature(Icons.Rounded.Public, R.string.onb2_feature_open)
                            Feature(Icons.Rounded.Schedule, R.string.onb2_feature_schedule)
                        }
                    }
                else ->
                    PageFrame(R.string.onb3_title, R.string.onb3_body) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            importViewModel.samples.forEach { sample ->
                                SampleRow(sample, installed = sample.id in installed, onAdd = { importViewModel.addSample(sample.id) })
                            }
                        }
                    }
            }
        }
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(PAGES) { index ->
                    val selected = pager.currentPage == index
                    val width by animateDpAsState(if (selected) 24.dp else 8.dp, label = "dot")
                    val color by animateColorAsState(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        label = "dot-color",
                    )
                    Box(
                        Modifier
                            .height(8.dp)
                            .size(width, 8.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
            }
            Button(
                onClick = {
                    if (last) onFinish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                },
            ) {
                AnimatedContent(last, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "cta") { isLast ->
                    Text(stringResource(if (isLast) R.string.action_get_started else R.string.action_next), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun PageFrame(
    title: Int,
    body: Int,
    art: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        art()
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(
                stringResource(body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Feature(
    icon: ImageVector,
    text: Int,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(stringResource(text), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}
