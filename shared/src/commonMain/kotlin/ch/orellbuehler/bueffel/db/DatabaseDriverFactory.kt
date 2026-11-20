package ch.orellbuehler.bueffel.db

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    internal fun createDriver(): SqlDriver
}
