package ch.orellbuehler.jackdaw.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    internal actual fun createDriver(): SqlDriver = NativeSqliteDriver(JackdawDatabase.Schema, "jackdaw.db")
}
