package org.uwuaosp.clock

import android.content.Context
import android.icu.util.TimeZone
import com.android.systemui.customization.clocks.DigitalTimeFormatter
import com.android.systemui.customization.clocks.TimeKeeper
import com.android.systemui.plugins.keyguard.data.model.AlarmData
import com.android.systemui.plugins.keyguard.data.model.WeatherData
import com.android.systemui.plugins.keyguard.data.model.ZenData
import com.android.systemui.plugins.keyguard.ui.clocks.ClockConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockEventListeners
import com.android.systemui.plugins.keyguard.ui.clocks.ClockEvents
import com.android.systemui.plugins.keyguard.ui.clocks.ClockMessageBuffers
import com.android.systemui.plugins.keyguard.ui.clocks.ClockSettings
import com.android.systemui.plugins.keyguard.ui.clocks.TimeFormatKind
import java.io.PrintWriter
import java.util.Locale

class UwuClockController(
    pluginCtx: Context,
    private val settings: ClockSettings,
    messageBuffers: ClockMessageBuffers,
    private val timeKeeper: TimeKeeper,
) : ClockController {
    private val timeFormatter = DigitalTimeFormatter("hh:mm", timeKeeper)
    private val clockId = settings.clockId ?: UwuClockProvider.CLOCK_ID
    private val clockStyle =
        when (clockId) {
            UwuClockProvider.CLOCK_ID -> UwuClockStyles.style1()
            UwuClockProvider.HORIZONTAL_CLOCK_ID -> UwuClockStyles.style2()
            else -> error("$clockId unsupported by this provider")
        }

    override val smallClock =
        UwuClockFaceController(
            pluginCtx = pluginCtx,
            settings = settings,
            timeFormatter = timeFormatter,
            messageBuffer = messageBuffers.smallClockMessageBuffer,
            isLargeClock = false,
            clockStyle = clockStyle,
        )

    override val largeClock =
        UwuClockFaceController(
            pluginCtx = pluginCtx,
            settings = settings,
            timeFormatter = timeFormatter,
            messageBuffer = messageBuffers.largeClockMessageBuffer,
            isLargeClock = true,
            clockStyle = clockStyle,
        )

    override val config =
        ClockConfig(
            clockStyle.clockId,
            pluginCtx.getString(clockStyle.nameResId),
            pluginCtx.getString(clockStyle.descriptionResId),
        )

    override val eventListeners = ClockEventListeners()

    override val events =
        object : ClockEvents {
            override var isReactiveTouchInteractionEnabled = false

            override fun onTimeZoneChanged(timeZone: TimeZone) {
                timeFormatter.timeKeeper.timeZone = timeZone
                smallClock.onTimeZoneChanged(timeZone)
                largeClock.onTimeZoneChanged(timeZone)
            }

            override fun onTimeFormatChanged(formatKind: TimeFormatKind) {
                timeFormatter.formatKind = formatKind
                smallClock.onTimeFormatChanged(formatKind)
                largeClock.onTimeFormatChanged(formatKind)
            }

            override fun onLocaleChanged(locale: Locale) {
                timeFormatter.locale = locale
                smallClock.onLocaleChanged(locale)
                largeClock.onLocaleChanged(locale)
            }

            override fun onWeatherDataChanged(data: WeatherData) {}

            override fun onAlarmDataChanged(data: AlarmData) {}

            override fun onZenDataChanged(data: ZenData) {}
        }

    override fun initialize(isDarkTheme: Boolean, dozeFraction: Float, foldFraction: Float) {
        listOf(smallClock, largeClock).forEach { face ->
            face.events.onThemeChanged(face.theme.copy(isDarkTheme = isDarkTheme))
            face.animations.doze(dozeFraction)
            face.animations.fold(foldFraction)
            face.events.onTimeTick()
        }
    }

    override fun dump(pw: PrintWriter) {
        pw.println("UwuClockController(clockId=$clockId)")
    }
}
