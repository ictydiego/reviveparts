package br.unasp.reviveparts.ui.screens.customer.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.data.db.entities.CardEntity
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun PaymentsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: PaymentsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = PaymentsViewModel.create(ctx.app) as T
    })
    val cards by vm.items.collectAsState()
    val error by vm.error.collectAsState()
    var showForm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
            Text("Pagamentos", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))

        if (cards.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CreditCard, null, tint = YellowPrimary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Nenhum cartão salvo", style = MaterialTheme.typography.titleLarge)
                    Text("Adicione um cartão para usar no pagamento.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            cards.forEach { c ->
                CreditCardVisual(c)
                Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp), horizontalArrangement = Arrangement.End) {
                    if (!c.isDefault) {
                        TextButton(onClick = { vm.setDefault(c) }) { Text("Definir como padrão", color = YellowPrimary) }
                    } else {
                        AssistChip(onClick = {}, label = { Text("Padrão") }, leadingIcon = { Icon(Icons.Default.Check, null, tint = YellowPrimary) })
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = { vm.delete(c) }) {
                        Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(4.dp))
                        Text("Excluir", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = { showForm = !showForm },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, null, tint = YellowPrimary)
            Spacer(Modifier.width(8.dp))
            Text(if (showForm) "Cancelar" else "Adicionar cartão", color = YellowPrimary)
        }

        if (showForm) {
            Spacer(Modifier.height(16.dp))
            AddCardForm(
                error = error,
                onDismissError = vm::clearError,
                onSave = { num, holder, exp, cvv ->
                    if (vm.add(num, holder, exp, cvv, makeDefault = cards.isEmpty())) {
                        showForm = false
                    }
                }
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AddCardForm(
    error: String?,
    onDismissError: () -> Unit,
    onSave: (number: String, holder: String, expiry: String, cvv: String) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var makeDefault by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Live preview
            CreditCardPreview(
                number = number,
                holder = holder.ifBlank { "NOME COMPLETO" },
                expiry = expiry.ifBlank { "MM/AA" }
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = number,
                onValueChange = { v -> number = v.filter { it.isDigit() }.take(16) },
                label = { Text("Número do cartão") },
                singleLine = true,
                visualTransformation = CardNumberTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = yellowFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = holder,
                onValueChange = { holder = it.uppercase() },
                label = { Text("Nome impresso no cartão") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = yellowFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedTextField(
                    value = expiry,
                    onValueChange = { v -> expiry = formatExpiry(v) },
                    label = { Text("Validade (MM/AA)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = yellowFieldColors()
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { v -> cvv = v.filter { it.isDigit() }.take(4) },
                    label = { Text("CVV") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.weight(1f),
                    colors = yellowFieldColors()
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = makeDefault, onCheckedChange = { makeDefault = it })
                Text("Definir como padrão")
            }
            if (error != null) {
                Spacer(Modifier.height(4.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    onDismissError()
                    onSave(number, holder, expiry, cvv)
                },
                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Salvar cartão") }
        }
    }
}

@Composable
private fun yellowFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = YellowPrimary,
    focusedLabelColor = YellowPrimary,
    cursorColor = YellowPrimary
)

private fun formatExpiry(input: String): String {
    val d = input.filter { it.isDigit() }.take(4)
    return when {
        d.length <= 2 -> d
        else -> d.substring(0, 2) + "/" + d.substring(2)
    }
}

private val CardNumberTransformation = VisualTransformation { text ->
    val digits = text.text.filter { it.isDigit() }.take(16)
    val out = buildString {
        for (i in digits.indices) {
            if (i > 0 && i % 4 == 0) append(' ')
            append(digits[i])
        }
    }
    val transformedLen = out.length
    val mapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val o = offset.coerceIn(0, digits.length)
            if (o == 0) return 0
            return (o + (o - 1) / 4).coerceAtMost(transformedLen)
        }
        override fun transformedToOriginal(offset: Int): Int {
            val t = offset.coerceIn(0, transformedLen)
            return (t - t / 5).coerceIn(0, digits.length)
        }
    }
    TransformedText(AnnotatedString(out), mapping)
}

@Composable
fun CreditCardVisual(card: CardEntity) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF2A2A2A), Color(0xFF111111))))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(card.brand, color = YellowPrimary, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    BrandLogos()
                }
                Spacer(Modifier.height(28.dp))
                Text("••••  ••••  ••••  ${card.last4}", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("TITULAR", color = Color(0xFF999999), style = MaterialTheme.typography.labelSmall)
                        Text(card.holderName.uppercase(), color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    }
                    Column {
                        Text("VALIDADE", color = Color(0xFF999999), style = MaterialTheme.typography.labelSmall)
                        Text(card.expiry, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditCardPreview(number: String, holder: String, expiry: String) {
    val padded = number.padEnd(16, '•')
    val formatted = buildString {
        padded.take(16).forEachIndexed { i, c ->
            if (i > 0 && i % 4 == 0) append("  ")
            append(c)
        }
    }
    val brand = when (number.firstOrNull()) {
        '4' -> "Visa"; '5' -> "Mastercard"; '3' -> "Amex"; '6' -> "Elo"; else -> "Cartão"
    }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF2A2A2A), Color(0xFF111111))))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(brand, color = YellowPrimary, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    BrandLogos()
                }
                Spacer(Modifier.height(20.dp))
                Text(formatted, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("TITULAR", color = Color(0xFF999999), style = MaterialTheme.typography.labelSmall)
                        Text(holder, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    }
                    Column {
                        Text("VALIDADE", color = Color(0xFF999999), style = MaterialTheme.typography.labelSmall)
                        Text(expiry, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandLogos() {
    Row {
        Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFEB001B)))
        Spacer(Modifier.width(-12.dp))
        Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF79E1B).copy(alpha = 0.85f)))
    }
}
