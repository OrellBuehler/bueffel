package ch.orellbuehler.bueffel.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    internal actual fun createDriver(): SqlDriver = NativeSqliteDriver(BueffelDatabase.Schema, "bueffel.db")
}
