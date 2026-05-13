package br.unasp.reviveparts.ui.screens.customer.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.R
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.ui.components.ModelViewer3D
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun AiSearchScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = AiViewModel.create(ctx.app) as T
    })
    val state by vm.state.collectAsState()
    val user by vm.user.collectAsState()

    Column(Modifier.fillMaxSize()) {
        when (val s = state) {
            is AiViewModel.UiState.Input -> InputView(
                state = s,
                userName = user?.name?.substringBefore(" ") ?: "",
                onTextChange = vm::setText,
                onImage = vm::setImage,
                onSend = vm::analyze
            )
            is AiViewModel.UiState.Analyzing -> AnalyzingView(s, onBack = vm::back)
            is AiViewModel.UiState.Result -> ResultView(
                s,
                onBack = vm::back,
                onAnother = vm::reset,
                onConfirm = vm::toModel3D
            )
            is AiViewModel.UiState.Model3D -> Model3DView(
                s,
                onBack = vm::back,
                onChangeQty = vm::changeQty,
                onAddToCart = { nav.navigate(Routes.cart(s.product.id, OrderSource.AI.name)) }
            )
            is AiViewModel.UiState.Error -> ErrorView(s.message, onRetry = vm::reset)
        }
    }
}

/* ========== Input ========== */

@Composable
private fun InputView(
    state: AiViewModel.UiState.Input,
    userName: String,
    onTextChange: (String) -> Unit,
    onImage: (Uri?) -> Unit,
    onSend: () -> Unit
) {
    var pickerOpen by remember { mutableStateOf(false) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { onImage(it) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row {
            Spacer(Modifier.weight(1f))
            IconButton({}) { Icon(Icons.Default.Notifications, null, tint = YellowPrimary) }
            Surface(shape = CircleShape, color = YellowPrimary, modifier = Modifier.size(40.dp), onClick = {}) {
                Box(contentAlignment = Alignment.Center) {
                    Text(userName.firstOrNull()?.uppercase() ?: "?", color = Black0)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Bom dia, ${userName.ifBlank { "Cliente" }}",
            style = MaterialTheme.typography.displayLarge,
            color = YellowPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Crie aqui sua peça personalizada!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(28.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = { pickerOpen = true },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.size(40.dp)
                    ) { Icon(Icons.Default.Add, "anexar") }
                    Spacer(Modifier.width(8.dp))
                    if (state.imageUri != null) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(YellowPrimary.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Image, null, tint = YellowPrimary) }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (state.imageUri == null) "Anexar foto (opcional)" else "Imagem anexada ✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedTextField(
                    value = state.text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                    placeholder = { Text("Descreva a peça...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = YellowPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = YellowPrimary
                    )
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    FilledIconButton(
                        onClick = onSend,
                        enabled = state.text.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = YellowPrimary, contentColor = Black0
                        ),
                        modifier = Modifier.size(48.dp)
                    ) { Icon(Icons.Default.ArrowUpward, "enviar") }
                }
            }
        }
    }

    if (pickerOpen) {
        AlertDialog(
            onDismissRequest = { pickerOpen = false },
            title = { Text("Adicionar foto") },
            text = { Text("Escolha uma opção:") },
            confirmButton = {
                TextButton(onClick = { pickerOpen = false; gallery.launch("image/*") }) { Text("Galeria") }
            },
            dismissButton = {
                TextButton(onClick = { pickerOpen = false; onImage(Uri.parse("mock://camera")) }) { Text("Câmera") }
            }
        )
    }
}

/* ========== Analyzing ========== */

