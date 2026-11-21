package ch.orellbuehler.jackdaw.sync

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import ch.orellbuehler.jackdaw.db.JackdawDatabase

internal actual fun newTestDriver(): SqlDriver = inMemoryDriver(JackdawDatabase.Schema)
