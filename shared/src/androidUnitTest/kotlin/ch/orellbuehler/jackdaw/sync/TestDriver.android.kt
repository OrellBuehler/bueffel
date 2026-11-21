package ch.orellbuehler.jackdaw.sync

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ch.orellbuehler.jackdaw.db.JackdawDatabase

internal actual fun newTestDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    JackdawDatabase.Schema.create(driver)
    return driver
}
