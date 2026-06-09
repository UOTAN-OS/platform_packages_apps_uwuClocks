package org.uwuaosp.clock

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import kotlin.math.ceil
import kotlin.math.max

class UwuGlyphView(context: Context) : FrameLayout(context) {
    private val textView =
        TextView(context).apply {
            gravity = Gravity.CENTER
            setSingleLine(true)
            includeFontPadding = false
        }
    private val imageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            adjustViewBounds = true
        }

    private var renderMode = GlyphRenderMode.TEXT
    private var currentText: CharSequence = ""
    private var currentFontFamily: String = "sans-serif"
    private var currentFontWeight: Int = Typeface.NORMAL
    private var currentTextSizePx: Float = 0f
    private var currentTextColor: Int = 0xffffffff.toInt()
    private var currentLetterSpacing: Float = 0f
    private var currentIncludeFontPadding: Boolean = false
    private var currentStyleScale: Float = 1f

    var letterSpacing: Float
        get() = currentLetterSpacing
        set(value) {
            textView.letterSpacing = value
            currentLetterSpacing = value
            refreshVectorDrawable()
        }

    var includeFontPadding: Boolean
        get() = currentIncludeFontPadding
        set(value) {
            textView.includeFontPadding = value
            currentIncludeFontPadding = value
            refreshVectorDrawable()
        }

    init {
        addView(
            textView,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER),
        )
        addView(
            imageView,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER),
        )
        showTextMode()
    }

    fun setTextGlyph(text: CharSequence, fontFamily: String, fontWeight: Int) {
        currentText = text
        currentFontFamily = fontFamily
        currentFontWeight = fontWeight
        when (renderMode) {
            GlyphRenderMode.TEXT -> {
                textView.typeface = createClockTypeface(fontFamily, fontWeight)
                textView.text = text
                showTextMode()
            }
            GlyphRenderMode.VECTOR -> {
                imageView.setImageDrawable(buildTextDrawable(text, fontFamily, fontWeight))
                showVectorMode()
            }
        }
        requestLayout()
        invalidate()
    }

    fun setDrawableGlyph(drawable: Drawable?) {
        renderMode = GlyphRenderMode.VECTOR
        imageView.setImageDrawable(drawable)
        showVectorMode()
        requestLayout()
        invalidate()
    }

    fun setRenderMode(mode: GlyphRenderMode) {
        renderMode = mode
        setTextGlyph(currentText, currentFontFamily, currentFontWeight)
    }

    fun getGlyphText(): CharSequence = currentText

    fun setTextSize(unit: Int, size: Float) {
        textView.setTextSize(unit, size)
        currentTextSizePx = size
        refreshVectorDrawable()
        requestLayout()
    }

    fun setTextColor(color: Int) {
        currentTextColor = color
        textView.setTextColor(color)
        refreshVectorDrawable()
    }

    fun setSingleLine(singleLine: Boolean) {
        textView.setSingleLine(singleLine)
    }

    private fun buildTextDrawable(text: CharSequence, fontFamily: String, fontWeight: Int): Drawable {
        return TextGlyphDrawable(
            text = text.toString(),
            typeface = createClockTypeface(fontFamily, fontWeight),
            textSizePx = if (currentTextSizePx > 0f) currentTextSizePx * currentStyleScale else textView.textSize,
            textColor = currentTextColor,
            letterSpacing = currentLetterSpacing,
        )
    }

    fun setStyleScale(scale: Float) {
        currentStyleScale = scale
        refreshVectorDrawable()
        requestLayout()
    }

    private fun refreshVectorDrawable() {
        if (renderMode == GlyphRenderMode.VECTOR) {
            imageView.setImageDrawable(buildTextDrawable(currentText, currentFontFamily, currentFontWeight))
        }
    }

    private fun showTextMode() {
        textView.visibility = VISIBLE
        imageView.visibility = GONE
    }

    private fun showVectorMode() {
        textView.visibility = GONE
        imageView.visibility = VISIBLE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val activeChild: View =
            when (renderMode) {
                GlyphRenderMode.TEXT -> textView
                GlyphRenderMode.VECTOR -> imageView
            }
        measureChild(activeChild, widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            resolveSize(activeChild.measuredWidth + paddingLeft + paddingRight, widthMeasureSpec),
            resolveSize(activeChild.measuredHeight + paddingTop + paddingBottom, heightMeasureSpec),
        )
    }

    private class TextGlyphDrawable(
        private val text: String,
        typeface: Typeface,
        private val textSizePx: Float,
        private val textColor: Int,
        private val letterSpacing: Float,
    ) : Drawable() {
        private val paint =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typeface
                this.color = textColor
                this.textSize = textSizePx
                this.isLinearText = true
                this.letterSpacing = letterSpacing
            }
        private val intrinsicPadding = max(2f, textSizePx * 0.08f)
        private val fontMetrics = paint.fontMetrics

        override fun draw(canvas: Canvas) {
            if (text.isEmpty()) return
            val contentWidth = paint.measureText(text).coerceAtLeast(1f)
            val contentHeight = (fontMetrics.descent - fontMetrics.ascent).coerceAtLeast(1f)
            val x = bounds.left + (bounds.width() - contentWidth) / 2f
            val baseline =
                bounds.top + (bounds.height() - contentHeight) / 2f - fontMetrics.ascent
            canvas.drawText(text, x, baseline, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
            invalidateSelf()
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
            invalidateSelf()
        }

        @Deprecated("Deprecated in Java")
        override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT

        override fun getIntrinsicWidth(): Int {
            return ceil(paint.measureText(text) + intrinsicPadding * 2f).toInt().coerceAtLeast(1)
        }

        override fun getIntrinsicHeight(): Int {
            return ceil((fontMetrics.descent - fontMetrics.ascent) + intrinsicPadding * 2f)
                .toInt()
                .coerceAtLeast(1)
        }
    }
}
