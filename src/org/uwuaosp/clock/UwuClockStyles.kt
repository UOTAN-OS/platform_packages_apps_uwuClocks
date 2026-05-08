package org.uwuaosp.clock

data class UwuClockStyleConfig(
    val clockId: String,
    val nameResId: Int,
    val descriptionResId: Int,
    val thumbnailResId: Int,
    val layoutMode: ClockLayoutMode,
    val dateFontFamily: String,
    val dateFontWeight: Int,
    val glyphFontFamily: String,
    val glyphFontWeight: Int,
    val glyphRenderMode: GlyphRenderMode,
    val glyphStyleScale: Float,
    val glyphLetterSpacing: Float,
    val dateAlpha: Float,
)

enum class GlyphRenderMode {
    TEXT,
    VECTOR,
}

enum class ClockLayoutMode {
    VERTICAL_SPLIT,
    HORIZONTAL_ROW,
}

object UwuClockStyles {
    private val uwuClock =
        UwuClockStyleConfig(
            clockId = UwuClockProvider.CLOCK_ID,
            nameResId = R.string.uwu_clock_name,
            descriptionResId = R.string.uwu_clock_description,
            thumbnailResId = R.drawable.uwu_clock_thumbnail,
            layoutMode = ClockLayoutMode.VERTICAL_SPLIT,
            dateFontFamily = "google-sans-flex",
            dateFontWeight = 400,
            glyphFontFamily = "google-sans-flex-clock",
            glyphFontWeight = 700,
            glyphRenderMode = GlyphRenderMode.VECTOR,
            glyphStyleScale = 1.24f,
            glyphLetterSpacing = -0.05f,
            dateAlpha = 0.9f,
        )

    private val horizontalLarge =
        UwuClockStyleConfig(
            clockId = UwuClockProvider.HORIZONTAL_CLOCK_ID,
            nameResId = R.string.uwu_clock_horizontal_name,
            descriptionResId = R.string.uwu_clock_horizontal_description,
            thumbnailResId = R.drawable.uwu_clock_horizontal_thumbnail,
            layoutMode = ClockLayoutMode.HORIZONTAL_ROW,
            dateFontFamily = "google-sans-flex",
            dateFontWeight = 500,
            glyphFontFamily = "google-sans-flex-clock",
            glyphFontWeight = 700,
            glyphRenderMode = GlyphRenderMode.TEXT,
            glyphStyleScale = 1.0f,
            glyphLetterSpacing = -0.04f,
            dateAlpha = 0.92f,
        )

    fun style1(): UwuClockStyleConfig = uwuClock

    fun style2(): UwuClockStyleConfig = horizontalLarge

    fun all(): List<UwuClockStyleConfig> = listOf(uwuClock, horizontalLarge)
}
