package eu.darken.capod.main.ui.widget

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import eu.darken.capod.R
import eu.darken.capod.pods.core.apple.ble.formatBatteryDurationShort
import kotlin.math.max
import kotlin.math.min

enum class BatteryLayout {
    TINY_COLUMN,
    NARROW,
    WIDE;

    companion object {
        fun forCells(widthCells: Int): BatteryLayout = when {
            widthCells <= 1 -> TINY_COLUMN
            widthCells >= 5 -> WIDE
            else -> NARROW
        }

        /**
         * Picks the layout for the widget's current size. In [WidgetTheme.LayoutStyle.AUTO] the row or
         * column is chosen by whichever lets the content grow the most, so a short wide widget gets a row
         * and a tall one a column instead of clipping.
         */
        fun forSize(
            widthDp: Float,
            heightDp: Float,
            theme: WidgetTheme,
            isDualPod: Boolean,
        ): BatteryLayout {
            if (forCells(cellsForWidth(widthDp)) == TINY_COLUMN) return TINY_COLUMN
            return when (theme.layoutStyle) {
                WidgetTheme.LayoutStyle.HORIZONTAL -> WIDE
                WidgetTheme.LayoutStyle.VERTICAL -> NARROW
                WidgetTheme.LayoutStyle.AUTO -> {
                    val wideFit = fitScale(WIDE, isDualPod, theme, widthDp, heightDp)
                    val narrowFit = fitScale(NARROW, isDualPod, theme, widthDp, heightDp)
                    // A row reads better on wide widgets, so it wins unless the column is clearly roomier.
                    if (wideFit >= narrowFit * WIDE_LAYOUT_BIAS) WIDE else NARROW
                }
            }
        }

        /**
         * Number of launcher cells needed for a given width.
         * https://developer.android.com/guide/practices/ui_guidelines/widget_design
         */
        fun cellsForWidth(widthDp: Float): Int {
            var n = 2
            while (70 * n - 30 <= widthDp) {
                ++n
            }
            return n - 1
        }
    }
}

/**
 * Multiplier for icon, text and spacing sizes. Content grows with the widget in
 * [WidgetTheme.ContentSize.AUTO]; fixed sizes are still shrunk when they would not fit, so a resize
 * never clips the content.
 */
fun widgetContentScale(
    layout: BatteryLayout,
    isDualPod: Boolean,
    theme: WidgetTheme,
    widthDp: Float,
    heightDp: Float,
): Float {
    if (layout == BatteryLayout.TINY_COLUMN) return 1f
    val fit = fitScale(layout, isDualPod, theme, widthDp, heightDp)
    val preferred = theme.contentSize.fixedScale ?: fit
    return min(preferred, fit).coerceIn(MIN_CONTENT_SCALE, MAX_CONTENT_SCALE)
}

private fun fitScale(
    layout: BatteryLayout,
    isDualPod: Boolean,
    theme: WidgetTheme,
    widthDp: Float,
    heightDp: Float,
): Float {
    val labelLine = if (theme.showDeviceLabel) TEXT_LINE_DP else 0f
    val statusIcons = if (theme.showStatusIcons) SMALL_ICON_DP else 0f
    val bar = if (theme.showBatteryBars) BAR_HEIGHT_DP + BAR_GAP_DP else 0f
    val (contentWidth, contentHeight) = when {
        !isDualPod -> {
            val text = if (theme.showEstimates) PERCENT_WITH_ESTIMATE_WIDTH_DP else PERCENT_WIDTH_DP
            (SMALL_ICON_DP + text + 2 * statusIcons) to (SMALL_ICON_DP + bar + labelLine)
        }

        layout == BatteryLayout.WIDE -> {
            val items = if (theme.showCase) 3 else 2
            val item = LARGE_ICON_DP + PERCENT_WIDTH_DP + statusIcons
            val estimate = if (theme.showEstimates) TEXT_LINE_DP else 0f
            (items * item + (items - 1) * WIDE_POD_ROW_PADDING_DP) to
                    (WIDE_POD_ROW_PADDING_DP + LARGE_ICON_DP + estimate + bar + labelLine)
        }

        else -> {
            val rows = if (theme.showCase) 3 else 2
            val text = if (theme.showEstimates) PERCENT_WITH_ESTIMATE_WIDTH_DP else PERCENT_WIDTH_DP
            (SMALL_ICON_DP + text + 2 * statusIcons) to (rows * (SMALL_ICON_DP + bar) + labelLine)
        }
    }
    val availableWidth = (widthDp - ROOT_HORIZONTAL_PADDING_DP).coerceAtLeast(1f)
    val availableHeight = (heightDp - ROOT_VERTICAL_PADDING_DP).coerceAtLeast(1f)
    return min(availableWidth / contentWidth, availableHeight / contentHeight)
}

