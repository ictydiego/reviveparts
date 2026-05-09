package br.unasp.reviveparts.ui.screens.owner.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.nav.Routes
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun OwnerProfileScreen(nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Perfil — Dono", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("ReviveParts • Painel administrativo")
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = {
            scope.launch { ctx.app.sessionRepo.logout(); nav.navigate(Routes.LOGIN) { popUpTo(0) } }
        }, modifier = Modifier.fillMaxWidth()) { Text("Sair") }
    }
}
