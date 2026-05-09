package br.unasp.reviveparts.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.R
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun LoginScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = AuthViewModel.create(ctx.app) as T
    })
    val state by vm.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.loggedInRole) {
        when (state.loggedInRole) {
            Role.OWNER -> nav.navigate(Routes.OWNER_DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
            Role.CUSTOMER -> nav.navigate(Routes.CUSTOMER_HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
            null -> Unit
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.logo_login_opacidade),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alpha = 0.2f,
            modifier = Modifier.fillMaxSize().padding(0.dp)
        )
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(R.drawable.logo), null, Modifier.size(140.dp))
            Spacer(Modifier.height(24.dp))
            Text("Entrar", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            PrimaryTextField(email, { email = it }, "E-mail", keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(8.dp))
            PrimaryTextField(password, { password = it }, "Senha", isPassword = true)
            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            YellowButton("Entrar", { vm.login(email, password) }, Modifier.fillMaxWidth(), enabled = !state.loading)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { nav.navigate(Routes.REGISTER) }) { Text("Criar conta") }
        }
    }
}
