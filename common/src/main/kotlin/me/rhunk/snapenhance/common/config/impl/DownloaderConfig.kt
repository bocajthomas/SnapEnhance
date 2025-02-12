package me.rhunk.snapenhance.common.config.impl

import me.rhunk.snapenhance.common.config.ConfigContainer
import me.rhunk.snapenhance.common.config.ConfigFlag
import me.rhunk.snapenhance.common.config.FeatureNotice
import me.rhunk.snapenhance.common.config.PropertyValue

class DownloaderConfig : ConfigContainer() {
    inner class FFMpegOptions : ConfigContainer() {
        val threads = integer("threads", 4) // Bump Default Value to 4 Tested on Pixel 5 (Qualcomm Snapdragon 765G) Had no lag
        val preset = unique("preset", "ultrafast", "superfast", "veryfast", "faster", "fast", "medium", "slow", "slower", "veryslow") {
            addFlags(ConfigFlag.NO_TRANSLATE)
        }
        val constantRateFactor = integer("constant_rate_factor", 30)
        val videoBitrate = integer("video_bitrate", 5000)
        val audioBitrate = integer("audio_bitrate", 128)
        val customVideoCodec = string("custom_video_codec") { addFlags(ConfigFlag.NO_TRANSLATE) }
        val customAudioCodec = string("custom_audio_codec") { addFlags(ConfigFlag.NO_TRANSLATE) }
    }

    class LoggingOptions : ConfigContainer() {
        val logging = multiple("logging", "started", "success", "progress", "failure").apply {
            set(mutableListOf("success", "progress", "failure"))
        }
        val disappearingRate: PropertyValue<Int> =  integer("disappearing_rate", defaultValue = 1300) { inputCheck = { (it.toIntOrNull() ?: 1300) in 100..3000} }
    }

    val saveFolder = string("save_folder") { addFlags(ConfigFlag.FOLDER, ConfigFlag.SENSITIVE); requireRestart() }
    val autoDownloadSources = multiple("auto_download_sources",
        "friend_snaps",
        "friend_stories",
        "public_stories",
        "spotlight"
    )
    val preventSelfAutoDownload = boolean("prevent_self_auto_download")
    val pathFormat = multiple("path_format",
        "create_author_folder",
        "create_source_folder",
        "append_hash",
        "append_source",
        "append_username",
        "append_date_time",
    ).apply { set(mutableListOf("append_hash", "append_date_time", "append_type", "append_username")) }
    val allowDuplicate = boolean("allow_duplicate")
    val mergeOverlays = boolean("merge_overlays") { addNotices(FeatureNotice.UNSTABLE) }
    val forceImageFormat = unique("force_image_format", "jpg", "png", "webp") {
        addFlags(ConfigFlag.NO_TRANSLATE)
    }
    val forceVoiceNoteFormat = unique("force_voice_note_format", "aac", "mp3", "opus") {
        addFlags(ConfigFlag.NO_TRANSLATE)
    }
    val autoDownloadVoiceNotes = boolean("auto_download_voice_notes") { requireRestart(); addNotices(FeatureNotice.UNSTABLE) }
    val downloadProfilePictures = boolean("download_profile_pictures") { requireRestart() }
    val operaDownloadButton = boolean("opera_download_button") { requireRestart() }
    val downloadContextMenu = boolean("download_context_menu")
    val ffmpegOptions = container("ffmpeg_options", FFMpegOptions()) { addNotices(FeatureNotice.UNSTABLE) }
    val loggingOptions = container("logging_options", LoggingOptions())
    val customPathFormat = string("custom_path_format") { addNotices(FeatureNotice.UNSTABLE) }
}