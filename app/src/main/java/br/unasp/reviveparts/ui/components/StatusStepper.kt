package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.ui.theme.Outline
import br.unasp.reviveparts.ui.theme.YellowPrimary
import br.unasp.reviveparts.ui.theme.Black0

@Composable
fun StatusStepper(current: OrderStatus, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        OrderStatus.pipeline.forEachIndexed { i, s ->
            val isPast = i < current.ordinal
            val isCurrent = i == current.ordinal
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape)
                        .background(if (isPast || isCurrent) YellowPrimary else Outline),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPast) Icon(Icons.Default.Check, null, tint = Black0)
                    else Text("${i + 1}", color = if (isCurrent) Black0 else MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    s.label,
                    style = if (isCurrent) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                    color = if (isCurrent) YellowPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
