package br.unasp.reviveparts.ui.screens.customer.payment

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
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun PaymentScreen(nav: NavController, productId: Long, source: String) {
    val ctx = LocalContext.current
    val vm: PaymentViewModel = viewModel(key = "pay-$productId-$source", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = PaymentViewModel.create(ctx.app, productId, OrderSource.valueOf(source)) as T
    })
    val state by vm.state.collectAsState()
    val product = state.product ?: return

    var selectedCardIdx by remember { mutableIntStateOf(0) }
    var pixSelected by remember { mutableStateOf(false) }
    var cardExpanded by remember { mutableStateOf(false) }
    var showAddCardForm by remember { mutableStateOf(false) }

    LaunchedEffect(state.createdOrderId) {
        val id = state.createdOrderId
        if (id != null && state.pixCopyPaste == null) {
            nav.navigate(Routes.orderDetail(id)) { popUpTo(Routes.CUSTOMER_HOME) }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = YellowPrimary) }
            Text("Forma de pagamento", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(20.dp))

        Text("Cartão de crédito", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (state.cards.isEmpty()) {
            EmptyCardCard()
        } else {
            val sel = state.cards.getOrNull(selectedCardIdx) ?: state.cards.first()
            CreditCardVisual(
                card = sel,
                expanded = cardExpanded,
                onToggle = { cardExpanded = !cardExpanded },
                isSelected = !pixSelected,
                onSelect = { pixSelected = false }
            )
            if (cardExpanded) {
                Spacer(Modifier.height(8.dp))
                state.cards.forEachIndexed { idx, c ->
                    if (idx != selectedCardIdx) {
                        OutlinedButton(
                            onClick = { selectedCardIdx = idx; cardExpanded = false; pixSelected = false },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("${c.brand} •••• ${c.last4}") }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            } else {
                Text(
                    "Clique na seta para mostrar outros cartões",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Pix", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        PixOptionRow(selected = pixSelected) { pixSelected = true }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = { showAddCardForm = !showAddCardForm },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(if (showAddCardForm) Icons.Default.Close else Icons.Default.Add, null, tint = YellowPrimary)
            Spacer(Modifier.width(8.dp))
            Text(if (showAddCardForm) "Cancelar" else "Adicionar cartão", color = YellowPrimary)
        }

        if (showAddCardForm) {
            Spacer(Modifier.height(16.dp))
            AddCardForm(
                onSave = { number, holder, expiry ->
                    showAddCardForm = false
                    vm.payCard(number, holder, expiry, detectBrand(number), save = true)
                }
            )
        }

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (pixSelected) vm.preparePix()
                else state.cards.getOrNull(selectedCardIdx)?.let { c ->
                    vm.payCard("4111111111111111", c.holderName, c.expiry, c.brand, save = false)
                }
            },
            enabled = !state.processing && (pixSelected || state.cards.isNotEmpty()),
            colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Finalizar o pagamento", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, null)
            Spacer(Modifier.width(12.dp))
            Text("R$ %.2f".format(product.priceCents / 100.0), style = MaterialTheme.typography.titleLarge)
        }

        if (state.pixCopyPaste != null) {
            Spacer(Modifier.height(12.dp))
            Text("PIX gerado:", style = MaterialTheme.typography.labelLarge)
            Text(state.pixCopyPaste!!, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { state.createdOrderId?.let { nav.navigate(Routes.orderDetail(it)) { popUpTo(Routes.CUSTOMER_HOME) } } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Já paguei") }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CreditCardVisual(card: CardEntity, expanded: Boolean, onToggle: () -> Unit, isSelected: Boolean, onSelect: () -> Unit) {
    val border = if (isSelected) 3.dp else 0.dp
    Card(
        onClick = onSelect,
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
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFEB001B)))
                    Spacer(Modifier.width(-12.dp))
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF79E1B).copy(alpha = 0.85f)))
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
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            null,
                            tint = YellowPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCardCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CreditCard, null, tint = YellowPrimary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text("Nenhum cartão salvo", style = MaterialTheme.typography.titleLarge)
            Text(
                "Toque em + Adicionar cartão",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PixOptionRow(selected: Boolean, onSelect: () -> Unit) {
    val borderColor = if (selected) YellowPrimary else Color.Transparent
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 0.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(YellowPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text("⬥", color = YellowPrimary) }
            Spacer(Modifier.width(12.dp))
            Text("PIX", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (selected) Icon(Icons.Default.CheckCircle, null, tint = YellowPrimary)
        }
    }
}

@Composable
private fun AddCardForm(onSave: (number: String, holder: String, expiry: String) -> Unit) {
    var number by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
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
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onSave(number, holder, expiry) },
                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) { Text("Salvar e pagar") }
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
private fun CreditCardPreview(number: String, holder: String, expiry: String) {
    val padded = number.padEnd(16, '•')
    val formatted = buildString {
        padded.take(16).forEachIndexed { i, c ->
            if (i > 0 && i % 4 == 0) append("  ")
            append(c)
        }
    }
    val brand = detectBrand(number)
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
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFEB001B)))
                    Spacer(Modifier.width(-12.dp))
                    Box(Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF79E1B).copy(alpha = 0.85f)))
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

private fun detectBrand(num: String): String {
    val n = num.filter { it.isDigit() }
    return when {
        n.startsWith("4") -> "Visa"
        n.startsWith("5") -> "Mastercard"
        n.startsWith("3") -> "Amex"
        n.startsWith("6") -> "Elo"
        else -> "Cartão"
    }
}