@Composable
private fun AnalyzingView(state: AiViewModel.UiState.Analyzing, onBack: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "ai-bob")
    val s by infinite.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
            Text("Analisando a imagem", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(8.dp))
        ProgressBars(state.step.coerceAtMost(2))
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .scale(s),
            contentAlignment = Alignment.Center
        ) {
            FocusCorners()
            Image(
                painterResource(R.drawable.manivela_vidro),
                null,
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("Nossa IA está lendo sua peça", style = MaterialTheme.typography.titleLarge, color = YellowPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            "Identificando formato, dimensão, material estimado e modelo do veículo...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        StepRow(0, state.step, "Foto recebida")
        StepRow(1, state.step, "Classificando o tipo de peça")
        StepRow(2, state.step, "Gerando modelo 3D")
        StepRow(3, state.step, "Calculando orçamento")
    }
}

@Composable
private fun ProgressBars(filled: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { i ->
            Box(
                Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                    .background(if (i <= filled) YellowPrimary else MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
private fun BoxScope.FocusCorners() {
    val sz = 22.dp
    val c = YellowPrimary
    Box(Modifier.align(Alignment.TopStart).size(sz).border(3.dp, c, RoundedCornerShape(4.dp)))
    Box(Modifier.align(Alignment.TopEnd).size(sz).border(3.dp, c, RoundedCornerShape(4.dp)))
    Box(Modifier.align(Alignment.BottomStart).size(sz).border(3.dp, c, RoundedCornerShape(4.dp)))
    Box(Modifier.align(Alignment.BottomEnd).size(sz).border(3.dp, c, RoundedCornerShape(4.dp)))
}

@Composable
private fun StepRow(idx: Int, current: Int, label: String) {
    val done = current > idx
    val active = current == idx
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Box(
            Modifier.size(28.dp).clip(CircleShape)
                .background(if (done || active) YellowPrimary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            when {
                done -> Icon(Icons.Default.Check, null, tint = Black0, modifier = Modifier.size(18.dp))
                active -> Icon(Icons.Default.HourglassTop, null, tint = Black0, modifier = Modifier.size(16.dp))
                else -> Icon(Icons.Default.Circle, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(8.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = if (active || done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* ========== Result ========== */

@Composable
private fun ResultView(
    state: AiViewModel.UiState.Result,
    onBack: () -> Unit,
    onAnother: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
            Text("Peça identificada", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(8.dp))
        ProgressBars(2)
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier.size(80.dp).clip(CircleShape).background(YellowPrimary).align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Check, null, tint = Black0, modifier = Modifier.size(48.dp)) }
        Spacer(Modifier.height(12.dp))
        Text("Peça encontrada!", style = MaterialTheme.typography.displayLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(
            "Nossa IA identificou com ${(state.confidence * 100).toInt()}% de confiança",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp)) {
                Box(
                    Modifier.size(96.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painterResource(R.drawable.manivela_vidro), null, modifier = Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.product.name, style = MaterialTheme.typography.titleLarge)
                    Text("Acionamento manual rotativo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TagChip("VW Gol")
                        TagChip("Plástico")
                        TagChip("~140g")
                    }
                    Spacer(Modifier.height(6.dp))
                    TagChip("13-15cm")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Não é essa peça?", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onAnother, modifier = Modifier.weight(1f)) { Text("Corrigir nome") }
                    OutlinedButton(onClick = onAnother, modifier = Modifier.weight(1f)) { Text("Outra foto") }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) { Text("Gerar modelo 3D", style = MaterialTheme.typography.titleLarge) }
    }
}

@Composable
private fun TagChip(label: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

/* ========== Model 3D ========== */

@Composable
private fun Model3DView(
    state: AiViewModel.UiState.Model3D,
    onBack: () -> Unit,
    onChangeQty: (Int) -> Unit,
    onAddToCart: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
            Text("Modelo em 3D", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onAddToCart) { Icon(Icons.Default.ShoppingCart, null, tint = YellowPrimary) }
        }
        ModelViewer3D(
            assetPath = "models/manivela_vidro.stl",
            modifier = Modifier.fillMaxWidth().height(280.dp)
        )
        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(state.product.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Peça utilizada para acionamento manual de mecanismos de janela." +
                            if (expanded) " Compatível com VW Fusca e Kombi clássicos. Material ABS reforçado, alta resistência. Ideal para reposição em veículos fora de linha."
                            else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Ver menos" else "Ver mais", color = YellowPrimary)
                }
                Spacer(Modifier.height(8.dp))
                Text("Overview", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OverviewItem(Icons.Default.Height, "Altura", "~6 a 8cm")
                    OverviewItem(Icons.Default.Scale, "Peso", "~140 a 150g")
                    OverviewItem(Icons.Default.SwapHoriz, "Comprimento", "~13 a 15cm")
                }
                Spacer(Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Preço:", modifier = Modifier.weight(1f))
                            Text(
                                "R$ %.2f".format(state.product.priceCents / 100.0),
                                style = MaterialTheme.typography.headlineMedium,
                                color = YellowPrimary
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledIconButton(
                                onClick = { onChangeQty(-1) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) { Icon(Icons.Default.Remove, "menos") }
                            Text("${state.qty}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 12.dp))
                            FilledIconButton(
                                onClick = { onChangeQty(1) },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) { Icon(Icons.Default.Add, "mais") }
                            Spacer(Modifier.width(12.dp))
                            Button(
                                onClick = onAddToCart,
                                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Adicionar")
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OverviewItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = YellowPrimary) }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Erro", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
        Text(message, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0)
        ) { Text("Tentar novamente") }
    }
}
