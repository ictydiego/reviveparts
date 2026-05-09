package br.unasp.reviveparts.ui.screens.customer.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.ui.components.LoadingDots
import br.unasp.reviveparts.ui.components.ModelViewer3D
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun AiSearchScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = AiViewModel.create(ctx.app) as T
    })
    val state by vm.state.collectAsState()
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        when (val s = state) {
            AiViewModel.UiState.Idle -> {
                Text("Descreva ou fotografe a peça", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                PrimaryTextField(text, { text = it }, "Ex: manivela de janela")
                Spacer(Modifier.height(12.dp))
                Row {
                    OutlinedButton(onClick = { pickImage.launch("image/*") }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text("Galeria")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { /* camera capture deferred */ }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Câmera")
                    }
                }
                if (imageUri != null) {
                    Spacer(Modifier.height(8.dp)); Text("Imagem selecionada ✓", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(24.dp))
                YellowButton("Identificar peça com IA", { vm.recognize(text, imageUri?.toString()) }, Modifier.fillMaxWidth())
            }
            AiViewModel.UiState.Recognizing -> {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Reconhecendo peça...", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(24.dp))
                    LoadingDots()
                }
            }
            is AiViewModel.UiState.Result -> {
                Text("É essa peça?", style = MaterialTheme.typography.headlineMedium)
                Text("${s.recognition.label} (${(s.recognition.confidence * 100).toInt()}% confiança)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                ModelViewer3D(s.product.model3dAsset, Modifier.fillMaxWidth().height(280.dp))
                Spacer(Modifier.height(16.dp))
                Text(s.product.name, style = MaterialTheme.typography.titleLarge)
                Text("R$ %.2f".format(s.product.priceCents / 100.0), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                YellowButton("Sim, gerar pedido", { nav.navigate(Routes.cart(s.product.id, OrderSource.AI.name)) }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { vm.reset() }) { Text("Não, tentar novamente") }
            }
            is AiViewModel.UiState.Error -> {
                Text("Erro: ${s.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                YellowButton("Tentar novamente", { vm.reset() }, Modifier.fillMaxWidth())
            }
        }
    }
}
