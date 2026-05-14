package br.unasp.reviveparts.ui.components

import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.unasp.reviveparts.R
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun LivePrinterDialog(orderId: Long, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(Modifier.fillMaxSize().background(Black0)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val uri = "android.resource://${ctx.packageName}/${R.raw.imprimindo}"
                        setVideoURI(android.net.Uri.parse(uri))
                        setOnPreparedListener { mp: MediaPlayer ->
                            mp.isLooping = true
                            mp.setVolume(0f, 0f)
                            start()
                        }
                    }
                }
            )

            // Top overlay: LIVE badge + viewers + close
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveBadge()
                Spacer(Modifier.width(8.dp))
                ViewerBadge(count = 1)
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) { Icon(Icons.Default.Close, "fechar", tint = Color.White) }
            }

            // Bottom overlay: title + order
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(16.dp)
            ) {
                Text("Impressão 3D — ao vivo", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text("Pedido #BR-${200000 + orderId}", color = YellowPrimary, style = MaterialTheme.typography.labelLarge)
                Text(
                    "Sua peça está sendo impressa agora na nossa fábrica.",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LiveBadge() {
    val infinite = rememberInfiniteTransition(label = "live-pulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFFE53935), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = alpha))
        )
        Spacer(Modifier.width(6.dp))
        Text("AO VIVO", color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ViewerBadge(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text("$count assistindo", color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}
