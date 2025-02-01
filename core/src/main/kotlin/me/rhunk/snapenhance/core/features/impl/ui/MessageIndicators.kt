package me.rhunk.snapenhance.core.features.impl.ui

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.sharp.*
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.rhunk.snapenhance.common.data.ContentType
import me.rhunk.snapenhance.common.ui.createComposeView
import me.rhunk.snapenhance.common.ui.rememberAsyncMutableState
import me.rhunk.snapenhance.common.util.protobuf.ProtoReader
import me.rhunk.snapenhance.core.event.events.impl.BindViewEvent
import me.rhunk.snapenhance.core.features.Feature
import me.rhunk.snapenhance.core.ui.AppleLogo
import kotlin.random.Random

class MessageIndicators : Feature("Message Indicators") {
    override fun init() {
        val messageIndicatorsConfig = context.config.userInterface.messageIndicators.getNullable() ?: return
        if (messageIndicatorsConfig.isEmpty()) return

        val getIconStyle = context.config.userInterface.iconStyle.getNullable()
        val locationOnIconStyle = if (getIconStyle != null) {
            when (getIconStyle) {
                "outlined" -> Icons.Outlined.LocationOn
                "filled" -> Icons.Filled.LocationOn
                "sharp" -> Icons.Sharp.LocationOn
                "two-tone" -> Icons.TwoTone.LocationOn
                else -> {
                    context.log.warn("Error setting icon style $getIconStyle")
                    Icons.Rounded.LocationOn
                }
            }
        } else {
            Icons.Rounded.LocationOn
        }

        val laptopIconStyle = if (getIconStyle != null) {
            when (getIconStyle) {
                "outlined" -> Icons.Outlined.Laptop
                "filled" -> Icons.Filled.Laptop
                "sharp" -> Icons.Sharp.Laptop
                "two-tone" -> Icons.TwoTone.Laptop
                else -> {
                    context.log.warn("Error setting icon style $getIconStyle")
                    Icons.Rounded.Laptop
                }
            }
        } else {
            Icons.Rounded.Laptop
        }

        val androidIconStyle = if (getIconStyle != null) {
            when (getIconStyle) {
                "outlined" -> Icons.Outlined.Android
                "filled" -> Icons.Filled.Android
                "sharp" -> Icons.Sharp.Android
                "two-tone" -> Icons.TwoTone.Android
                else -> {
                    context.log.warn("Error setting icon style $getIconStyle")
                    Icons.Rounded.Android
                }
            }
        } else {
            Icons.Rounded.Android
        }

        val lockIconStyle = if (getIconStyle != null) {
            when (getIconStyle) {
                "outlined" -> Icons.Outlined.Lock
                "filled" -> Icons.Filled.Lock
                "sharp" -> Icons.Sharp.Lock
                "two-tone" -> Icons.TwoTone.Lock
                else -> {
                    context.log.warn("Error setting icon style $getIconStyle")
                    Icons.Rounded.Lock
                }
            }
        } else {
            Icons.Rounded.Lock
        }

        val editIconStyle = if (getIconStyle != null) {
            when (getIconStyle) {
                "outlined" -> Icons.Outlined.Edit
                "filled" -> Icons.Filled.Edit
                "sharp" -> Icons.Sharp.Edit
                "two-tone" -> Icons.TwoTone.Edit
                else -> {
                    context.log.warn("Error setting icon style $getIconStyle")
                    Icons.Rounded.Edit
                }
            }
        } else {
            Icons.Rounded.Edit
        }

        val messageInfoTag = Random.nextLong().toString()
        onNextActivityCreate {
            val appleLogo = AppleLogo

            context.event.subscribe(BindViewEvent::class) { event ->
                event.chatMessage { _, _ ->
                    val view = event.view as? ViewGroup ?: return@subscribe
                    view.findViewWithTag<View>(messageInfoTag)?.let { view.removeView(it) }

                    val message = event.databaseMessage ?: return@chatMessage
                    if (message.contentType != ContentType.SNAP.id && message.contentType != ContentType.EXTERNAL_MEDIA.id) return@chatMessage
                    val reader = ProtoReader(message.messageContent ?: return@chatMessage)



                    createComposeView(event.view.context) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(top = 4.dp, end = 1.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            val hasEncryption by rememberAsyncMutableState(defaultValue = false) {
                                reader.getByteArray(4, 3, 3) != null || reader.containsPath(3, 99, 3)
                            }
                            val sentFromIosDevice by rememberAsyncMutableState(defaultValue = false) {
                                if (reader.containsPath(4, 4, 3)) !reader.containsPath(4, 4, 3, 3, 17) else reader.getVarInt(4, 4, 11, 17, 7) != null
                            }
                            val sentFromWebApp by rememberAsyncMutableState(defaultValue = false) {
                                reader.getVarInt(4, 4, *(if (reader.containsPath(4, 4, 3)) intArrayOf(3, 3, 22, 1) else intArrayOf(11, 22, 1))) == 7L
                            }
                            val sentWithLocation by rememberAsyncMutableState(defaultValue = false) {
                                reader.getVarInt(4, 4, 11, 17, 5) != null
                            }
                            val sentUsingOvfEditor by rememberAsyncMutableState(defaultValue = false) {
                                (reader.getString(4, 4, 11, 12, 1) ?: reader.getString(4, 4, 11, 13, 4, 1, 2, 12, 20, 1)) == "c13129f7-fe4a-44c4-9b9d-e0b26fee8f82"
                            }
                            val sentUsingDirectorMode by rememberAsyncMutableState(defaultValue = false) {
                                reader.followPath(4, 4, 11, 28)?.let {
                                    (it.getVarInt(1) to it.getVarInt(2)) == (0L to 0L)
                                } == true || reader.getByteArray(4, 4, 11, 13, 4, 1, 2, 12, 27, 1) != null
                            }

                            /*TODO: feat: message indicator icon color - Support theming with the icons */
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (sentWithLocation && messageIndicatorsConfig.contains("location_indicator")) {
                                    Image(
                                        imageVector = locationOnIconStyle,
                                        colorFilter = ColorFilter.tint(Color.Green),
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                if (messageIndicatorsConfig.contains("platform_indicator")) {
                                    Image(
                                        imageVector = when {
                                            sentFromWebApp -> laptopIconStyle
                                            sentFromIosDevice -> appleLogo
                                            else -> androidIconStyle
                                        },
                                        colorFilter = ColorFilter.tint(Color.Green),
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                if (hasEncryption && messageIndicatorsConfig.contains("encryption_indicator")) {
                                    Image(
                                        imageVector = lockIconStyle,
                                        colorFilter = ColorFilter.tint(Color.Green),
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                if (sentUsingDirectorMode && messageIndicatorsConfig.contains("director_mode_indicator")) {
                                    Image(
                                        imageVector = editIconStyle,
                                        colorFilter = ColorFilter.tint(Color.Red),
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                if (sentUsingOvfEditor && messageIndicatorsConfig.contains("ovf_editor_indicator")) {
                                    Text(
                                        text = "OVF",
                                        color = Color.Red,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        }
                    }.apply {
                        tag = messageInfoTag
                        addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
                            layout(left, 0, right, 0)
                        }
                        setPadding(0, 0, 0, -(50 * event.view.resources.displayMetrics.density).toInt())
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        view.addView(this)
                    }
                }
            }
        }
    }
}