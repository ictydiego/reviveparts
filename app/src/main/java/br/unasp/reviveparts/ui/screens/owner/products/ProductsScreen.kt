package br.unasp.reviveparts.ui.screens.owner.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun ProductsScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: ProductsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = ProductsViewModel.create(ctx.app) as T
    })
    val items by vm.items.collectAsState(initial = emptyList())

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { nav.navigate(Routes.PRODUCT_NEW) }, containerColor = MaterialTheme.colorScheme.primary) {
            Icon(Icons.Default.Add, "novo")
        }
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            Text("Produtos", style = MaterialTheme.typography.headlineMedium)
            LazyColumn {
                items(items, key = { it.id }) { p ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.padding(16.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(p.name, style = MaterialTheme.typography.titleLarge)
                                Text("R$ %.2f  •  estoque ${p.stockQty}".format(p.priceCents / 100.0))
                            }
                            TextButton(onClick = { nav.navigate(Routes.productEdit(p.id)) }) { Text("Editar") }
                            TextButton(onClick = { vm.delete(p) }) { Text("Excluir") }
                        }
                    }
                }
            }
        }
    }
}
