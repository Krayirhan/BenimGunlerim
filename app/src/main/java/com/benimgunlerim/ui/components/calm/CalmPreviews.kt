package com.benimgunlerim.ui.components.calm

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.benimgunlerim.ui.theme.BenimGunlerimTheme

@Preview(name = "Nefes alma", showBackground = true)
@Composable
fun BreathingCirclePreview() {
    BenimGunlerimTheme {
        BreathingCircle(isExhaling = false)
    }
}

@Preview(name = "Nefes verme", showBackground = true)
@Composable
fun BreathingCircleExhalingPreview() {
    BenimGunlerimTheme {
        BreathingCircle(isExhaling = true)
    }
}
