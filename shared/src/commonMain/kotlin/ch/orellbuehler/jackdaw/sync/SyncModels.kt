package ch.orellbuehler.jackdaw.sync

class SyncPackRecord(
    val recordName: String,
    val packId: String,
    val source: String?,
    val importedAt: Long,
    val contentAt: Long,
    val resetAt: Long,
    val deletedAt: Long,
    val version: Long,
)

class SyncReviewRecord(
    val recordName: String,
    val id: String,
    val ts: Long,
    val day: Long,
    val packId: String,
    val itemId: String,
    val facet: Long,
    val exercise: Long,
    val grade: Long,
    val correct: Boolean,
    val practice: Boolean,
    val reschedule: Boolean,
    val itemType: String,
    val version: Long,
)

class SyncSettingRecord(
    val recordName: String,
    val name: String,
    val value: String,
    val updatedAt: Long,
    val version: Long,
)

class SyncApplyResult(
    val needsUpload: List<String>,
    val notes: List<String>,
)

class SyncSignal(
    val sequence: Long,
)

object SyncNames {
    const val PACK = "pack"
    const val REVIEW = "review"
    const val SETTING = "setting"
    internal const val PACK_PREFIX = "pack."
    internal const val REVIEW_PREFIX = "log."
    internal const val SETTING_PREFIX = "setting."

    internal fun packName(packId: String): String = PACK_PREFIX + packId

    internal fun reviewName(id: String): String = REVIEW_PREFIX + id

    internal fun settingName(name: String): String = SETTING_PREFIX + name

    fun kindOf(recordName: String): String =
        when {
            recordName.startsWith(PACK_PREFIX) -> PACK
            recordName.startsWith(REVIEW_PREFIX) -> REVIEW
            recordName.startsWith(SETTING_PREFIX) -> SETTING
            else -> ""
        }
}
