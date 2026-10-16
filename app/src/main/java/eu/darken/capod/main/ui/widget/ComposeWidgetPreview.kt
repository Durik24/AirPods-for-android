package eu.darken.capod.main.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.darken.capod.R
import eu.darken.capod.common.compose.Preview2
import eu.darken.capod.common.compose.PreviewWrapper
import eu.darken.capod.pods.core.apple.ble.isKnownBattery
import kotlin.math.roundToInt

@Composable
fun ComposeWidgetPreview(
    state: WidgetRenderState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is WidgetRenderState.DualPod -> DualPodPreview(state, modifier)
        is WidgetRenderState.SinglePod -> SinglePodPreview(state, modifier)
        is WidgetRenderState.Message -> MessagePreview(state, modifier)
        is WidgetRenderState.Loading -> LoadingPreview(state, modifier)
    }
}

@Composable
fun CheckerboardBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val lightColor = Color(0xFFE8E8E8)
    val darkColor = Color(0xFFD0D0D0)

    Box(
        modifier = modifier.clipToBounds().drawBehind {
            val cellSize = 8.dp.toPx()
            val cols = (size.width / cellSize).toInt() + 1
            val rows = (size.height / cellSize).toInt() + 1
            drawRect(lightColor)
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    if ((row + col) % 2 == 0) {
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = Size(cellSize, cellSize),
                        )
                    }
                }
            }
        },
    ) {
        content()
    }
}

