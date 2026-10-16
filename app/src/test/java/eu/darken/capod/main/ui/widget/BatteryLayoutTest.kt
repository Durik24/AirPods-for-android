package eu.darken.capod.main.ui.widget

import eu.darken.capod.main.ui.widget.BatteryLayout.NARROW
import eu.darken.capod.main.ui.widget.BatteryLayout.TINY_COLUMN
import eu.darken.capod.main.ui.widget.BatteryLayout.WIDE
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BatteryLayoutTest : BaseTest() {

    @Test fun `1 cell wide resolves to tiny column`() {
        BatteryLayout.forCells(1) shouldBe TINY_COLUMN
    }

    @Test fun `2 cells wide resolves to narrow`() {
        BatteryLayout.forCells(2) shouldBe NARROW
    }

    @Test fun `3 cells wide resolves to narrow`() {
        BatteryLayout.forCells(3) shouldBe NARROW
    }

    @Test fun `4 cells wide resolves to narrow`() {
        BatteryLayout.forCells(4) shouldBe NARROW
    }

    @Test fun `5 cells wide resolves to wide`() {
        BatteryLayout.forCells(5) shouldBe WIDE
    }

    @Test fun `8 cells wide resolves to wide`() {
        BatteryLayout.forCells(8) shouldBe WIDE
    }

    @Test fun `zero cells resolves to tiny column not thrown`() {
        BatteryLayout.forCells(0) shouldBe TINY_COLUMN
    }

    @Test fun `one cell wide stays tiny regardless of style`() {
        WidgetTheme.LayoutStyle.entries.forEach { style ->
            BatteryLayout.forSize(60f, 200f, WidgetTheme(layoutStyle = style), isDualPod = true) shouldBe TINY_COLUMN
        }
    }

    @Test fun `auto picks a row for a short wide widget`() {
        BatteryLayout.forSize(320f, 90f, WidgetTheme(), isDualPod = true) shouldBe WIDE
    }

    @Test fun `auto picks a column for a small square widget`() {
        BatteryLayout.forSize(150f, 150f, WidgetTheme(), isDualPod = true) shouldBe NARROW
    }

    @Test fun `auto picks a column for a 2x1 widget`() {
        BatteryLayout.forSize(150f, 70f, WidgetTheme(), isDualPod = true) shouldBe NARROW
    }

    @Test fun `forced styles win over the size`() {
        BatteryLayout.forSize(150f, 150f, WidgetTheme(layoutStyle = WidgetTheme.LayoutStyle.HORIZONTAL), isDualPod = true) shouldBe WIDE
        BatteryLayout.forSize(400f, 80f, WidgetTheme(layoutStyle = WidgetTheme.LayoutStyle.VERTICAL), isDualPod = true) shouldBe NARROW
    }

    @Test fun `cells for width matches launcher cell formula`() {
        BatteryLayout.cellsForWidth(40f) shouldBe 1
        BatteryLayout.cellsForWidth(110f) shouldBe 2
        BatteryLayout.cellsForWidth(250f) shouldBe 4
        BatteryLayout.cellsForWidth(320f) shouldBe 5
    }
}
