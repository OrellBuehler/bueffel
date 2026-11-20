package ch.orellbuehler.bueffel.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseTest {
    @Test
    fun placeholderRoundTrip() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BueffelDatabase.Schema.create(driver)
        val queries = BueffelDatabase(driver).bueffelDatabaseQueries

        queries.insertPlaceholder("eins")

        assertEquals(listOf("eins"), queries.selectPlaceholders().executeAsList().map { it.name })
    }
}
