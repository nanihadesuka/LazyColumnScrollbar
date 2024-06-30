package my.nanihadesuka.compose

import org.junit.Test

class ScrollbarSettingsTest {

    @Test(expected = IllegalArgumentException::class)
    fun `min thumb value is greater than max thumb value - crash`() {
        ScrollbarSettings(
            thumbMinLength = 0.8f,
            thumbMaxLength = 0.5f,
        )
    }

    @Test
    fun `min thumb value is not greater than max thumb value - success`() {
        ScrollbarSettings(
            thumbMinLength = 0.5f,
            thumbMaxLength = 0.8f,
        )
    }

    @Test
    fun `min thumb value is equal than max thumb value - success`() {
        ScrollbarSettings(
            thumbMinLength = 0.5f,
            thumbMaxLength = 0.5f,
        )
    }
}