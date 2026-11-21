package ch.orellbuehler.jackdaw.db

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    internal fun createDriver(): SqlDriver
}
