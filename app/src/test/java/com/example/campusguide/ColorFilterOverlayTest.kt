import androidx.compose.ui.graphics.Color
import com.example.campusguide.ui.accessibility.ColorBlindMode
import com.example.campusguide.ui.accessibility.overlayColorForMode
import org.junit.Assert.*
import org.junit.Test

class ColorFilterOverlayTest {

    @Test
    fun `NONE mode results in fully transparent overlay`() {
        assertEquals(Color.Transparent, overlayColorForMode(ColorBlindMode.NONE))
    }

    @Test
    fun `other modes have semi transparent overlay`() {
        listOf(
            ColorBlindMode.PROTANOPIA,
            ColorBlindMode.DEUTERANOPIA,
            ColorBlindMode.TRITANOPIA
        ).forEach { mode ->
            val color = overlayColorForMode(mode)
            assertNotNull("Expected overlay color for $mode", color)

            val alpha = color.alpha
            assertTrue("alpha should be > 0", alpha > 0f)
            assertTrue("alpha should be < 1", alpha < 1f)
        }
    }
}