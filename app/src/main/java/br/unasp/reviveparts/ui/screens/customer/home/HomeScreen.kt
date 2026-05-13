package br.unasp.reviveparts.ui.screens.customer.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.R
import br.unasp.reviveparts.app
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.ui.components.CustomerDrawerContent
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel.create(ctx.app) as T
    })
    val products by vm.products.collectAsState(initial = emptyList())
    val user by vm.user.collectAsState()
    val hasOrderUpdate by vm.hasOrderUpdate.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var filterOpen by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ProductEntity?>(null) }
    val productSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val brands = remember(products) {
        products.map { it.carBrand.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
    val filtered = remember(products, search, selectedBrand) {
        products.filter { p ->
            val matchesSearch = search.isBlank() ||
                p.name.contains(search, ignoreCase = true) ||
                p.description.contains(search, ignoreCase = true) ||
                p.carBrand.contains(search, ignoreCase = true)
            val matchesBrand = selectedBrand == null || p.carBrand.equals(selectedBrand, ignoreCase = true)
            matchesSearch && matchesBrand
        }
    }
    val topSellers = filtered.take(6)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            CustomerDrawerContent(
                userName = user?.name ?: "",
                userEmail = user?.email ?: "",
                onClose = { scope.launch { drawerState.close() } },
                onOrders = { nav.navigate(Routes.CUSTOMER_ORDERS) },
                onFavorites = { nav.navigate(Routes.CUSTOMER_FAVORITES) },
                onProfile = { nav.navigate(Routes.CUSTOMER_PROFILE) },
                onPayments = { nav.navigate(Routes.CUSTOMER_PAYMENTS) },
                onLogout = {
                    scope.launch {
                        ctx.app.sessionRepo.logout()
                        nav.navigate(Routes.LOGIN) { popUpTo(0) }
                    }
                }
            )
        }
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            TopHeader(
                userName = user?.name?.substringBefore(" ") ?: "",
                hasNotification = hasOrderUpdate,
                onBell = {
                    vm.clearOrderUpdate()
                    nav.navigate(Routes.CUSTOMER_ORDERS)
                },
                onAvatar = { scope.launch { drawerState.open() } }
            )
            Spacer(Modifier.height(16.dp))
            SearchRow(
                value = search,
                onValueChange = { search = it },
                onFilter = { filterOpen = true },
                hasActiveFilter = selectedBrand != null
            )
            Spacer(Modifier.height(20.dp))
            SectionHeader("Mais vendidos")
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(topSellers, key = { it.id }) { p ->
                    RoundProductCard(p) { nav.navigate(Routes.partDetail(p.id)) }
                }
            }
            Spacer(Modifier.height(20.dp))
            SectionHeader("Visto recentemente")
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(filtered, key = { _, p -> p.id }) { _, p ->
                    TallProductCard(
                        p = p,
                        onArrow = { selected = p },
                        onClick = { nav.navigate(Routes.partDetail(p.id)) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }

        if (selected != null) {
            ModalBottomSheet(
                onDismissRequest = { selected = null },
                sheetState = productSheetState,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                ProductSheetContent(
                    p = selected!!,
                    onBack = { scope.launch { productSheetState.hide() }.invokeOnCompletion { selected = null } },
                    search = search,
                    onSearchChange = { search = it },
                    onCart = {
                        val id = selected!!.id
                        selected = null
                        nav.navigate(Routes.cart(id, "CATALOG"))
                    },
                    onImage = {
                        val id = selected!!.id
                        selected = null
                        nav.navigate(Routes.partDetail(id))
                    }
                )
            }
        }

        if (filterOpen) {
            ModalBottomSheet(
                onDismissRequest = { filterOpen = false },
                sheetState = filterSheetState,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                FilterSheetContent(
                    brands = brands,
                    selectedBrand = selectedBrand,
                    onSelect = {
                        selectedBrand = it
                        scope.launch { filterSheetState.hide() }.invokeOnCompletion { filterOpen = false }
                    }
                )
            }
        }
    }
}