private const val WIDE_LAYOUT_BIAS = 0.85f
private const val MIN_CONTENT_SCALE = 0.7f
private const val MAX_CONTENT_SCALE = 2.2f
private const val PERCENT_WIDTH_DP = 36f
private const val PERCENT_WITH_ESTIMATE_WIDTH_DP = 92f
private const val BAR_HEIGHT_DP = 4f
private const val BAR_GAP_DP = 3f

enum class LastKnownPlacement {
    NONE,
    OWN_LINE,
    APPEND_TO_LABEL,
    LABEL_SLOT,
}

fun lastKnownPlacement(
    hasLastKnown: Boolean,
    isDualPod: Boolean,
    layout: BatteryLayout,
    labelShown: Boolean,
    heightDp: Float,
    fontScale: Float,
    contentScale: Float = 1f,
): LastKnownPlacement = when {
    !hasLastKnown -> LastKnownPlacement.NONE
    layout == BatteryLayout.TINY_COLUMN -> LastKnownPlacement.NONE
    !labelShown -> LastKnownPlacement.LABEL_SLOT
    heightDp >= requiredHeightForOwnLine(isDualPod, layout, fontScale) * contentScale -> LastKnownPlacement.OWN_LINE
    else -> LastKnownPlacement.APPEND_TO_LABEL
}

fun lastKnownLineText(prefixed: String, bare: String, widthDp: Float, fontScale: Float): String {
    val estimatedWidth = prefixed.length * AVG_CHAR_WIDTH_EM * TEXT_SIZE_SP * fontScale
    return if (estimatedWidth <= widthDp - ROOT_HORIZONTAL_PADDING_DP) prefixed else bare
}

// Rough width estimate for the 12sp last-known line inside the 16dp-padded widget root.
private const val AVG_CHAR_WIDTH_EM = 0.6f
private const val TEXT_SIZE_SP = 12f
private const val ROOT_HORIZONTAL_PADDING_DP = 32f

// Height budget of the GlanceWidgetContent layouts, in dp at font scale 1.0 for text lines.
private const val ROOT_VERTICAL_PADDING_DP = 8f
private const val TEXT_LINE_DP = 16f
private const val SMALL_ICON_DP = 20f
private const val LARGE_ICON_DP = 40f
private const val WIDE_POD_ROW_PADDING_DP = 12f

private fun requiredHeightForOwnLine(
    isDualPod: Boolean,
    layout: BatteryLayout,
    fontScale: Float,
): Float {
    val textLine = TEXT_LINE_DP * fontScale
    val podContent = when {
        !isDualPod -> max(SMALL_ICON_DP, textLine)
        layout == BatteryLayout.WIDE -> WIDE_POD_ROW_PADDING_DP + max(LARGE_ICON_DP, textLine)
        else -> 3 * max(SMALL_ICON_DP, textLine)
    }
    return ROOT_VERTICAL_PADDING_DP + podContent + 2 * textLine
}

sealed class WidgetRenderState {
    abstract val theme: WidgetTheme
    @get:ColorInt abstract val resolvedBgColor: Int
    @get:ColorInt abstract val resolvedTextColor: Int
    @get:ColorInt abstract val resolvedIconColor: Int
    abstract val layout: BatteryLayout

