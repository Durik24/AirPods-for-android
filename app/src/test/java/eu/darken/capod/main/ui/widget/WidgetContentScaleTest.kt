package eu.darken.capod.main.ui.widget

import io.kotest.matchers.floats.shouldBeGreaterThan
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class WidgetContentScaleTest : BaseTest() {

    private fun scale(
        widthDp: Float,
        heightDp: Float,
        theme: WidgetTheme = WidgetTheme(),
        layout: BatteryLayout = BatteryLayout.NARROW,
        isDualPod: Boolean = true,
    ) = widgetContentScale(layout, isDualPod, theme, widthDp, heightDp)

    @Test
    fun `tiny column is never scaled`() {
        scale(40f, 400f, layout = BatteryLayout.TINY_COLUMN) shouldBe 1f
    }

    @Test
    fun `auto size grows when the widget gets bigger`() {
        val small = scale(180f, 100f)
        val large = scale(360f, 220f)
        large shouldBeGreaterThan small
        large shouldBeGreaterThan 1.5f
    }

    @Test
    fun `fixed size does not grow past its own scale`() {
        val theme = WidgetTheme(contentSize = WidgetTheme.ContentSize.SMALL)
        scale(400f, 400f, theme) shouldBe 0.85f
    }

    @Test
    fun `fixed large size shrinks when it would not fit`() {
        val theme = WidgetTheme(contentSize = WidgetTheme.ContentSize.LARGE)
        scale(180f, 100f, theme) shouldBeLessThan 1.3f
    }

    @Test
    fun `scale is clamped on very small and very large widgets`() {
        scale(60f, 30f) shouldBe 0.7f
        scale(2000f, 2000f) shouldBe 2.2f
    }

    @Test
    fun `hiding content lets the rest grow`() {
        val full = scale(200f, 120f)
        val trimmed = scale(
            200f,
            120f,
            WidgetTheme(showCase = false, showEstimates = false, showStatusIcons = false, showDeviceLabel = false),
        )
        trimmed shouldBeGreaterThan full
    }

    @Test
    fun `wide layout honours the case toggle`() {
        val withCase = scale(300f, 200f, layout = BatteryLayout.WIDE)
        val withoutCase = scale(300f, 200f, WidgetTheme(showCase = false), layout = BatteryLayout.WIDE)
        withoutCase shouldBeGreaterThan withCase
    }
}
