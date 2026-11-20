package ch.orellbuehler.bueffel

import android.app.Application
import ch.orellbuehler.bueffel.db.DatabaseDriverFactory

class BueffelApplication : Application() {
    val sdk: BueffelSdk by lazy { BueffelSdk(DatabaseDriverFactory(this)) }
}
