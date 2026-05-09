package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

data class DrawerEntry(val label: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun CustomerDrawerContent(
    userName: String,
    userEmail: String,
    onClose: () -> Unit,
    onOrders: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit,
    onPayments: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight().width(300.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape).background(YellowPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userName.firstOrNull()?.uppercase() ?: "?",
                        color = Black0,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(userName.ifBlank { "Cliente" }, style = MaterialTheme.typography.titleLarge)
                    Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(24.dp))

            DrawerSectionLabel("Conta principal")
            DrawerRow("Meus Pedidos", Icons.Default.ShoppingBag) { onClose(); onOrders() }
            DrawerRow("Favoritos", Icons.Default.FavoriteBorder) { onClose(); onFavorites() }

            Spacer(Modifier.height(16.dp))
            DrawerSectionLabel("Conta")
            DrawerRow("Dados Pessoais", Icons.Default.Person) { onClose(); onProfile() }
            DrawerRow("Pagamentos", Icons.Default.CreditCard) { onClose(); onPayments() }

            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            DrawerRow("Sair", Icons.Default.Logout) { onClose(); onLogout() }
        }
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = YellowPrimary,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun DrawerRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Icon(icon, null, tint = YellowPrimary)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onClick) { Text("Abrir") }
    }
}
