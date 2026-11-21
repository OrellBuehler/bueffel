package ch.orellbuehler.jackdaw.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(
    private val context: Context,
) {
    internal actual fun createDriver(): SqlDriver = AndroidSqliteDriver(JackdawDatabase.Schema, context, "jackdaw.db")
}
