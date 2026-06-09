package org.uwuaosp.clock

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.constraintlayout.widget.ConstraintSet.START
import com.android.systemui.customization.clocks.DefaultClockFaceLayout
import com.android.systemui.plugins.keyguard.ui.clocks.ClockPreviewConfig
import com.android.systemui.plugins.keyguard.ui.clocks.ClockViewIds

class UwuClockFaceLayout(view: View) : DefaultClockFaceLayout(view) {
    override fun applyConstraints(constraints: ConstraintSet): ConstraintSet {
        return super.applyConstraints(constraints).apply {
            useFullWidthLargeClock()
        }
    }

    override fun applyPreviewConstraints(
        clockPreviewConfig: ClockPreviewConfig,
        constraints: ConstraintSet,
    ): ConstraintSet {
        return super.applyPreviewConstraints(clockPreviewConfig, constraints).apply {
            useFullWidthLargeClock()
        }
    }

    private fun ConstraintSet.useFullWidthLargeClock() {
        constrainWidth(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_LARGE, MATCH_CONSTRAINT)
        connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_LARGE, START, PARENT_ID, START)
        connect(ClockViewIds.LOCKSCREEN_CLOCK_VIEW_LARGE, END, PARENT_ID, END)
    }
}
