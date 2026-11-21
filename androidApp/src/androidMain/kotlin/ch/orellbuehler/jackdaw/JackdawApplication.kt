package ch.orellbuehler.jackdaw

import android.app.Application
import ch.orellbuehler.jackdaw.db.DatabaseDriverFactory

class JackdawApplication : Application() {
    val sdk: JackdawSdk by lazy { JackdawSdk(DatabaseDriverFactory(this)) }
}