@Composable
private fun DualPodPreview(
    state: WidgetRenderState.DualPod,
    modifier: Modifier = Modifier,
) {
    val theme = state.theme
    val bgColor = Color(state.resolvedBgColor)
    val textColor = Color(state.resolvedTextColor)
    val iconColor = Color(state.resolvedIconColor)
    val iconTint = ColorFilter.tint(iconColor)
    val dimens = PreviewDimens(theme.contentSize.fixedScale ?: 1f)

    when (state.layout) {
        BatteryLayout.WIDE -> {
            WidgetContainer(bgColor = bgColor, cornerRadius = theme.cornerRadius, modifier = modifier) {
                Row(
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(dimens.gap, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WidePodItem(state.leftIcon, state.leftPercent, state.leftCharging, state.leftInEar, state.leftEstimate, theme, textColor, iconColor, iconTint, dimens)
                    if (theme.showCase) {
                        WidePodItem(state.caseIcon, state.casePercent, state.caseCharging, false, null, theme, textColor, iconColor, iconTint, dimens)
                    }
                    WidePodItem(state.rightIcon, state.rightPercent, state.rightCharging, state.rightInEar, state.rightEstimate, theme, textColor, iconColor, iconTint, dimens)
                }
                DeviceLabel(
                    label = state.deviceLabel,
                    visible = theme.showDeviceLabel,
                    textColor = textColor,
                    dimens = dimens,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }

        BatteryLayout.NARROW -> {
            WidgetContainer(bgColor = bgColor, cornerRadius = theme.cornerRadius, modifier = modifier) {
                PodItemRow(state.leftIcon, state.leftPercent, state.leftCharging, state.leftInEar, state.leftEstimate, theme, textColor, iconColor, iconTint, dimens)
                PodItemRow(state.rightIcon, state.rightPercent, state.rightCharging, state.rightInEar, state.rightEstimate, theme, textColor, iconColor, iconTint, dimens)
                if (theme.showCase) {
                    PodItemRow(state.caseIcon, state.casePercent, state.caseCharging, false, null, theme, textColor, iconColor, iconTint, dimens)
                }
                DeviceLabel(
                    label = state.deviceLabel,
                    visible = theme.showDeviceLabel,
                    textColor = textColor,
                    dimens = dimens,
                )
            }
        }

        BatteryLayout.TINY_COLUMN -> {
            WidgetContainer(
                bgColor = bgColor,
                cornerRadius = theme.cornerRadius,
                modifier = modifier,
                horizontalPadding = 4.dp,
                verticalPadding = 0.dp,
            ) {
                TinyPodItem(state.leftIcon, state.leftPercent, textColor, iconTint)
                TinyPodItem(state.rightIcon, state.rightPercent, textColor, iconTint)
                if (theme.showCase) {
                    TinyPodItem(state.caseIcon, state.casePercent, textColor, iconTint)
                }
            }
        }
    }
}

@Composable
private fun SinglePodPreview(
    state: WidgetRenderState.SinglePod,
    modifier: Modifier = Modifier,
) {
    val theme = state.theme
    val bgColor = Color(state.resolvedBgColor)
    val textColor = Color(state.resolvedTextColor)
    val iconColor = Color(state.resolvedIconColor)
    val iconTint = ColorFilter.tint(iconColor)
    val dimens = PreviewDimens(theme.contentSize.fixedScale ?: 1f)

    when (state.layout) {
        BatteryLayout.WIDE, BatteryLayout.NARROW -> {
            WidgetContainer(bgColor = bgColor, cornerRadius = theme.cornerRadius, modifier = modifier) {
                PodItemRow(state.batteryIcon, state.percent, state.charging, state.worn, state.estimate, theme, textColor, iconColor, iconTint, dimens)
                DeviceLabel(
                    label = state.deviceLabel,
                    visible = theme.showDeviceLabel,
                    textColor = textColor,
                    dimens = dimens,
                )
            }
        }

        BatteryLayout.TINY_COLUMN -> {
            WidgetContainer(
                bgColor = bgColor,
                cornerRadius = theme.cornerRadius,
                modifier = modifier,
                horizontalPadding = 4.dp,
                verticalPadding = 0.dp,
            ) {
                TinyPodItem(state.batteryIcon, state.percent, textColor, iconTint)
            }
        }
    }
}

@Composable
private fun MessagePreview(
    state: WidgetRenderState.Message,
    modifier: Modifier = Modifier,
) {
    val bgColor = Color(state.resolvedBgColor)
    val textColor = Color(state.resolvedTextColor)
    val isCompact = state.layout == BatteryLayout.TINY_COLUMN

    WidgetContainer(
        bgColor = bgColor,
        cornerRadius = state.theme.cornerRadius,
        modifier = modifier,
        horizontalPadding = if (isCompact) 4.dp else 16.dp,
        verticalPadding = if (isCompact) 0.dp else 4.dp,
    ) {
        Text(
            text = state.primaryText,
            fontSize = if (isCompact) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = if (isCompact) 2 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!isCompact && state.secondaryText != null) {
            Text(
                text = state.secondaryText,
                fontSize = 12.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LoadingPreview(
    state: WidgetRenderState.Loading,
    modifier: Modifier = Modifier,
) {
    val bgColor = Color(state.resolvedBgColor)
    val textColor = Color(state.resolvedTextColor)
    val isCompact = state.layout == BatteryLayout.TINY_COLUMN

    WidgetContainer(
        bgColor = bgColor,
        cornerRadius = state.theme.cornerRadius,
        modifier = modifier,
        horizontalPadding = if (isCompact) 4.dp else 16.dp,
        verticalPadding = if (isCompact) 0.dp else 4.dp,
    ) {
        Text(
            text = "…",
            fontSize = if (isCompact) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WidgetContainer(
    bgColor: Color,
    cornerRadius: Int,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 4.dp,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(WidgetTheme.clampCornerRadius(cornerRadius).dp))
            .background(bgColor)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        content()
    }
}

private class PreviewDimens(scale: Float) {
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
private fun WidePodItem(
    icon: Int,
    percent: Float,
    charging: Boolean,
    inEar: Boolean,
    estimate: String?,
    theme: WidgetTheme,
    textColor: Color,
    iconColor: Color,
    iconTint: ColorFilter,
    dimens: PreviewDimens,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(dimens.largeIcon),
                colorFilter = iconTint,
            )
            Text(
                text = formatPercent(percent),
                fontSize = dimens.percentSize,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = dimens.textGap),
            )
            StatusIcons(charging, inEar, theme, iconTint, dimens)
        }
        if (theme.showEstimates && estimate != null) {
            Text(
                text = estimate,
                fontSize = dimens.secondarySize,
                color = textColor.copy(alpha = FADED_ALPHA),
                maxLines = 1,
            )
        }
        if (theme.showBatteryBars) {
            Spacer(modifier = Modifier.height(dimens.barGap))
            BatteryBar(percent, iconColor, dimens.wideBarWidth, dimens.barHeight)
        }
    }
}

@Composable
private fun PodItemRow(
    icon: Int,
    percent: Float,
    charging: Boolean,
    inEar: Boolean,
    estimate: String?,
    theme: WidgetTheme,
    textColor: Color,
    iconColor: Color,
    iconTint: ColorFilter,
    dimens: PreviewDimens,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(dimens.smallIcon),
                colorFilter = iconTint,
            )
            Text(
                text = formatPercent(percent),
                fontSize = dimens.percentSize,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                modifier = Modifier.padding(start = dimens.textGap),
            )
            if (theme.showEstimates && estimate != null) {
                Text(
                    text = " · $estimate",
                    fontSize = dimens.secondarySize,
                    color = textColor.copy(alpha = FADED_ALPHA),
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.width(dimens.textGap))
            StatusIcons(charging, inEar, theme, iconTint, dimens)
        }
        if (theme.showBatteryBars) {
            BatteryBar(percent, iconColor, dimens.narrowBarWidth, dimens.barHeight)
            Spacer(modifier = Modifier.height(dimens.barGap))
        }
    }
}

@Composable
private fun StatusIcons(
    charging: Boolean,
    inEar: Boolean,
    theme: WidgetTheme,
    iconTint: ColorFilter,
    dimens: PreviewDimens,
) {
    if (!theme.showStatusIcons) return
    if (charging) {
        Image(
            painter = painterResource(R.drawable.ic_baseline_power_24),
            contentDescription = null,
            modifier = Modifier.size(dimens.statusIcon),
            colorFilter = iconTint,
        )
    }
    if (inEar) {
        Image(
            painter = painterResource(R.drawable.ic_baseline_hearing_24),
            contentDescription = null,
            modifier = Modifier.size(dimens.statusIcon),
            colorFilter = iconTint,
        )
    }
}

@Composable
private fun BatteryBar(
    percent: Float,
    iconColor: Color,
    width: Dp,
    height: Dp,
) {
    val known = isKnownBattery(percent)
    val fill = if (known) percent.coerceIn(0f, 1f) else 0f
    val barColor = if (known && percent <= 0.2f) Color(0xFFE53935) else iconColor
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(iconColor.copy(alpha = 0.22f)),
    ) {
        Box(
            modifier = Modifier
                .width(width * fill)
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(barColor),
        )
    }
}

@Composable
private fun TinyPodItem(
    icon: Int,
    percent: Float,
    textColor: Color,
    iconTint: ColorFilter,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            colorFilter = iconTint,
        )
        Text(
            text = formatPercent(percent),
            fontSize = 12.sp,
            color = textColor,
            maxLines = 1,
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

@Composable
private fun DeviceLabel(
    label: String?,
    visible: Boolean,
    textColor: Color,
    dimens: PreviewDimens,
    modifier: Modifier = Modifier,
) {
    if (visible && label != null) {
        Text(
            text = label,
            fontSize = dimens.labelSize,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = FADED_ALPHA),
            textAlign = TextAlign.Center,
            modifier = modifier.padding(top = dimens.textGap),
        )
    }
}

private const val FADED_ALPHA = 0.72f

private fun formatPercent(percent: Float): String =
    if (isKnownBattery(percent)) "${(percent * 100).roundToInt()}%" else "—"

@Preview2
@Composable
private fun PreviewDualTiny11() = PreviewWrapper {
    ComposeWidgetPreview(
        state = WidgetRenderState.previewDualPod(layout = BatteryLayout.TINY_COLUMN),
        modifier = Modifier.size(40.dp),
    )
}

@Preview2
@Composable
private fun PreviewDualTinyTall() = PreviewWrapper {
    ComposeWidgetPreview(
        state = WidgetRenderState.previewDualPod(layout = BatteryLayout.TINY_COLUMN),
        modifier = Modifier.size(width = 40.dp, height = 110.dp),
    )
}

@Preview2
@Composable
private fun PreviewDualNarrow() = PreviewWrapper {
    ComposeWidgetPreview(state = WidgetRenderState.previewDualPod(layout = BatteryLayout.NARROW))
}

@Preview2
@Composable
private fun PreviewDualWide() = PreviewWrapper {
    ComposeWidgetPreview(state = WidgetRenderState.previewDualPod(layout = BatteryLayout.WIDE))
}
