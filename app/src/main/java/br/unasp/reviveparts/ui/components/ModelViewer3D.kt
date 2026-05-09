package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import br.unasp.reviveparts.ui.theme.Surface1

@Composable
fun ModelViewer3D(assetPath: String, modifier: Modifier = Modifier) {
    val path = when {
        assetPath.endsWith(".stl", true) || assetPath.endsWith(".3mf", true) -> assetPath
        else -> "models/manivela_vidro.stl"
    }
    AndroidView(
        modifier = modifier.background(Surface1),
        factory = { ctx -> StlGlView(ctx, path) }
    )
}
