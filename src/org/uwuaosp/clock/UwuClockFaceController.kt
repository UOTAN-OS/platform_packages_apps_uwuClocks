package org.uwuaosp.clock

import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import com.android.systemui.customization.clocks.DigitalTimeFormatter
import com.android.systemui.customization.clocks.DigitalTimespec
import com.android.systemui.customization.clocks.DigitalTimespecHandler
import com.android.systemui.log.core.Logger
import com.android.systemui.log.core.MessageBuffer
import com.android.systemui.plugins.keyguard.ui.clocks.ClockAnimations
import com.android.systemui.plugins.keyguard.ui.clocks.ClockAxisStyle
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceController
import com.android.systemui.plugins.keyguard.ui.clocks.ClockFaceEvents
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPositionAnimationArgs
import com.android.systemui.plugins.keyguard.ui.clocks.ClockSettings
import com.android.systemui.plugins.keyguard.ui.clocks.ClockViewIds
import com.android.systemui.plugins.keyguard.ui.clocks.ThemeConfig
import com.android.systemui.plugins.keyguard.ui.clocks.TimeFormatKind
import com.android.systemui.plugins.keyguard.VRect
import java.util.Locale
import kotlin.math.max

class UwuClockFaceController(
    private val pluginCtx: Context,
    private val settings: ClockSettings,
    private val timeFormatter: DigitalTimeFormatter,
    private val messageBuffer: MessageBuffer,
    private val isLargeClock: Boolean,
    private val clockStyle: UwuClockStyleConfig,
) : ClockFaceController {
    private val logger = Logger(messageBuffer, this::class.simpleName!!)
    private val timespecHandler = DigitalTimespecHandler(DigitalTimespec.TIME_FULL_FORMAT, timeFormatter)
    private val clockId = settings.clockId ?: UwuClockProvider.CLOCK_ID
    private val isHorizontalRow = clockStyle.layoutMode == ClockLayoutMode.HORIZONTAL_ROW

    private var locale: Locale = Locale.getDefault()
    private var timeZone: TimeZone = TimeZone.getDefault()
    private var lastRenderedTimeText = ""
    private var lastRenderedDateText = ""

    private val dateView =
        buildTextView(fontWeight = clockStyle.dateFontWeight, fontFamily = clockStyle.dateFontFamily).apply {
            alpha = clockStyle.dateAlpha
        }

    private val topSpacer = Space(pluginCtx)

    private val minuteView = UwuGlyphView(pluginCtx).apply {
            includeFontPadding = false
            letterSpacing = clockStyle.glyphLetterSpacing
            setRenderMode(clockStyle.glyphRenderMode)
            setStyleScale(clockStyle.glyphStyleScale)
        }

    private val hourView = UwuGlyphView(pluginCtx).apply {
            includeFontPadding = false
            letterSpacing = clockStyle.glyphLetterSpacing
            setRenderMode(clockStyle.glyphRenderMode)
            setStyleScale(clockStyle.glyphStyleScale)
        }

    private val bottomSpacer = Space(pluginCtx)
    private val timeRow =
        LinearLayout(pluginCtx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            clipChildren = false
            clipToPadding = false
        }
    private val contentView =
        if (isHorizontalRow) {
            LinearLayout(pluginCtx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                clipChildren = false
                clipToPadding = false
                timeRow.addView(hourView, wrapContentLayoutParams())
                timeRow.addView(minuteView, wrapContentLayoutParams())
                addView(topSpacer, LinearLayout.LayoutParams(1, 0))
                addView(timeRow, wrapContentLayoutParams())
                addView(dateView)
                addView(bottomSpacer, LinearLayout.LayoutParams(1, 0))
            }
        } else {
            LinearLayout(pluginCtx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                clipChildren = false
                clipToPadding = false
                addView(topSpacer, LinearLayout.LayoutParams(1, 0))
                addView(hourView)
                addView(dateView)
                addView(minuteView)
                addView(bottomSpacer, LinearLayout.LayoutParams(1, 0))
            }
        }

    @Deprecated("Prefer use of layout")
    override val view: FrameLayout =
        FrameLayout(pluginCtx).apply {
            id =
                if (isLargeClock) ClockViewIds.LOCKSCREEN_CLOCK_VIEW_LARGE
                else ClockViewIds.LOCKSCREEN_CLOCK_VIEW_SMALL
            clipChildren = false
            clipToPadding = false
            setBackgroundColor(Color.TRANSPARENT)
            visibility = View.VISIBLE
            addView(
                contentView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                ),
            )
        }

    override val layout = UwuClockFaceLayout(view)
    override val config = ClockFaceConfig()
    override var theme = ThemeConfig(isDarkTheme = true, settings.seedColor)

    override val events =
        object : ClockFaceEvents {
            override fun onTimeTick() {
                timeFormatter.timeKeeper.updateTime()
                refreshClock()
            }

            override fun onThemeChanged(theme: ThemeConfig) {
                this@UwuClockFaceController.theme = theme
                applyPalette(theme)
            }

            override fun onFontSettingChanged(fontSizePx: Float) {
                applyFontSizes(fontSizePx)
                contentView.requestLayout()
            }

            override fun onTargetRegionChanged(targetRegion: VRect) {}

            override fun onSecondaryDisplayChanged(onSecondaryDisplay: Boolean) {}
        }

    override val animations =
        object : ClockAnimations {
            override fun enter() {}

            override fun doze(fraction: Float) {
                val alpha = 1f - (fraction * 0.18f)
                dateView.alpha = alpha
                hourView.alpha = alpha
                minuteView.alpha = alpha
            }

            override fun fold(fraction: Float) {}

            override fun charge() {}

            override fun onPickerCarouselSwiping(swipingFraction: Float) {}

            override fun onPositionAnimated(anim: ClockPositionAnimationArgs) {}

            override fun onFidgetTap(x: Float, y: Float) {}

            override fun onFontAxesChanged(style: ClockAxisStyle) {}
        }

    init {
        applyFontSizes(defaultFontSizePx())
        applyPalette(theme)
        refreshClock(forceLayout = true)
    }

    fun onTimeZoneChanged(timeZone: TimeZone) {
        this.timeZone = timeZone
        refreshClock(forceLayout = true)
    }

    fun onTimeFormatChanged(formatKind: TimeFormatKind) {
        refreshClock(forceLayout = true)
    }

    fun onLocaleChanged(locale: Locale) {
        this.locale = locale
        refreshClock(forceLayout = true)
    }

    private fun refreshClock(forceLayout: Boolean = false) {
        val timeText = timespecHandler.getText().toString()
        val dateText = buildDateText()
        val hasTimeChanged = timeText != lastRenderedTimeText
        val hasDateChanged = dateText != lastRenderedDateText

        if (!forceLayout && !hasTimeChanged && !hasDateChanged) {
            return
        }

        val separator = timeText.indexOf(':')
        if (separator > 0 && separator < timeText.lastIndex) {
            hourView.setTextGlyph(
                timeText.substring(0, separator),
                clockStyle.glyphFontFamily,
                clockStyle.glyphFontWeight,
            )
            minuteView.setTextGlyph(
                timeText.substring(separator + 1),
                clockStyle.glyphFontFamily,
                clockStyle.glyphFontWeight,
            )
        } else {
            val half = timeText.length / 2
            hourView.setTextGlyph(
                timeText.substring(0, half),
                clockStyle.glyphFontFamily,
                clockStyle.glyphFontWeight,
            )
            minuteView.setTextGlyph(
                timeText.substring(half),
                clockStyle.glyphFontFamily,
                clockStyle.glyphFontWeight,
            )
        }

        dateView.text = dateText
        lastRenderedTimeText = timeText
        lastRenderedDateText = dateText
        contentView.requestLayout()
        logger.i("Rendered uwu clock: ${hourView.getGlyphText()} ${dateView.text} ${minuteView.getGlyphText()}")
    }

    private fun buildDateText(): String {
        val now = timeFormatter.timeKeeper.time
        val isChinese = locale.language == Locale.CHINESE.language
        val pattern = if (isChinese) "M月d日 EEEE" else "MMM d EEEE"
        return SimpleDateFormat(pattern, locale).apply { timeZone = this@UwuClockFaceController.timeZone }
            .format(now)
            .let { text ->
                if (isChinese) {
                    text
                } else {
                    text.replaceFirstChar { char ->
                        if (char.isLowerCase()) {
                            char.titlecase(locale)
                        } else {
                            char.toString()
                        }
                    }
                }
            }
    }

    private fun applyFontSizes(fontSizePx: Float) {
        val isHorizontalLarge = clockId == UwuClockProvider.HORIZONTAL_CLOCK_ID
        val timeSize =
            when {
                isHorizontalLarge && isLargeClock -> fontSizePx * 0.50f
                isHorizontalLarge -> fontSizePx * 0.82f
                isLargeClock -> fontSizePx * 1.00f
                else -> fontSizePx * 1.08f
            }
        val dateSize =
            when {
                isHorizontalLarge && isLargeClock -> timeSize * 0.18f
                isHorizontalLarge -> timeSize * 0.16f
                isLargeClock -> timeSize * 0.16f
                else -> timeSize * 0.20f
            }
        val topSpacerHeight =
            when {
                isHorizontalLarge && isLargeClock -> 0
                isHorizontalLarge -> 0
                isLargeClock -> (timeSize * 0.08f).toInt()
                else -> (timeSize * 0.04f).toInt()
            }
        val hourBottomMargin =
            when {
                isHorizontalLarge && isLargeClock -> 0
                isHorizontalLarge -> 0
                isLargeClock -> (timeSize * 0.42f).toInt()
                else -> (timeSize * 0.24f).toInt()
            }
        val dateBottomMargin =
            when {
                isHorizontalLarge && isLargeClock -> (timeSize * 0.04f).toInt()
                isHorizontalLarge -> (timeSize * 0.03f).toInt()
                isLargeClock -> (timeSize * 0.38f).toInt()
                else -> (timeSize * 0.18f).toInt()
            }
        val bottomSpacerHeight =
            when {
                isHorizontalLarge && isLargeClock -> 0
                isHorizontalLarge -> 0
                isLargeClock -> (timeSize * 0.06f).toInt()
                else -> (timeSize * 0.03f).toInt()
            }

        dateView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateSize)
        hourView.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize)
        minuteView.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize)
        dateView.setPadding(0, dp(if (isHorizontalLarge) 0 else 2), 0, dp(if (isHorizontalLarge) 0 else 2))
        if (isHorizontalLarge) {
            val sidePadding = dp(if (isLargeClock) 6 else 4)
            hourView.setPadding(0, 0, sidePadding, 0)
            minuteView.setPadding(sidePadding, 0, 0, 0)
            timeRow.setPadding(dp(if (isLargeClock) 8 else 4), 0, dp(if (isLargeClock) 8 else 4), 0)
        } else {
            hourView.setPadding(0, 0, 0, 0)
            minuteView.setPadding(0, 0, 0, 0)
            timeRow.setPadding(0, 0, 0, 0)
        }

        if (isHorizontalRow) {
            updateLinearLayoutParams(topSpacer, topSpacerHeight)
            updateLinearLayoutParams(hourView, bottomMargin = hourBottomMargin)
            updateLinearLayoutParams(minuteView, bottomMargin = 0)
            updateLinearLayoutParams(dateView, bottomMargin = dateBottomMargin)
            updateLinearLayoutParams(bottomSpacer, bottomSpacerHeight)
        } else {
            updateLinearLayoutParams(topSpacer, topSpacerHeight)
            updateLinearLayoutParams(hourView, bottomMargin = hourBottomMargin)
            updateLinearLayoutParams(dateView, bottomMargin = dateBottomMargin)
            updateLinearLayoutParams(minuteView)
            updateLinearLayoutParams(bottomSpacer, bottomSpacerHeight)
        }
    }

    private fun applyPalette(theme: ThemeConfig) {
        val palette = createPalette(theme)
        hourView.setTextColor(palette.hourColor)
        minuteView.setTextColor(palette.minuteColor)
        dateView.setTextColor(palette.dateColor)
    }

    private fun createPalette(theme: ThemeConfig): ClockPalette {
        val base = theme.seedColor ?: theme.getDefaultColor(pluginCtx)
        val hsv = FloatArray(3)
        Color.colorToHSV(base, hsv)
        val isDark = theme.isDarkTheme
        val hourColor =
            shiftColor(
                hsv,
                saturationMul = 0.55f,
                valueMul = if (isDark) 1.20f else 0.48f,
                minValue = if (isDark) 0.92f else 0.22f,
            )
        val minuteColor =
            shiftColor(
                hsv,
                saturationMul = 0.72f,
                valueMul = if (isDark) 1.04f else 0.42f,
                minValue = if (isDark) 0.78f else 0.18f,
            )
        val dateColor =
            shiftColor(
                hsv,
                saturationMul = 0.40f,
                valueMul = if (isDark) 1.08f else 0.44f,
                minValue = if (isDark) 0.82f else 0.18f,
                alpha = 232,
            )
        return ClockPalette(hourColor, minuteColor, dateColor)
    }

    private fun shiftColor(
        hsv: FloatArray,
        saturationMul: Float,
        valueMul: Float,
        minValue: Float,
        alpha: Int = 255,
    ): Int {
        val shifted =
            floatArrayOf(
                hsv[0],
                (hsv[1] * saturationMul).coerceIn(0f, 1f),
                max((hsv[2] * valueMul).coerceIn(0f, 1f), minValue),
            )
        return Color.HSVToColor(alpha, shifted)
    }

    private fun updateLinearLayoutParams(
        child: View,
        height: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        bottomMargin: Int = 0,
    ) {
        val params =
            (child.layoutParams as? LinearLayout.LayoutParams)
                ?: LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
        params.width =
            if (child === topSpacer || child === bottomSpacer) {
                1
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
        params.height = height
        params.gravity = Gravity.CENTER_HORIZONTAL
        params.bottomMargin = bottomMargin
        child.layoutParams = params
    }

    private fun wrapContentLayoutParams(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )

    private fun buildTextView(fontWeight: Int, fontFamily: String): TextView {
        return TextView(pluginCtx).apply {
            gravity = Gravity.CENTER
            typeface = createClockTypeface(fontFamily, fontWeight)
            setSingleLine(true)
            maxLines = 1
            setTextColor(Color.WHITE)
        }
    }

    private fun defaultFontSizePx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            if (isLargeClock) 96f else 42f,
            pluginCtx.resources.displayMetrics,
        )
    }

    private fun dp(value: Int): Int =
        (value * pluginCtx.resources.displayMetrics.density).toInt()

    private data class ClockPalette(
        val hourColor: Int,
        val minuteColor: Int,
        val dateColor: Int,
    )
}
