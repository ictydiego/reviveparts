package br.unasp.reviveparts.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun RegisterScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = AuthViewModel.create(ctx.app) as T
    })
    val state by vm.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(state.loggedInRole) {
        when (state.loggedInRole) {
            Role.OWNER -> nav.navigate(Routes.OWNER_DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
            Role.CUSTOMER -> nav.navigate(Routes.CUSTOMER_HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
            null -> Unit
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("Criar conta", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        PrimaryTextField(name, { name = it }, "Nome completo")
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(email, { email = it }, "E-mail", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(password, { password = it }, "Senha", isPassword = true)
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(phone, { phone = it }, "Telefone", keyboardType = KeyboardType.Phone)
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(cpf, { cpf = it }, "CPF", keyboardType = KeyboardType.Number)
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(address, { address = it }, "Endereço")
        if (state.error != null) {
            Spacer(Modifier.height(8.dp)); Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        YellowButton("Cadastrar", { vm.register(name, email, password, phone, cpf, address) }, Modifier.fillMaxWidth(), enabled = !state.loading)
    }
}
