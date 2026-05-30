package com.precioluz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.precioluz.app.domain.model.DayPrices
import com.precioluz.app.domain.model.PriceTier

// Tarjeta de recomendación: mejor ventana + hora más cara
@Composable
fun RecoCard(
    day: DayPrices,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val greenColor = if (isDark) PriceTier.GREEN.colorDark() else PriceTier.GREEN.colorLight()
    val redColor   = if (isDark) PriceTier.RED.colorDark()   else PriceTier.RED.colorLight()

    val b0      = day.bestWindow.first
    val b1      = day.bestWindow.last
    val bestAvg = (day.hours[b0].price + day.hours[b1].price) / 2.0

    GlassCard(modifier = modifier.fillMaxWidth(), isDark = isDark) {
        Column(modifier = Modifier.padding(5.dp)) {
            RecoRow(
                emoji      = "💰",
                emojiBg    = greenColor.copy(alpha = .18f),
                glowColor  = greenColor.copy(alpha = .35f),
                label      = "Mejor momento para enchufar",
                labelColor = greenColor,
                value      = "${b0.toString().padStart(2, '0')}:00–${(b1 + 1).toString().padStart(2, '0')}:00 · ${"%.3f".format(bestAvg).replace('.', ',')} €/kWh",
            )
            HorizontalDivider(
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f),
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            RecoRow(
                emoji      = "💀",
                emojiBg    = redColor.copy(alpha = .18f),
                glowColor  = redColor.copy(alpha = .35f),
                label      = "Hora más cara — evítala",
                labelColor = redColor,
                value      = "${day.max.hour.toString().padStart(2, '0')}–${(day.max.hour + 1).toString().padStart(2, '0')}h · ${"%.3f".format(day.max.price).replace('.', ',')} €/kWh",
            )
        }
    }
}

@Composable
private fun RecoRow(
    emoji: String,
    emojiBg: Color,
    glowColor: Color,
    label: String,
    labelColor: Color,
    value: String,
) {
    Row(
        modifier          = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Emoji con sombra coloreada que simula el glow del diseño HTML
        Box(
            modifier = Modifier
                .shadow(
                    elevation   = 12.dp,
                    shape       = RoundedCornerShape(13.dp),
                    ambientColor = glowColor,
                    spotColor    = glowColor,
                )
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(emojiBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
            )
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
