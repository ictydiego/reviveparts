package br.unasp.reviveparts.ui.screens.owner.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.unasp.reviveparts.R
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.theme.YellowPrimary
import kotlinx.coroutines.launch

@Composable
fun OwnerProfileScreen(nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = YellowPrimary, modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painterResource(R.drawable.logo),
                        null,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("ReviveParts", style = MaterialTheme.typography.titleLarge)
                Text("dono@reviveparts.com", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = YellowPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        "ADMIN",
                        style = MaterialTheme.typography.labelSmall,
                        color = YellowPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionLabel("Conta")
        SettingRow(Icons.Default.Business, "Dados da empresa", "ReviveParts LTDA")
        SettingRow(Icons.Default.LocationOn, "Endereço da fábrica", "São Paulo / SP")
        SettingRow(Icons.Default.Phone, "Suporte", "(11) 90000-0000")

        Spacer(Modifier.height(16.dp))
        SectionLabel("Operação")
        SettingRow(Icons.Default.Inventory2, "Gerenciar produtos", onClick = { nav.navigate(Routes.OWNER_PRODUCTS) })
        SettingRow(Icons.Default.Receipt, "Pedidos", onClick = { nav.navigate(Routes.OWNER_DASHBOARD) })
        SettingRow(Icons.Default.Print, "Fila de impressão", subtitle = "Em breve")

        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = {
                scope.launch {
                    ctx.app.sessionRepo.logout()
                    nav.navigate(Routes.LOGIN) { popUpTo(0) }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(8.dp))
            Text("Sair da conta", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = YellowPrimary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String? = null, onClick: (() -> Unit)? = null) {
    Card(
        onClick = onClick ?: {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(YellowPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = YellowPrimary) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onClick != null) Icon(Icons.Default.ChevronRight, null, tint = YellowPrimary)
        }
    }
}
