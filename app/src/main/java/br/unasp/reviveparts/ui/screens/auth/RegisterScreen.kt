package br.unasp.reviveparts.ui.screens.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.theme.YellowPrimary
import br.unasp.reviveparts.ui.utils.CpfVisualTransformation
import br.unasp.reviveparts.ui.utils.PhoneVisualTransformation
import br.unasp.reviveparts.ui.theme.Black0
import java.util.*

@Composable
fun RegisterScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = AuthViewModel.create(ctx.app) as T
    })
    val state by vm.state.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fetchLocation(ctx) { address = it }
        }
    }

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

        PrimaryTextField(
            value = phone,
            onChange = { if (it.length <= 11) phone = it.filter { c -> c.isDigit() } },
            label = "Telefone",
            keyboardType = KeyboardType.Phone,
            visualTransformation = PhoneVisualTransformation()
        )
        Spacer(Modifier.height(8.dp))

        PrimaryTextField(
            value = cpf,
            onChange = { if (it.length <= 11) cpf = it.filter { c -> c.isDigit() } },
            label = "CPF",
            keyboardType = KeyboardType.Number,
            visualTransformation = CpfVisualTransformation()
        )
        Spacer(Modifier.height(8.dp))

        PrimaryTextField(
            value = address,
            onChange = { address = it },
            label = "Endereço",
            trailingIcon = {
                IconButton(onClick = {
                    val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                    val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
                        fetchLocation(ctx) { address = it }
                    } else {
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
                }) {
                    Icon(Icons.Default.MyLocation, "Pegar localização", tint = YellowPrimary)
                }
            }
        )

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        YellowButton(
            "Cadastrar",
            { vm.register(name, email, password, phone, cpf, address) },
            Modifier.fillMaxWidth(),
            enabled = !state.loading
        )

        Spacer(Modifier.height(16.dp))
        TextButton(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Já tenho uma conta. Entrar")
        }
    }
}

private fun fetchLocation(context: Context, onAddressFound: (String) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    try {
        val location = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)

        location?.let {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val fullAddress = "${addr.thoroughfare ?: ""}, ${addr.subThoroughfare ?: ""}, ${addr.subLocality ?: ""}, ${addr.locality ?: ""} - ${addr.adminArea ?: ""}"
                onAddressFound(fullAddress.trim().removePrefix(",").trim())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