@Composable
private fun TopHeader(userName: String, hasNotification: Boolean, onBell: () -> Unit, onAvatar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Bem-vindo!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(userName.ifBlank { "Cliente" }, style = MaterialTheme.typography.headlineMedium, color = YellowPrimary)
        }
        IconButton(onClick = onBell) {
            BadgedBox(
                badge = { if (hasNotification) Badge(containerColor = MaterialTheme.colorScheme.error) }
            ) {
                Icon(Icons.Default.Notifications, "notificações", tint = YellowPrimary)
            }
        }
        Spacer(Modifier.width(4.dp))
        Surface(
            onClick = onAvatar,
            shape = CircleShape,
            color = YellowPrimary,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    userName.firstOrNull()?.uppercase() ?: "?",
                    color = Black0,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun SearchRow(
    value: String,
    onValueChange: (String) -> Unit,
    onFilter: () -> Unit,
    hasActiveFilter: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Encontre a sua peça") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = YellowPrimary,
                cursorColor = YellowPrimary
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = onFilter,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (hasActiveFilter) Black0 else YellowPrimary,
                contentColor = if (hasActiveFilter) YellowPrimary else Black0
            ),
            modifier = Modifier.size(52.dp)
        ) {
            BadgedBox(
                badge = { if (hasActiveFilter) Badge(containerColor = MaterialTheme.colorScheme.error) }
            ) {
                Icon(Icons.Default.Tune, "filtro")
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSheetContent(
    brands: List<String>,
    selectedBrand: String?,
    onSelect: (String?) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
        Text("Filtros", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Marca do carro",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedBrand == null,
                onClick = { onSelect(null) },
                label = { Text("Todas") },
                leadingIcon = if (selectedBrand == null) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                } else null
            )
            brands.forEach { brand ->
                FilterChip(
                    selected = selectedBrand == brand,
                    onClick = { onSelect(brand) },
                    label = { Text(brand) },
                    leadingIcon = if (selectedBrand == brand) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
        if (brands.isEmpty()) {
            Text(
                "Nenhuma marca cadastrada ainda.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun RoundProductCard(p: ProductEntity, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(96.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(painterResource(productDrawable(p.name)), null, modifier = Modifier.size(56.dp), contentScale = ContentScale.Fit)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(p.name, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TallProductCard(p: ProductEntity, onArrow: () -> Unit, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(p.name.uppercase(), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    if (p.carBrand.isNotBlank()) {
                        Text(
                            p.carBrand,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        "R$ %.2f".format(p.priceCents / 100.0),
                        color = YellowPrimary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = YellowPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("5.0", style = MaterialTheme.typography.labelLarge)
                    }
                }
                FilledIconButton(
                    onClick = onArrow,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = YellowPrimary,
                        contentColor = Black0
                    ),
                    modifier = Modifier.size(40.dp)
                ) { Icon(Icons.Default.NorthEast, "abrir") }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(productDrawable(p.name)), null, modifier = Modifier.fillMaxSize().padding(12.dp), contentScale = ContentScale.Fit)
            }
        }
    }
}

@Composable
private fun ProductSheetContent(
    p: ProductEntity,
    onBack: () -> Unit,
    search: String,
    onSearchChange: (String) -> Unit,
    onCart: () -> Unit,
    onImage: () -> Unit
) {
    var liked by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "voltar", tint = YellowPrimary)
            }
            Text("Adquira também", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(8.dp))
        SearchRow(search, onSearchChange, { })
        Spacer(Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = Black0
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Marca: ${p.carBrand.ifBlank { "Universal" }}", style = MaterialTheme.typography.labelLarge, color = Color(0xFF555555))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.name, style = MaterialTheme.typography.headlineMedium, color = Black0, modifier = Modifier.weight(1f))
                    FilledIconButton(
                        onClick = onCart,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = YellowPrimary, contentColor = Black0
                        )
                    ) { Icon(Icons.Default.ShoppingCart, "comprar") }
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    onClick = onImage,
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painterResource(productDrawable(p.name)),
                            null,
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "R$ %.2f".format(p.priceCents / 100.0),
                        color = Black0,
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { liked = !liked }) {
                        Icon(
                            if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "favoritar",
                            tint = if (liked) YellowPrimary else Color(0xFF999999)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                SpecRowLight("Peso", "~700g")
                SpecRowLight("Altura", "~10cm")
                SpecRowLight("Comprimento", "~40cm")
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Avaliações", style = MaterialTheme.typography.titleLarge, color = Black0, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Star, null, tint = YellowPrimary)
                    Spacer(Modifier.width(4.dp))
                    Text("5.0", style = MaterialTheme.typography.titleLarge, color = Black0)
                }
            }
        }
    }
}

@Composable
private fun SpecRowLight(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = Color(0xFF666666))
        Text(value, style = MaterialTheme.typography.labelLarge, color = Black0)
    }
}

private fun productDrawable(name: String): Int = br.unasp.reviveparts.ui.screens.customer.productImage(name)
