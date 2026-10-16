package eu.darken.capod.main.ui.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import eu.darken.capod.R
import eu.darken.capod.main.ui.MainActivity
import eu.darken.capod.pods.core.apple.ble.isKnownBattery
import kotlin.math.roundToInt

@Composable
fun GlanceWidgetContent(
    state: WidgetRenderState,
    context: android.content.Context,
) {
    val openApp = actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )

    when (state) {
        is WidgetRenderState.DualPod -> GlanceDualPod(state, GlanceModifier.clickable(openApp))
        is WidgetRenderState.SinglePod -> GlanceSinglePod(state, GlanceModifier.clickable(openApp))
        is WidgetRenderState.Message -> GlanceMessage(state, GlanceModifier.clickable(openApp))
        is WidgetRenderState.Loading -> GlanceLoading(state, GlanceModifier.clickable(openApp))
    }
}

/** Sizes for one render pass, derived from the widget's current size and the user's size choice. */
private class GlanceDimens(val scale: Float) {
    val smallIcon: Dp = (20 * scale).dp
    val largeIcon: Dp = (40 * scale).dp
    val statusIcon: Dp = (16 * scale).dp
    val gap: Dp = (12 * scale).dp
    val textGap: Dp = (4 * scale).dp
    val percentSize: TextUnit = (13 * scale).sp
    val secondarySize: TextUnit = (11 * scale).sp
    val labelSize: TextUnit = (12 * scale).sp
    val barHeight: Dp = (4 * scale).dp
    val barGap: Dp = (3 * scale).dp
    val narrowBarWidth: Dp = (56 * scale).dp
    val wideBarWidth: Dp = (76 * scale).dp
}

@Composable
private fun widgetDimens(state: WidgetRenderState, isDualPod: Boolean): GlanceDimens {
    val size = LocalSize.current
    return GlanceDimens(
        widgetContentScale(
            layout = state.layout,
            isDualPod = isDualPod,
            theme = state.theme,
            widthDp = size.width.value,
            heightDp = size.height.value,
        )
    )
}

@Composable
private fun GlanceDualPod(
    state: WidgetRenderState.DualPod,
    clickModifier: GlanceModifier,
) {
    val theme = state.theme
    val iconTint = ColorFilter.tint(fixedColor(state.resolvedIconColor))
    val dimens = widgetDimens(state, isDualPod = true)
    val colors = PodColors(state.resolvedTextColor, state.resolvedIconColor)

    when (state.layout) {
        BatteryLayout.WIDE -> {
            GlanceWidgetRoot(state.resolvedBgColor, theme.cornerRadius, clickModifier) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlanceWidePodItem(state.leftIcon, state.leftPercent, state.leftCharging, state.leftInEar, state.leftEstimate, theme, colors, iconTint, dimens)
                    if (theme.showCase) {
                        Spacer(GlanceModifier.width(dimens.gap))
                        GlanceWidePodItem(state.caseIcon, state.casePercent, state.caseCharging, false, null, theme, colors, iconTint, dimens)
                    }
                    Spacer(GlanceModifier.width(dimens.gap))
                    GlanceWidePodItem(state.rightIcon, state.rightPercent, state.rightCharging, state.rightInEar, state.rightEstimate, theme, colors, iconTint, dimens)
                }
                GlanceLabelWithLastKnown(state.deviceLabel, theme.showDeviceLabel, state.lastKnown, isDualPod = true, state.layout, state.resolvedTextColor, dimens)
            }
        }

        BatteryLayout.NARROW -> {
            GlanceWidgetRoot(state.resolvedBgColor, theme.cornerRadius, clickModifier) {
                GlancePodRow(state.leftIcon, state.leftPercent, state.leftCharging, state.leftInEar, state.leftEstimate, theme, colors, iconTint, dimens)
                GlancePodRow(state.rightIcon, state.rightPercent, state.rightCharging, state.rightInEar, state.rightEstimate, theme, colors, iconTint, dimens)
                if (theme.showCase) {
                    GlancePodRow(state.caseIcon, state.casePercent, state.caseCharging, false, null, theme, colors, iconTint, dimens)
                }
                GlanceLabelWithLastKnown(state.deviceLabel, theme.showDeviceLabel, state.lastKnown, isDualPod = true, state.layout, state.resolvedTextColor, dimens)
            }
        }

        BatteryLayout.TINY_COLUMN -> {
            val textStyle = TextStyle(color = fixedColor(state.resolvedTextColor), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            GlanceWidgetRoot(
                bgColor = state.resolvedBgColor,
                cornerRadius = theme.cornerRadius,
                clickModifier = clickModifier,
                horizontalPadding = 4.dp,
                verticalPadding = 0.dp,
            ) {
                GlanceTinyPodItem(state.leftIcon, state.leftPercent, textStyle, iconTint)
                GlanceTinyPodItem(state.rightIcon, state.rightPercent, textStyle, iconTint)
                if (theme.showCase) {
                    GlanceTinyPodItem(state.caseIcon, state.casePercent, textStyle, iconTint)
                }
            }
        }
    }
}

