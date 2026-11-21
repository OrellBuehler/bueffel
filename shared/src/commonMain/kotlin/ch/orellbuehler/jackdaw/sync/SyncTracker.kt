package ch.orellbuehler.jackdaw.sync

import ch.orellbuehler.jackdaw.db.JackdawDatabase
import ch.orellbuehler.jackdaw.store.Env
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class SyncTracker(
    private val db: JackdawDatabase,
    private val env: Env,
) {
    val signal = MutableStateFlow(0L)

    fun touchPack(packId: String) = touch(SyncNames.packName(packId), SyncNames.PACK, packId)

    fun touchReview(id: String) = touch(SyncNames.reviewName(id), SyncNames.REVIEW, id)

    fun touchSetting(name: String) {
        if (name in SyncStore.SYNCED_SETTINGS) touch(SyncNames.settingName(name), SyncNames.SETTING, name)
    }

    fun announce() {
        signal.update { it + 1 }
    }

    private fun touch(
        recordName: String,
        kind: String,
        key: String,
    ) {
        if (!env.syncTracking) return
        val q = db.syncQueries
        q.touchInsert(recordName, kind, key)
        q.touchBump(recordName)
        signal.update { it + 1 }
    }
}
