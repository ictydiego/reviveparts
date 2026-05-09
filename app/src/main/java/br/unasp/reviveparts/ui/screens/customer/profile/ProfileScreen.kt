package br.unasp.reviveparts.ui.screens.customer.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun ProfileScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: ProfileViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = ProfileViewModel.create(ctx.app) as T
    })
    val user by vm.user.collectAsState()
    val cards by vm.cardsList.collectAsState()
    var name by remember(user?.id) { mutableStateOf(user?.name ?: "") }
    var phone by remember(user?.id) { mutableStateOf(user?.phone ?: "") }
    var address by remember(user?.id) { mutableStateOf(user?.address ?: "") }

    LaunchedEffect(user) {
        user?.let { name = it.name; phone = it.phone; address = it.address }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        PrimaryTextField(name, { name = it }, "Nome")
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(phone, { phone = it }, "Telefone")
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(address, { address = it }, "Endereço")
        Spacer(Modifier.height(16.dp))
        YellowButton("Salvar", { vm.save(name, phone, address) }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Text("Cartões salvos", style = MaterialTheme.typography.titleLarge)
        cards.forEach { c ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${c.brand} •••• ${c.last4}")
                        Text(c.holderName, style = MaterialTheme.typography.bodyMedium)
                    }
                    TextButton(onClick = { vm.deleteCard(c) }) { Text("Excluir") }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = { vm.logout { nav.navigate(Routes.LOGIN) { popUpTo(0) } } }, modifier = Modifier.fillMaxWidth()) { Text("Sair") }
    }
}