@Composable
private fun GlanceSinglePod(
    state: WidgetRenderState.SinglePod,
    clickModifier: GlanceModifier,
) {
    val theme = state.theme
    val iconTint = ColorFilter.tint(fixedColor(state.resolvedIconColor))

    when (state.layout) {
        BatteryLayout.WIDE, BatteryLayout.NARROW -> {
            val dimens = widgetDimens(state, isDualPod = false)
            val colors = PodColors(state.resolvedTextColor, state.resolvedIconColor)
            GlanceWidgetRoot(state.resolvedBgColor, theme.cornerRadius, clickModifier) {
                GlancePodRow(state.batteryIcon, state.percent, state.charging, state.worn, state.estimate, theme, colors, iconTint, dimens)
                GlanceLabelWithLastKnown(state.deviceLabel, theme.showDeviceLabel, state.lastKnown, isDualPod = false, state.layout, state.resolvedTextColor, dimens)
            }
        }

        BatteryLayout.TINY_COLUMN -> {
            val textStyle = TextStyle(color = fixedColor(state.resolvedTextColor), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            GlanceWidgetRoot(
                bgColor = state.resolvedBgColor,
                cornerRadius = theme.cornerRadius,
                clickModifier = clickModifier,
                horizontalPadding = 4.dp,
                verticalPadding = 0.dp,
            ) {
                GlanceTinyPodItem(state.batteryIcon, state.percent, textStyle, iconTint)
            }
        }
    }
}

@Composable
private fun GlanceMessage(
    state: WidgetRenderState.Message,
    clickModifier: GlanceModifier,
) {
    val isCompact = state.layout == BatteryLayout.TINY_COLUMN
    val fontSize = if (isCompact) 10.sp else 12.sp
    val hPad = if (isCompact) 4.dp else 16.dp
    val vPad = if (isCompact) 0.dp else 4.dp

    GlanceWidgetRoot(
        bgColor = state.resolvedBgColor,
        cornerRadius = state.theme.cornerRadius,
        clickModifier = clickModifier,
        horizontalPadding = hPad,
        verticalPadding = vPad,
    ) {
        Text(
            text = state.primaryText,
            style = TextStyle(
                color = fixedColor(state.resolvedTextColor),
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            modifier = GlanceModifier.fillMaxWidth(),
            maxLines = if (isCompact) 2 else Int.MAX_VALUE,
        )
        if (!isCompact && state.secondaryText != null) {
            Text(
                text = state.secondaryText,
                style = TextStyle(
                    color = fadedColor(state.resolvedTextColor),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
                modifier = GlanceModifier.fillMaxWidth(),
                maxLines = 4,
            )
        }
    }
}

@Composable
private fun GlanceLoading(
    state: WidgetRenderState.Loading,
    clickModifier: GlanceModifier,
) {
    val isCompact = state.layout == BatteryLayout.TINY_COLUMN
    GlanceWidgetRoot(
        bgColor = state.resolvedBgColor,
        cornerRadius = state.theme.cornerRadius,
        clickModifier = clickModifier,
        horizontalPadding = if (isCompact) 4.dp else 16.dp,
        verticalPadding = if (isCompact) 0.dp else 4.dp,
    ) {
        Text(
            text = "…",
            style = TextStyle(
                color = fixedColor(state.resolvedTextColor),
                fontSize = if (isCompact) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            modifier = GlanceModifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun GlanceWidgetRoot(
    bgColor: Int,
    cornerRadius: Int,
    clickModifier: GlanceModifier,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 4.dp,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = clickModifier
            .fillMaxSize()
            .background(fixedColor(bgColor))
            .cornerRadius(WidgetTheme.clampCornerRadius(cornerRadius).dp)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

private class PodColors(textColor: Int, iconColor: Int) {
    val text = fixedColor(textColor)
    val secondary = fadedColor(textColor)
    val bar = fixedColor(iconColor)
    val barLow = fixedColor(LOW_BATTERY_COLOR)
    val barTrack = ColorProvider(Color(iconColor).copy(alpha = 0.22f))
}

/** Icon on top, percentage next to the status icons, estimate and battery bar below. */
@Composable
private fun GlanceWidePodItem(
    icon: Int,
    percent: Float,
    charging: Boolean,
    inEar: Boolean,
    estimate: String?,
    theme: WidgetTheme,
    colors: PodColors,
    iconTint: ColorFilter,
    dimens: GlanceDimens,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = null,
                modifier = GlanceModifier.size(dimens.largeIcon),
                colorFilter = iconTint,
            )
            Text(
                text = formatGlancePercent(percent),
                style = TextStyle(color = colors.text, fontSize = dimens.percentSize, fontWeight = FontWeight.Medium),
                maxLines = 1,
                modifier = GlanceModifier.padding(horizontal = dimens.textGap),
            )
            GlanceStatusIcons(charging, inEar, theme, iconTint, dimens)
        }
        if (theme.showEstimates && estimate != null) {
            Text(
                text = estimate,
                style = TextStyle(color = colors.secondary, fontSize = dimens.secondarySize),
                maxLines = 1,
            )
        }
        if (theme.showBatteryBars) {
            Spacer(GlanceModifier.height(dimens.barGap))
            GlanceBatteryBar(percent, colors, dimens.wideBarWidth, dimens.barHeight)
        }
    }
}

/** One pod per line: icon, percentage, estimate and status icons, with an optional battery bar under it. */
@Composable
private fun GlancePodRow(
    icon: Int,
    percent: Float,
    charging: Boolean,
    inEar: Boolean,
    estimate: String?,
    theme: WidgetTheme,
    colors: PodColors,
    iconTint: ColorFilter,
    dimens: GlanceDimens,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = null,
                modifier = GlanceModifier.size(dimens.smallIcon),
                colorFilter = iconTint,
            )
            Text(
                text = formatGlancePercent(percent),
                style = TextStyle(color = colors.text, fontSize = dimens.percentSize, fontWeight = FontWeight.Medium),
                maxLines = 1,
                modifier = GlanceModifier.padding(start = dimens.textGap),
            )
            if (theme.showEstimates && estimate != null) {
                Text(
                    text = " · $estimate",
                    style = TextStyle(color = colors.secondary, fontSize = dimens.secondarySize),
                    maxLines = 1,
                )
            }
            Spacer(GlanceModifier.width(dimens.textGap))
            GlanceStatusIcons(charging, inEar, theme, iconTint, dimens)
        }
        if (theme.showBatteryBars) {
            GlanceBatteryBar(percent, colors, dimens.narrowBarWidth, dimens.barHeight)
            Spacer(GlanceModifier.height(dimens.barGap))
        }
    }
}

@Composable
private fun GlanceStatusIcons(
    charging: Boolean,
    inEar: Boolean,
    theme: WidgetTheme,
    iconTint: ColorFilter,
    dimens: GlanceDimens,
) {
    if (!theme.showStatusIcons) return
    if (charging) {
        Image(
            provider = ImageProvider(R.drawable.ic_baseline_power_24),
            contentDescription = null,
            modifier = GlanceModifier.size(dimens.statusIcon),
            colorFilter = iconTint,
        )
    }
    if (inEar) {
        Image(
            provider = ImageProvider(R.drawable.ic_baseline_hearing_24),
            contentDescription = null,
            modifier = GlanceModifier.size(dimens.statusIcon),
            colorFilter = iconTint,
        )
    }
}

@Composable
private fun GlanceBatteryBar(
    percent: Float,
    colors: PodColors,
    width: Dp,
    height: Dp,
) {
    val known = isKnownBattery(percent)
    LinearProgressIndicator(
        progress = if (known) percent.coerceIn(0f, 1f) else 0f,
        modifier = GlanceModifier.width(width).height(height).cornerRadius(height / 2),
        color = if (known && percent <= LOW_BATTERY_THRESHOLD) colors.barLow else colors.bar,
        backgroundColor = colors.barTrack,
    )
}

@Composable
private fun GlanceTinyPodItem(
    icon: Int,
    percent: Float,
    textStyle: TextStyle,
    iconTint: ColorFilter,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(icon),
            contentDescription = null,
            modifier = GlanceModifier.size(14.dp),
            colorFilter = iconTint,
        )
        Text(
            text = formatGlancePercent(percent),
            style = textStyle,
            maxLines = 1,
            modifier = GlanceModifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun GlanceDeviceLabel(
    label: String?,
    visible: Boolean,
    textColor: Int,
    dimens: GlanceDimens,
    maxLines: Int = Int.MAX_VALUE,
) {
    if (visible && label != null) {
        Text(
            text = label,
            style = TextStyle(
                color = fadedColor(textColor),
                fontSize = dimens.labelSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            maxLines = maxLines,
            modifier = GlanceModifier.padding(top = dimens.textGap),
        )
    }
}

@Composable
private fun GlanceLabelWithLastKnown(
    label: String?,
    showLabel: Boolean,
    lastKnown: String?,
    isDualPod: Boolean,
    layout: BatteryLayout,
    textColor: Int,
    dimens: GlanceDimens,
) {
    val context = LocalContext.current
    val shownLabel = label?.takeIf { showLabel }
    val fontScale = context.resources.configuration.fontScale
    val placement = lastKnownPlacement(
        hasLastKnown = lastKnown != null,
        isDualPod = isDualPod,
        layout = layout,
        labelShown = shownLabel != null,
        heightDp = LocalSize.current.height.value,
        fontScale = fontScale,
        contentScale = dimens.scale,
    )
    if (lastKnown == null || placement == LastKnownPlacement.NONE) {
        GlanceDeviceLabel(label, showLabel, textColor, dimens, maxLines = 2)
        return
    }

    val lastKnownStyle = TextStyle(
        color = fadedColor(textColor),
        fontSize = dimens.secondarySize,
        textAlign = TextAlign.Center,
    )
    val lastKnownLine = lastKnownLineText(
        prefixed = context.getString(R.string.battery_cached_label, lastKnown),
        bare = lastKnown,
        widthDp = LocalSize.current.width.value,
        fontScale = fontScale * dimens.scale,
    )
    when (placement) {
        LastKnownPlacement.OWN_LINE -> {
            GlanceDeviceLabel(label, showLabel, textColor, dimens, maxLines = 1)
            Text(
                text = lastKnownLine,
                style = lastKnownStyle,
                maxLines = 1,
            )
        }

        LastKnownPlacement.LABEL_SLOT -> Text(
            text = lastKnownLine,
            style = lastKnownStyle,
            maxLines = 1,
            modifier = GlanceModifier.padding(top = dimens.textGap),
        )

        LastKnownPlacement.APPEND_TO_LABEL -> if (shownLabel != null) {
            val labelStyle = lastKnownStyle.copy(fontWeight = FontWeight.Medium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.padding(top = dimens.textGap),
            ) {
                Text(
                    text = shownLabel,
                    style = labelStyle,
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(
                    text = " · $lastKnown",
                    style = labelStyle,
                    maxLines = 1,
                )
            }
        }

        LastKnownPlacement.NONE -> Unit
    }
}

private const val LOW_BATTERY_THRESHOLD = 0.2f
private const val LOW_BATTERY_COLOR = 0xFFE53935.toInt()

private fun fixedColor(argb: Int): ColorProvider = ColorProvider(Color(argb))

private fun fadedColor(argb: Int): ColorProvider = ColorProvider(Color(argb).copy(alpha = 0.72f))

private fun formatGlancePercent(percent: Float): String =
    if (isKnownBattery(percent)) "${(percent * 100).roundToInt()}%" else "—"
