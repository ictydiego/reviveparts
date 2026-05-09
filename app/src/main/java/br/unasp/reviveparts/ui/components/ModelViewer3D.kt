package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.unasp.reviveparts.ui.theme.OnSurfaceMute
import br.unasp.reviveparts.ui.theme.Surface1

/**
 * Placeholder 3D viewer.
 *
 * The real SceneView 2.2.x integration was deferred — its `ModelNode`/`SceneView`
 * API surface didn't match the snippet in the original plan, and getting it
 * compiling reliably needs a follow-up wiring pass with the actual lib.
 *
 * For Phase 1 this just renders a dark surface with a "3D viewer (em breve)"
 * label so screens that depend on this composable can layout & navigate.
 */
@Composable
fun ModelViewer3D(assetPath: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Surface1),
        contentAlignment = Alignment.Center
    ) {
        Text("Visualizador 3D (em breve)", color = OnSurfaceMute)
    }
}
