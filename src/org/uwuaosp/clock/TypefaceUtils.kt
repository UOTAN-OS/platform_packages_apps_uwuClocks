package org.uwuaosp.clock

import android.graphics.Typeface

internal fun createClockTypeface(fontFamily: String, fontWeight: Int): Typeface {
    val base = Typeface.create(fontFamily, Typeface.NORMAL)
    return Typeface.create(base, fontWeight.coerceIn(1, 1000), false)
}
