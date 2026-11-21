package ch.orellbuehler.jackdaw.sync

import app.cash.sqldelight.db.SqlDriver

internal expect fun newTestDriver(): SqlDriver
