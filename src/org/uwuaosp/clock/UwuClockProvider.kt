package org.uwuaosp.clock

import android.content.Context
import android.os.Build
import com.android.internal.annotations.Keep
import com.android.systemui.customization.clocks.TimeKeeperImpl
import com.android.systemui.log.LogcatOnlyMessageBuffer
import com.android.systemui.log.core.LogLevel
import com.android.systemui.plugins.annotations.Requires
import com.android.systemui.plugins.keyguard.ui.clocks.ClockController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMessageBuffers
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMetadata
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPickerConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockProviderPlugin
import com.android.systemui.plugins.keyguard.ui.clocks.ClockSettings

@Keep
@Requires(target = ClockProviderPlugin::class, version = ClockProviderPlugin.VERSION)
class UwuClockProvider : ClockProviderPlugin {
    private lateinit var pluginCtx: Context
    private lateinit var messageBuffers: ClockMessageBuffers

    override fun onCreate(hostCtx: Context, pluginCtx: Context) {
        this.pluginCtx = pluginCtx
    }

    override fun initialize(buffers: ClockMessageBuffers?) {
        messageBuffers =
            buffers ?: ClockMessageBuffers(LogcatOnlyMessageBuffer(LogLevel.DEBUG))
    }

    override fun getClocks(): List<ClockMetadata> =
        listOf(
            ClockMetadata(CLOCK_ID),
            ClockMetadata(HORIZONTAL_CLOCK_ID),
        )

    override fun createClock(ctx: Context, settings: ClockSettings): ClockController {
        val clockId = settings.clockId ?: CLOCK_ID
        return when (clockId) {
            CLOCK_ID, HORIZONTAL_CLOCK_ID ->
                UwuClockController(pluginCtx, settings, messageBuffers, TimeKeeperImpl())
            else -> error("$clockId unsupported by this provider")
        }
    }

    override fun getClockPickerConfig(settings: ClockSettings): ClockPickerConfig {
        val style = styleFor(settings.clockId ?: CLOCK_ID)
        return ClockPickerConfig(
            style.clockId,
            pluginCtx.getString(style.nameResId),
            pluginCtx.getString(style.descriptionResId),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                pluginCtx.resources.getDrawable(style.thumbnailResId, null)
            } else {
                @Suppress("DEPRECATION")
                pluginCtx.resources.getDrawable(style.thumbnailResId)
            },
        )
    }

    companion object {
        const val CLOCK_ID = "UWU_CLOCK"
        const val HORIZONTAL_CLOCK_ID = "UWU_CLOCK_HORIZONTAL"
    }

    private fun styleFor(clockId: String): UwuClockStyleConfig {
        return when (clockId) {
            CLOCK_ID -> UwuClockStyles.style1()
            HORIZONTAL_CLOCK_ID -> UwuClockStyles.style2()
            else -> error("$clockId unsupported by this provider")
        }
    }
}
