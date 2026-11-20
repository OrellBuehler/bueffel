package ch.orellbuehler.bueffel

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ch.orellbuehler.bueffel.data.ContentResolverTextReader
import ch.orellbuehler.bueffel.ui.BueffelApp
import ch.orellbuehler.bueffel.ui.addpack.ImportViewModel
import ch.orellbuehler.bueffel.ui.settings.AppViewModel

class MainActivity : ComponentActivity() {
    private val sdk get() = (application as BueffelApplication).sdk

    private val appViewModel: AppViewModel by viewModels { factory { AppViewModel(sdk) } }
    private val importViewModel: ImportViewModel by viewModels {
        factory { ImportViewModel(sdk, ContentResolverTextReader(applicationContext.contentResolver)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BueffelApp(appViewModel, importViewModel) }
        if (savedInstanceState == null) handleIncoming(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncoming(intent)
    }

    override fun onResume() {
        super.onResume()
        appViewModel.refresh()
    }

    private fun handleIncoming(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_VIEW -> intent.data?.let(importViewModel::openUri)
            Intent.ACTION_SEND -> {
                val stream = intent.streamUri()
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                when {
                    stream != null -> importViewModel.openUri(stream)
                    !text.isNullOrBlank() -> importViewModel.openText(text)
                }
            }
        }
    }

    private fun Intent.streamUri(): Uri? =
        if (Build.VERSION.SDK_INT >= 33) {
            getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(Intent.EXTRA_STREAM)
        }

    private inline fun <reified VM : ViewModel> factory(crossinline create: () -> VM) =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
        }
}