    data class DualPod(
        override val theme: WidgetTheme,
        @ColorInt override val resolvedBgColor: Int,
        @ColorInt override val resolvedTextColor: Int,
        @ColorInt override val resolvedIconColor: Int,
        override val layout: BatteryLayout,
        val deviceLabel: String?,
        @DrawableRes val leftIcon: Int,
        val leftPercent: Float,
        val leftCharging: Boolean,
        val leftInEar: Boolean,
        val leftEstimate: String?,
        @DrawableRes val rightIcon: Int,
        val rightPercent: Float,
        val rightCharging: Boolean,
        val rightInEar: Boolean,
        val rightEstimate: String?,
        @DrawableRes val caseIcon: Int,
        val casePercent: Float,
        val caseCharging: Boolean,
        val lastKnown: String? = null,
    ) : WidgetRenderState()

    data class SinglePod(
        override val theme: WidgetTheme,
        @ColorInt override val resolvedBgColor: Int,
        @ColorInt override val resolvedTextColor: Int,
        @ColorInt override val resolvedIconColor: Int,
        override val layout: BatteryLayout,
        val deviceLabel: String?,
        @DrawableRes val headsetIcon: Int,
        val percent: Float,
        @DrawableRes val batteryIcon: Int,
        val charging: Boolean,
        val worn: Boolean,
        val estimate: String?,
        val lastKnown: String? = null,
    ) : WidgetRenderState()

    data class Message(
        override val theme: WidgetTheme,
        @ColorInt override val resolvedBgColor: Int,
        @ColorInt override val resolvedTextColor: Int,
        @ColorInt override val resolvedIconColor: Int,
        override val layout: BatteryLayout,
        val primaryText: String,
        val secondaryText: String? = null,
    ) : WidgetRenderState()

    data class Loading(
        override val theme: WidgetTheme,
        @ColorInt override val resolvedBgColor: Int,
        @ColorInt override val resolvedTextColor: Int,
        @ColorInt override val resolvedIconColor: Int,
        override val layout: BatteryLayout,
    ) : WidgetRenderState()

    companion object {
        fun previewDualPod(
            theme: WidgetTheme = WidgetTheme.DEFAULT,
            @ColorInt bgColor: Int = 0xFFFFFFFF.toInt(),
            @ColorInt textColor: Int = 0xFF1E1E1E.toInt(),
            @ColorInt iconColor: Int = 0xFF1E1E1E.toInt(),
            layout: BatteryLayout = BatteryLayout.NARROW,
        ): DualPod = DualPod(
            theme = theme,
            resolvedBgColor = bgColor,
            resolvedTextColor = textColor,
            resolvedIconColor = iconColor,
            layout = layout,
            deviceLabel = "My AirPods Pro",
            leftIcon = R.drawable.device_airpods_pro2_left,
            leftPercent = 0.85f,
            leftCharging = false,
            leftInEar = true,
            leftEstimate = "4h 55m",
            rightIcon = R.drawable.device_airpods_pro2_right,
            rightPercent = 0.92f,
            rightCharging = true,
            rightInEar = false,
            rightEstimate = "25m",
            caseIcon = R.drawable.device_airpods_pro2_case,
            casePercent = 1.0f,
            caseCharging = false,
        )

        fun previewSinglePod(
            theme: WidgetTheme = WidgetTheme.DEFAULT,
            @ColorInt bgColor: Int = 0xFFFFFFFF.toInt(),
            @ColorInt textColor: Int = 0xFF1E1E1E.toInt(),
            @ColorInt iconColor: Int = 0xFF1E1E1E.toInt(),
            layout: BatteryLayout = BatteryLayout.NARROW,
        ): SinglePod = SinglePod(
            theme = theme,
            resolvedBgColor = bgColor,
            resolvedTextColor = textColor,
            resolvedIconColor = iconColor,
            layout = layout,
            deviceLabel = "My AirPods Max",
            headsetIcon = R.drawable.device_airpods_max,
            percent = 0.72f,
            batteryIcon = R.drawable.ic_baseline_battery_3_bar_24,
            charging = false,
            worn = true,
            estimate = "14h 30m",
        )
    }
}

/**
 * Replaces the preview factory's hardcoded sample estimates with properly localized ones for
 * user-facing preview surfaces (widget picker, configuration screen, store screenshots).
 */
fun WidgetRenderState.DualPod.withLocalizedPreviewEstimates(context: Context): WidgetRenderState.DualPod = copy(
    leftEstimate = formatBatteryDurationShort(context, 295),
    rightEstimate = formatBatteryDurationShort(context, 25),
)
