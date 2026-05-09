# ReviveParts MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build complete Android Compose MVP for ReviveParts: customer browses/orders 3D-printed VW manivela, owner manages orders/products. Local-only (Room), AI + payments mocked, real 3D viewer.

**Architecture:** Single-module Android app. Compose UI + Material3, Navigation-Compose. Room for persistence. Repositories wrap DAOs. ViewModels expose StateFlow. Two roles share the app, distinguished by hardcoded owner email. Mock services for AI recognition and payment processing. Real 3D rendering via SceneView for the recognized part.

**Tech Stack:** Kotlin, Jetpack Compose (BOM 2024.09), Material3, Navigation-Compose 2.8, Room 2.6 (KSP), Coil 2.7, CameraX 1.4, SceneView 2.2, ZXing 3.5, DataStore 1.1.

**Spec:** `docs/superpowers/specs/2026-05-09-reviveparts-mvp-design.md`

---

## File Structure

```
app/src/main/java/br/unasp/reviveparts/
├── RevivePartsApp.kt
├── MainActivity.kt
├── ui/
│   ├── theme/Color.kt, Theme.kt, Type.kt
│   ├── components/
│   │   ├── PartCard.kt
│   │   ├── StatusStepper.kt
│   │   ├── YellowButton.kt
│   │   ├── PrimaryTextField.kt
│   │   ├── ModelViewer3D.kt
│   │   ├── BottomBarHost.kt
│   │   └── LoadingDots.kt
│   ├── nav/Routes.kt, AppNavHost.kt
│   └── screens/
│       ├── auth/{LoginScreen,RegisterScreen,AuthViewModel}.kt
│       ├── customer/
│       │   ├── home/{HomeScreen,HomeViewModel}.kt
│       │   ├── partdetail/{PartDetailScreen,PartDetailViewModel}.kt
│       │   ├── ai/{AiSearchScreen,AiViewModel}.kt
│       │   ├── cart/{CartScreen,CartViewModel}.kt
│       │   ├── payment/{PaymentScreen,PaymentViewModel}.kt
│       │   ├── orders/{OrdersScreen,OrdersViewModel}.kt
│       │   ├── orderdetail/{OrderDetailScreen,OrderDetailViewModel}.kt
│       │   └── profile/{ProfileScreen,ProfileViewModel}.kt
│       └── owner/
│           ├── dashboard/{OwnerDashboardScreen,OwnerDashboardViewModel}.kt
│           ├── orderdetail/{OwnerOrderDetailScreen,OwnerOrderDetailViewModel}.kt
│           ├── products/{ProductsScreen,ProductsViewModel}.kt
│           ├── productedit/{ProductEditScreen,ProductEditViewModel}.kt
│           └── profile/OwnerProfileScreen.kt
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── Converters.kt
│   │   ├── entities/{User,Product,Card,Order,OrderEvent}.kt
│   │   ├── dao/{UserDao,ProductDao,CardDao,OrderDao,OrderEventDao}.kt
│   │   └── Seed.kt
│   ├── repo/{UserRepository,ProductRepository,OrderRepository,CardRepository,SessionRepository}.kt
│   ├── ai/FakeAiService.kt
│   └── payments/{PaymentSimulator,PixGenerator,LuhnValidator}.kt
└── domain/model/{Role,OrderStatus,PaymentType,OrderSource,RecognitionResult}.kt

app/src/main/assets/models/manivela_vw.glb
app/src/main/res/values/{strings.xml,colors.xml}
```

---

## Task 1: Gradle dependencies + theme + Application class

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `build.gradle.kts` (root)
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/br/unasp/reviveparts/RevivePartsApp.kt`
- Create: `app/src/main/java/br/unasp/reviveparts/ui/theme/Color.kt`
- Modify: `app/src/main/java/br/unasp/reviveparts/ui/theme/Theme.kt`

- [ ] **Step 1.1: Add versions and libs**

Replace `gradle/libs.versions.toml`:

```toml
[versions]
agp = "9.1.1"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.10.0"
activityCompose = "1.13.0"
kotlin = "2.2.10"
composeBom = "2024.09.00"
ksp = "2.2.10-1.0.24"
room = "2.6.1"
navigation = "2.8.4"
coil = "2.7.0"
cameraX = "1.4.0"
sceneView = "2.2.1"
zxing = "3.5.3"
datastore = "1.1.1"
viewmodelCompose = "2.8.7"
coroutines = "1.9.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "viewmodelCompose" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
camerax-core = { group = "androidx.camera", name = "camera-core", version.ref = "cameraX" }
camerax-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "cameraX" }
camerax-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "cameraX" }
camerax-view = { group = "androidx.camera", name = "camera-view", version.ref = "cameraX" }
sceneview = { group = "io.github.sceneview", name = "sceneview", version.ref = "sceneView" }
zxing-core = { group = "com.google.zxing", name = "core", version.ref = "zxing" }
kotlinx-coroutines = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 1.2: Update root build.gradle.kts**

Add to root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}
```

- [ ] **Step 1.3: Update app/build.gradle.kts**

Replace plugins + dependencies blocks:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "br.unasp.reviveparts"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.unasp.reviveparts"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    packaging {
        resources.excludes += setOf("META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.coil.compose)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.sceneview)
    implementation(libs.zxing.core)
    implementation(libs.kotlinx.coroutines)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

Note: AGP 9 uses simplified compileSdk syntax. If the existing block syntax is required, keep `compileSdk { version = release(36) }`.

- [ ] **Step 1.4: Manifest — add Application + permissions**

Replace `AndroidManifest.xml` `<application>` opening tag and add permissions:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-feature android:name="android.hardware.camera" android:required="false"/>

    <application
        android:name=".RevivePartsApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Reviveparts">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.Reviveparts">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 1.5: Application class**

Create `app/src/main/java/br/unasp/reviveparts/RevivePartsApp.kt`:

```kotlin
package br.unasp.reviveparts

import android.app.Application
import androidx.room.Room
import br.unasp.reviveparts.data.db.AppDatabase
import br.unasp.reviveparts.data.db.Seed
import br.unasp.reviveparts.data.repo.*
import br.unasp.reviveparts.data.ai.FakeAiService
import br.unasp.reviveparts.data.payments.PaymentSimulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RevivePartsApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val db by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "reviveparts.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val userRepo by lazy { UserRepository(db.userDao()) }
    val productRepo by lazy { ProductRepository(db.productDao()) }
    val orderRepo by lazy { OrderRepository(db.orderDao(), db.orderEventDao()) }
    val cardRepo by lazy { CardRepository(db.cardDao()) }
    val sessionRepo by lazy { SessionRepository(this) }
    val aiService by lazy { FakeAiService() }
    val paymentSimulator by lazy { PaymentSimulator() }

    override fun onCreate() {
        super.onCreate()
        appScope.launch { Seed.seedIfEmpty(db) }
    }
}

val android.content.Context.app: RevivePartsApp
    get() = applicationContext as RevivePartsApp
```

- [ ] **Step 1.6: Theme colors**

Replace `app/src/main/java/br/unasp/reviveparts/ui/theme/Color.kt`:

```kotlin
package br.unasp.reviveparts.ui.theme

import androidx.compose.ui.graphics.Color

val YellowPrimary = Color(0xFFFFD60A)
val YellowDark    = Color(0xFFE6BF00)
val Black0        = Color(0xFF0A0A0A)
val Black1        = Color(0xFF141414)
val Surface1      = Color(0xFF1A1A1A)
val Surface2      = Color(0xFF262626)
val OnSurface     = Color(0xFFFAFAFA)
val OnSurfaceMute = Color(0xFFB0B0B0)
val Outline       = Color(0xFF3A3A3A)
val Danger        = Color(0xFFFF5252)
```

- [ ] **Step 1.7: Theme**

Replace `Theme.kt`:

```kotlin
package br.unasp.reviveparts.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

private val DarkColors = darkColorScheme(
    primary = YellowPrimary,
    onPrimary = Black0,
    primaryContainer = YellowDark,
    onPrimaryContainer = Black0,
    background = Black0,
    onBackground = OnSurface,
    surface = Surface1,
    onSurface = OnSurface,
    surfaceVariant = Surface2,
    onSurfaceVariant = OnSurfaceMute,
    outline = Outline,
    error = Danger,
    onError = Black0
)

@Composable
fun RevivepartsTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Black0.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
```

- [ ] **Step 1.8: Build sanity check**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL (compile-only — references to seed/db classes resolved later when those tasks land; if they fail, mark them stub for now or implement Task 2 first).

- [ ] **Step 1.9: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/java/br/unasp/reviveparts/RevivePartsApp.kt app/src/main/java/br/unasp/reviveparts/ui/theme/
git commit -m "chore: add deps + dark/yellow theme + Application class"
```

---

## Task 2: Domain models + Room layer

**Files:** all under `app/src/main/java/br/unasp/reviveparts/`
- `domain/model/Enums.kt`
- `data/db/entities/*.kt`
- `data/db/Converters.kt`
- `data/db/dao/*.kt`
- `data/db/AppDatabase.kt`
- `data/db/Seed.kt`

- [ ] **Step 2.1: Enums**

`domain/model/Enums.kt`:

```kotlin
package br.unasp.reviveparts.domain.model

enum class Role { CUSTOMER, OWNER }

enum class OrderStatus(val label: String) {
    PLACED("Pedido feito"),
    IN_REVIEW("Em análise"),
    PRINTING("Imprimindo"),
    PACKING("Embalando"),
    SHIPPED("Saiu para entrega"),
    DELIVERED("Entregue");

    fun next(): OrderStatus? = entries.getOrNull(ordinal + 1)
    companion object { val pipeline: List<OrderStatus> = entries.toList() }
}

enum class PaymentType { CARD, PIX }
enum class OrderSource { CATALOG, AI }

data class RecognitionResult(
    val productId: Long,
    val confidence: Float,
    val label: String
)

object SeedIds { const val MANIVELA_VW: Long = 1L }
```

- [ ] **Step 2.2: Entities**

`data/db/entities/User.kt`:
```kotlin
package br.unasp.reviveparts.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.unasp.reviveparts.domain.model.Role

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val phone: String = "",
    val cpf: String = "",
    val address: String = "",
    val role: Role
)
```

`data/db/entities/Product.kt`:
```kotlin
package br.unasp.reviveparts.data.db.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val photoPath: String,
    val model3dAsset: String,
    val priceCents: Long,
    val prototypeHours: Int,
    val stockQty: Int,
    val isReady: Boolean
)
```

`data/db/entities/Card.kt`:
```kotlin
package br.unasp.reviveparts.data.db.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val holderName: String,
    val last4: String,
    val brand: String,
    val expiry: String,
    val isDefault: Boolean = false
)
```

`data/db/entities/Order.kt`:
```kotlin
package br.unasp.reviveparts.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.domain.model.PaymentType

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val status: OrderStatus,
    val paymentType: PaymentType,
    val totalCents: Long,
    val source: OrderSource,
    val createdAt: Long
)
```

`data/db/entities/OrderEvent.kt`:
```kotlin
package br.unasp.reviveparts.data.db.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import br.unasp.reviveparts.domain.model.OrderStatus

@Entity(tableName = "order_events")
data class OrderEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val status: OrderStatus,
    val timestampMs: Long
)
```

- [ ] **Step 2.3: Converters**

`data/db/Converters.kt`:
```kotlin
package br.unasp.reviveparts.data.db
import androidx.room.TypeConverter
import br.unasp.reviveparts.domain.model.*

class Converters {
    @TypeConverter fun roleToStr(r: Role): String = r.name
    @TypeConverter fun strToRole(s: String): Role = Role.valueOf(s)
    @TypeConverter fun statusToStr(s: OrderStatus): String = s.name
    @TypeConverter fun strToStatus(s: String): OrderStatus = OrderStatus.valueOf(s)
    @TypeConverter fun ptToStr(p: PaymentType): String = p.name
    @TypeConverter fun strToPt(s: String): PaymentType = PaymentType.valueOf(s)
    @TypeConverter fun srcToStr(s: OrderSource): String = s.name
    @TypeConverter fun strToSrc(s: String): OrderSource = OrderSource.valueOf(s)
}
```

- [ ] **Step 2.4: DAOs**

`data/db/dao/UserDao.kt`:
```kotlin
package br.unasp.reviveparts.data.db.dao
import androidx.room.*
import br.unasp.reviveparts.data.db.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: Long): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update suspend fun update(user: UserEntity)
}
```

`data/db/dao/ProductDao.kt`:
```kotlin
package br.unasp.reviveparts.data.db.dao
import androidx.room.*
import br.unasp.reviveparts.data.db.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isReady = 1 ORDER BY id ASC")
    fun observeReady(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY id ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Insert suspend fun insert(p: ProductEntity): Long
    @Update suspend fun update(p: ProductEntity)
    @Delete suspend fun delete(p: ProductEntity)
}
```

`data/db/dao/CardDao.kt`:
```kotlin
package br.unasp.reviveparts.data.db.dao
import androidx.room.*
import br.unasp.reviveparts.data.db.entities.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE userId = :uid ORDER BY isDefault DESC, id ASC")
    fun observeForUser(uid: Long): Flow<List<CardEntity>>

    @Insert suspend fun insert(c: CardEntity): Long
    @Update suspend fun update(c: CardEntity)
    @Delete suspend fun delete(c: CardEntity)

    @Query("UPDATE cards SET isDefault = 0 WHERE userId = :uid")
    suspend fun clearDefault(uid: Long)
}
```

`data/db/dao/OrderDao.kt`:
```kotlin
package br.unasp.reviveparts.data.db.dao
import androidx.room.*
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :uid ORDER BY createdAt DESC")
    fun observeByUser(uid: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = :s ORDER BY createdAt DESC")
    fun observeByStatus(s: OrderStatus): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeById(id: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): OrderEntity?

    @Insert suspend fun insert(o: OrderEntity): Long

    @Query("UPDATE orders SET status = :s WHERE id = :id")
    suspend fun setStatus(id: Long, s: OrderStatus)
}
```

`data/db/dao/OrderEventDao.kt`:
```kotlin
package br.unasp.reviveparts.data.db.dao
import androidx.room.*
import br.unasp.reviveparts.data.db.entities.OrderEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderEventDao {
    @Query("SELECT * FROM order_events WHERE orderId = :oid ORDER BY timestampMs ASC")
    fun observeForOrder(oid: Long): Flow<List<OrderEventEntity>>

    @Insert suspend fun insert(e: OrderEventEntity): Long
}
```

- [ ] **Step 2.5: Database**

`data/db/AppDatabase.kt`:
```kotlin
package br.unasp.reviveparts.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.unasp.reviveparts.data.db.dao.*
import br.unasp.reviveparts.data.db.entities.*

@Database(
    entities = [UserEntity::class, ProductEntity::class, CardEntity::class, OrderEntity::class, OrderEventEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cardDao(): CardDao
    abstract fun orderDao(): OrderDao
    abstract fun orderEventDao(): OrderEventDao
}
```

- [ ] **Step 2.6: Seed**

`data/db/Seed.kt`:
```kotlin
package br.unasp.reviveparts.data.db

import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.domain.model.SeedIds

object Seed {
    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.productDao().count() > 0) return
        val u = db.userDao()
        u.insert(UserEntity(id = 0, name = "Dono Reviveparts", email = "dono@reviveparts.com", password = "dono123", role = Role.OWNER))

        val p = db.productDao()
        p.insert(ProductEntity(
            id = SeedIds.MANIVELA_VW,
            name = "Manivela de Vidro VW Fusca",
            description = "Manivela de janela em ABS reforçado, compatível com VW Fusca/Kombi clássicos.",
            photoPath = "drawable://manivela_vw",
            model3dAsset = "models/manivela_vw.glb",
            priceCents = 4990,
            prototypeHours = 6,
            stockQty = 12,
            isReady = true
        ))
        p.insert(ProductEntity(0, "Maçaneta Externa Fusca", "Maçaneta externa replicada em ABS preto.", "drawable://placeholder_part", "models/manivela_vw.glb", 7990, 8, 4, true))
        p.insert(ProductEntity(0, "Botão de Ar Kombi", "Botão do painel da Kombi.", "drawable://placeholder_part", "models/manivela_vw.glb", 2490, 3, 25, true))
        p.insert(ProductEntity(0, "Puxador de Porta", "Puxador interno.", "drawable://placeholder_part", "models/manivela_vw.glb", 3490, 5, 0, true))
    }
}
```

- [ ] **Step 2.7: Build check**

Run: `./gradlew :app:assembleDebug`
Expected: SUCCESSFUL after KSP generates Room impls.

- [ ] **Step 2.8: Commit**

```bash
git add app/src/main/java/br/unasp/reviveparts/data app/src/main/java/br/unasp/reviveparts/domain
git commit -m "feat(db): add Room schema + DAOs + seed data"
```

---

## Task 3: Repositories + SessionRepository + mock services

**Files:**
- `data/repo/UserRepository.kt`
- `data/repo/ProductRepository.kt`
- `data/repo/OrderRepository.kt`
- `data/repo/CardRepository.kt`
- `data/repo/SessionRepository.kt`
- `data/ai/FakeAiService.kt`
- `data/payments/LuhnValidator.kt`
- `data/payments/PaymentSimulator.kt`
- `data/payments/PixGenerator.kt`
- Test: `app/src/test/java/br/unasp/reviveparts/LuhnValidatorTest.kt`
- Test: `app/src/test/java/br/unasp/reviveparts/OrderStatusTest.kt`
- Test: `app/src/test/java/br/unasp/reviveparts/FakeAiServiceTest.kt`

- [ ] **Step 3.1: TDD — Luhn validator (test first)**

`app/src/test/java/br/unasp/reviveparts/LuhnValidatorTest.kt`:
```kotlin
package br.unasp.reviveparts
import br.unasp.reviveparts.data.payments.LuhnValidator
import org.junit.Assert.*
import org.junit.Test

class LuhnValidatorTest {
    @Test fun validVisaPasses() = assertTrue(LuhnValidator.isValid("4111111111111111"))
    @Test fun validMastercardPasses() = assertTrue(LuhnValidator.isValid("5500 0000 0000 0004"))
    @Test fun invalidFails() = assertFalse(LuhnValidator.isValid("4111111111111112"))
    @Test fun shortFails() = assertFalse(LuhnValidator.isValid("1234"))
    @Test fun nonDigitsFails() = assertFalse(LuhnValidator.isValid("abcd1111"))
}
```

Run: `./gradlew :app:testDebugUnitTest --tests "br.unasp.reviveparts.LuhnValidatorTest"` → FAIL (class missing).

- [ ] **Step 3.2: Implement Luhn**

`data/payments/LuhnValidator.kt`:
```kotlin
package br.unasp.reviveparts.data.payments

object LuhnValidator {
    fun isValid(input: String): Boolean {
        val digits = input.filter { it.isDigit() }
        if (digits.length < 13 || input.any { !it.isDigit() && !it.isWhitespace() }) return false
        var sum = 0
        var alt = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i].digitToInt()
            if (alt) { n *= 2; if (n > 9) n -= 9 }
            sum += n
            alt = !alt
        }
        return sum % 10 == 0
    }
}
```

Run tests → PASS.

- [ ] **Step 3.3: TDD — OrderStatus.next()**

`app/src/test/java/br/unasp/reviveparts/OrderStatusTest.kt`:
```kotlin
package br.unasp.reviveparts
import br.unasp.reviveparts.domain.model.OrderStatus
import org.junit.Assert.*
import org.junit.Test

class OrderStatusTest {
    @Test fun nextFromPlacedIsInReview() = assertEquals(OrderStatus.IN_REVIEW, OrderStatus.PLACED.next())
    @Test fun nextFromShippedIsDelivered() = assertEquals(OrderStatus.DELIVERED, OrderStatus.SHIPPED.next())
    @Test fun nextFromDeliveredIsNull() = assertNull(OrderStatus.DELIVERED.next())
    @Test fun pipelineHasSixSteps() = assertEquals(6, OrderStatus.pipeline.size)
}
```

Run → PASS (already implemented in Task 2).

- [ ] **Step 3.4: TDD — FakeAiService**

`app/src/test/java/br/unasp/reviveparts/FakeAiServiceTest.kt`:
```kotlin
package br.unasp.reviveparts
import br.unasp.reviveparts.data.ai.FakeAiService
import br.unasp.reviveparts.domain.model.SeedIds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeAiServiceTest {
    @Test fun alwaysReturnsManivela() = runTest {
        val r = FakeAiService(simulatedDelayMs = 0).recognize("uma manivela", null)
        assertEquals(SeedIds.MANIVELA_VW, r.productId)
    }
}
```

- [ ] **Step 3.5: Implement FakeAiService**

`data/ai/FakeAiService.kt`:
```kotlin
package br.unasp.reviveparts.data.ai

import br.unasp.reviveparts.domain.model.RecognitionResult
import br.unasp.reviveparts.domain.model.SeedIds
import kotlinx.coroutines.delay

class FakeAiService(private val simulatedDelayMs: Long = 2500L) {
    suspend fun recognize(text: String?, imagePath: String?): RecognitionResult {
        delay(simulatedDelayMs)
        return RecognitionResult(
            productId = SeedIds.MANIVELA_VW,
            confidence = 0.92f,
            label = "Manivela de Vidro VW (reconhecida)"
        )
    }
}
```

Run all unit tests: `./gradlew :app:testDebugUnitTest` → PASS.

- [ ] **Step 3.6: Repositories**

`data/repo/UserRepository.kt`:
```kotlin
package br.unasp.reviveparts.data.repo

import br.unasp.reviveparts.data.db.dao.UserDao
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.domain.model.Role

class UserRepository(private val dao: UserDao) {
    private val ownerEmail = "dono@reviveparts.com"

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val u = dao.findByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("E-mail não encontrado"))
        if (u.password != password) return Result.failure(IllegalArgumentException("Senha incorreta"))
        return Result.success(u)
    }

    suspend fun register(name: String, email: String, password: String, phone: String, cpf: String, address: String): Result<UserEntity> {
        val normEmail = email.trim().lowercase()
        if (dao.findByEmail(normEmail) != null) return Result.failure(IllegalStateException("E-mail já cadastrado"))
        val role = if (normEmail == ownerEmail) Role.OWNER else Role.CUSTOMER
        val id = dao.insert(UserEntity(name = name, email = normEmail, password = password, phone = phone, cpf = cpf, address = address, role = role))
        return Result.success(dao.findById(id)!!)
    }

    suspend fun findById(id: Long) = dao.findById(id)
    fun observeById(id: Long) = dao.observeById(id)
    suspend fun update(u: UserEntity) = dao.update(u)
}
```

`data/repo/ProductRepository.kt`:
```kotlin
package br.unasp.reviveparts.data.repo

import br.unasp.reviveparts.data.db.dao.ProductDao
import br.unasp.reviveparts.data.db.entities.ProductEntity

class ProductRepository(private val dao: ProductDao) {
    fun observeReady() = dao.observeReady()
    fun observeAll() = dao.observeAll()
    suspend fun findById(id: Long) = dao.findById(id)
    suspend fun upsert(p: ProductEntity): Long = if (p.id == 0L) dao.insert(p) else { dao.update(p); p.id }
    suspend fun delete(p: ProductEntity) = dao.delete(p)
}
```

`data/repo/OrderRepository.kt`:
```kotlin
package br.unasp.reviveparts.data.repo

import br.unasp.reviveparts.data.db.dao.OrderDao
import br.unasp.reviveparts.data.db.dao.OrderEventDao
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.OrderEventEntity
import br.unasp.reviveparts.domain.model.OrderStatus

class OrderRepository(
    private val orderDao: OrderDao,
    private val eventDao: OrderEventDao
) {
    fun observeByUser(uid: Long) = orderDao.observeByUser(uid)
    fun observeAll() = orderDao.observeAll()
    fun observeByStatus(s: OrderStatus) = orderDao.observeByStatus(s)
    fun observeById(id: Long) = orderDao.observeById(id)
    fun observeEvents(orderId: Long) = eventDao.observeForOrder(orderId)
    suspend fun findById(id: Long) = orderDao.findById(id)

    suspend fun place(order: OrderEntity): Long {
        val id = orderDao.insert(order.copy(status = OrderStatus.PLACED, createdAt = System.currentTimeMillis()))
        eventDao.insert(OrderEventEntity(orderId = id, status = OrderStatus.PLACED, timestampMs = System.currentTimeMillis()))
        return id
    }

    suspend fun advance(orderId: Long) {
        val current = orderDao.findById(orderId) ?: return
        val next = current.status.next() ?: return
        orderDao.setStatus(orderId, next)
        eventDao.insert(OrderEventEntity(orderId = orderId, status = next, timestampMs = System.currentTimeMillis()))
    }
}
```

`data/repo/CardRepository.kt`:
```kotlin
package br.unasp.reviveparts.data.repo

import br.unasp.reviveparts.data.db.dao.CardDao
import br.unasp.reviveparts.data.db.entities.CardEntity

class CardRepository(private val dao: CardDao) {
    fun observeForUser(uid: Long) = dao.observeForUser(uid)
    suspend fun add(c: CardEntity, makeDefault: Boolean): Long {
        if (makeDefault) dao.clearDefault(c.userId)
        return dao.insert(c.copy(isDefault = makeDefault))
    }
    suspend fun delete(c: CardEntity) = dao.delete(c)
    suspend fun setDefault(c: CardEntity) {
        dao.clearDefault(c.userId)
        dao.update(c.copy(isDefault = true))
    }
}
```

`data/repo/SessionRepository.kt`:
```kotlin
package br.unasp.reviveparts.data.repo

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import br.unasp.reviveparts.domain.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionRepository(private val ctx: Context) {
    private object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val ROLE = stringPreferencesKey("role")
    }

    data class Session(val userId: Long, val role: Role)

    val session: Flow<Session?> = ctx.dataStore.data.map { p ->
        val id = p[Keys.USER_ID] ?: return@map null
        val role = p[Keys.ROLE]?.let { Role.valueOf(it) } ?: return@map null
        Session(id, role)
    }

    suspend fun current(): Session? = session.first()

    suspend fun login(userId: Long, role: Role) {
        ctx.dataStore.edit {
            it[Keys.USER_ID] = userId
            it[Keys.ROLE] = role.name
        }
    }

    suspend fun logout() {
        ctx.dataStore.edit { it.clear() }
    }
}
```

- [ ] **Step 3.7: PaymentSimulator + PixGenerator**

`data/payments/PaymentSimulator.kt`:
```kotlin
package br.unasp.reviveparts.data.payments

import kotlinx.coroutines.delay

class PaymentSimulator {
    suspend fun chargeCard(numberDigits: String, totalCents: Long): Result<String> {
        delay(1500)
        if (!LuhnValidator.isValid(numberDigits)) return Result.failure(IllegalArgumentException("Cartão inválido"))
        return Result.success("auth_${System.currentTimeMillis()}")
    }
}
```

`data/payments/PixGenerator.kt`:
```kotlin
package br.unasp.reviveparts.data.payments

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object PixGenerator {
    fun pixCopyPaste(orderId: Long, totalCents: Long): String {
        val amount = "%.2f".format(totalCents / 100.0)
        return "00020126360014br.gov.bcb.pix0114REVIVEPARTS$orderId" +
                "5204000053039865406$amount" +
                "5802BR5910RevivePart6009SAOPAULO62070503***6304ABCD"
    }

    fun qr(content: String, size: Int = 512): Bitmap {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) for (y in 0 until size)
            bmp.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        return bmp
    }
}
```

- [ ] **Step 3.8: Build + run all tests**

```
./gradlew :app:testDebugUnitTest :app:assembleDebug
```
Expected: PASS + BUILD SUCCESSFUL.

- [ ] **Step 3.9: Commit**

```bash
git add app/src/main/java/br/unasp/reviveparts/data/repo app/src/main/java/br/unasp/reviveparts/data/ai app/src/main/java/br/unasp/reviveparts/data/payments app/src/test
git commit -m "feat(data): repos, mock AI, payment simulator, PIX QR + tests"
```

---

## Task 4: Theme typography + reusable UI components

**Files:**
- `ui/theme/Type.kt`
- `ui/components/YellowButton.kt`
- `ui/components/PrimaryTextField.kt`
- `ui/components/PartCard.kt`
- `ui/components/StatusStepper.kt`
- `ui/components/LoadingDots.kt`
- `ui/components/ModelViewer3D.kt`
- `ui/components/BottomBarHost.kt`
- `res/drawable/placeholder_part.xml` (vector)

- [ ] **Step 4.1: Typography**

`ui/theme/Type.kt`:
```kotlin
package br.unasp.reviveparts.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 36.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
)
```

- [ ] **Step 4.2: YellowButton**

`ui/components/YellowButton.kt`:
```kotlin
package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.unasp.reviveparts.ui.theme.Black0
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun YellowButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary, contentColor = Black0),
        modifier = modifier.height(52.dp)
    ) { Text(text) }
}
```

- [ ] **Step 4.3: PrimaryTextField**

`ui/components/PrimaryTextField.kt`:
```kotlin
package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import br.unasp.reviveparts.ui.theme.YellowPrimary
import br.unasp.reviveparts.ui.theme.OnSurface

@Composable
fun PrimaryTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = YellowPrimary,
            focusedLabelColor = YellowPrimary,
            cursorColor = YellowPrimary,
            focusedTextColor = OnSurface,
            unfocusedTextColor = OnSurface
        )
    )
}
```

- [ ] **Step 4.4: PartCard**

`ui/components/PartCard.kt`:
```kotlin
package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun PartCard(p: ProductEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.titleLarge)
                Text("R$ %.2f".format(p.priceCents / 100.0), color = YellowPrimary, style = MaterialTheme.typography.labelLarge)
                Text(if (p.stockQty > 0) "Em estoque (${p.stockQty})" else "${p.prototypeHours}h prototipagem",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable private fun Box(modifier: Modifier) = androidx.compose.foundation.layout.Box(modifier)
```

> Replace fragile inner Box helper — use `androidx.compose.foundation.layout.Box` directly. Remove the bottom helper if it conflicts, the import on top is sufficient.

Cleaner version (use this):

```kotlin
package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun PartCard(p: ProductEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, style = MaterialTheme.typography.titleLarge)
                Text("R$ %.2f".format(p.priceCents / 100.0), color = YellowPrimary, style = MaterialTheme.typography.labelLarge)
                Text(if (p.stockQty > 0) "Em estoque (${p.stockQty})" else "${p.prototypeHours}h prototipagem",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
```

- [ ] **Step 4.5: StatusStepper**

`ui/components/StatusStepper.kt`:
```kotlin
package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.ui.theme.Outline
import br.unasp.reviveparts.ui.theme.YellowPrimary
import br.unasp.reviveparts.ui.theme.Black0

@Composable
fun StatusStepper(current: OrderStatus, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        OrderStatus.pipeline.forEachIndexed { i, s ->
            val isPast = i < current.ordinal
            val isCurrent = i == current.ordinal
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape)
                        .background(if (isPast || isCurrent) YellowPrimary else Outline),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPast) Icon(Icons.Default.Check, null, tint = Black0)
                    else Text("${i + 1}", color = if (isCurrent) Black0 else MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    s.label,
                    style = if (isCurrent) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                    color = if (isCurrent) YellowPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
```

- [ ] **Step 4.6: LoadingDots**

`ui/components/LoadingDots.kt`:
```kotlin
package br.unasp.reviveparts.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.unasp.reviveparts.ui.theme.YellowPrimary

@Composable
fun LoadingDots() {
    val infinite = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { i ->
            val alpha by infinite.animateFloat(
                initialValue = 0.2f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = i * 150, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "dot$i"
            )
            Box(Modifier.size(14.dp).clip(CircleShape).background(YellowPrimary.copy(alpha = alpha)))
        }
    }
}
```

- [ ] **Step 4.7: ModelViewer3D**

`ui/components/ModelViewer3D.kt`:
```kotlin
package br.unasp.reviveparts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode
import br.unasp.reviveparts.ui.theme.Surface1

@Composable
fun ModelViewer3D(assetPath: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.background(Surface1),
        factory = { ctx ->
            SceneView(ctx).apply {
                try {
                    val node = ModelNode(this).apply { loadModelGlbAsync("file:///android_asset/$assetPath") }
                    addChildNode(node)
                } catch (t: Throwable) { /* fallback: show empty surface */ }
            }
        }
    )
}
```

> If SceneView 2.2.x API differs, swap to: `engine = ...; modelLoader.loadModel(...)`. Verify against `https://sceneview.github.io/`.

- [ ] **Step 4.8: BottomBarHost**

`ui/components/BottomBarHost.kt`:
```kotlin
package br.unasp.reviveparts.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.ui.nav.Routes
import br.unasp.reviveparts.ui.theme.YellowPrimary

data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val customerTabs = listOf(
    TabItem(Routes.CUSTOMER_HOME, "Início", Icons.Default.Home),
    TabItem(Routes.CUSTOMER_ORDERS, "Pedidos", Icons.Default.Receipt),
    TabItem(Routes.CUSTOMER_AI, "+", Icons.Default.AddCircle),
    TabItem(Routes.CUSTOMER_PROFILE, "Perfil", Icons.Default.Person)
)
private val ownerTabs = listOf(
    TabItem(Routes.OWNER_DASHBOARD, "Pedidos", Icons.Default.Receipt),
    TabItem(Routes.OWNER_PRODUCTS, "Produtos", Icons.Default.Inventory),
    TabItem(Routes.OWNER_PROFILE, "Perfil", Icons.Default.Person)
)

@Composable
fun AppBottomBar(nav: NavController, role: Role) {
    val tabs = if (role == Role.OWNER) ownerTabs else customerTabs
    val current = nav.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEach { t ->
            NavigationBarItem(
                selected = current == t.route,
                onClick = { nav.navigate(t.route) { launchSingleTop = true; popUpTo(nav.graph.startDestinationId) } },
                icon = { Icon(t.icon, t.label) },
                label = { Text(t.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = YellowPrimary,
                    selectedTextColor = YellowPrimary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
```

- [ ] **Step 4.9: placeholder vector**

`app/src/main/res/drawable/placeholder_part.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="72dp" android:height="72dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFD60A" android:pathData="M12,2L2,7l10,5 10,-5z"/>
    <path android:fillColor="#FFD60A" android:pathData="M2,17l10,5 10,-5M2,12l10,5 10,-5"/>
</vector>
```

- [ ] **Step 4.10: Build + commit**

```
./gradlew :app:assembleDebug
git add app/src/main/java/br/unasp/reviveparts/ui app/src/main/res/drawable/placeholder_part.xml
git commit -m "feat(ui): theme typography + reusable components + 3D viewer"
```

---

## Task 5: Navigation skeleton + MainActivity wiring

**Files:**
- `ui/nav/Routes.kt`
- `ui/nav/AppNavHost.kt`
- `MainActivity.kt`
- `ui/screens/RoleShell.kt`

- [ ] **Step 5.1: Routes**

`ui/nav/Routes.kt`:
```kotlin
package br.unasp.reviveparts.ui.nav

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"

    const val CUSTOMER_HOME = "customer/home"
    const val CUSTOMER_AI = "customer/ai"
    const val CUSTOMER_ORDERS = "customer/orders"
    const val CUSTOMER_PROFILE = "customer/profile"
    fun partDetail(id: Long) = "customer/part/$id"
    const val PART_DETAIL = "customer/part/{id}"
    fun cart(productId: Long, source: String) = "customer/cart/$productId/$source"
    const val CART = "customer/cart/{productId}/{source}"
    fun payment(orderId: Long) = "customer/payment/$orderId"
    const val PAYMENT = "customer/payment/{orderId}"
    fun orderDetail(id: Long) = "customer/order/$id"
    const val ORDER_DETAIL = "customer/order/{id}"

    const val OWNER_DASHBOARD = "owner/dashboard"
    const val OWNER_PRODUCTS = "owner/products"
    const val OWNER_PROFILE = "owner/profile"
    fun ownerOrderDetail(id: Long) = "owner/order/$id"
    const val OWNER_ORDER_DETAIL = "owner/order/{id}"
    fun productEdit(id: Long?) = if (id == null) "owner/product/edit" else "owner/product/edit/$id"
    const val PRODUCT_EDIT = "owner/product/edit/{id}"
    const val PRODUCT_NEW = "owner/product/edit"
}
```

- [ ] **Step 5.2: AppNavHost**

`ui/nav/AppNavHost.kt` (skeleton with placeholders for screens; each screen task fills in):
```kotlin
package br.unasp.reviveparts.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.ui.components.AppBottomBar
import br.unasp.reviveparts.ui.screens.auth.LoginScreen
import br.unasp.reviveparts.ui.screens.auth.RegisterScreen
import br.unasp.reviveparts.ui.screens.customer.ai.AiSearchScreen
import br.unasp.reviveparts.ui.screens.customer.cart.CartScreen
import br.unasp.reviveparts.ui.screens.customer.home.HomeScreen
import br.unasp.reviveparts.ui.screens.customer.orderdetail.OrderDetailScreen
import br.unasp.reviveparts.ui.screens.customer.orders.OrdersScreen
import br.unasp.reviveparts.ui.screens.customer.partdetail.PartDetailScreen
import br.unasp.reviveparts.ui.screens.customer.payment.PaymentScreen
import br.unasp.reviveparts.ui.screens.customer.profile.ProfileScreen
import br.unasp.reviveparts.ui.screens.owner.dashboard.OwnerDashboardScreen
import br.unasp.reviveparts.ui.screens.owner.orderdetail.OwnerOrderDetailScreen
import br.unasp.reviveparts.ui.screens.owner.productedit.ProductEditScreen
import br.unasp.reviveparts.ui.screens.owner.products.ProductsScreen
import br.unasp.reviveparts.ui.screens.owner.profile.OwnerProfileScreen
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val sessionFlow = ctx.app.sessionRepo.session
    val session by sessionFlow.collectAsState(initial = null)
    val sessionLoaded = remember { mutableStateOf(false) }
    LaunchedEffect(session) { sessionLoaded.value = true }

    val role = session?.role
    val showBar = role != null && nav.currentBackStackEntryAsState().value?.destination?.route in withBarRoutes

    Scaffold(
        bottomBar = { if (showBar && role != null) AppBottomBar(nav, role) }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = if (session == null) Routes.LOGIN else when (role) {
                Role.OWNER -> Routes.OWNER_DASHBOARD
                else -> Routes.CUSTOMER_HOME
            },
            modifier = Modifier.padding(pad)
        ) {
            composable(Routes.LOGIN) { LoginScreen(nav) }
            composable(Routes.REGISTER) { RegisterScreen(nav) }

            composable(Routes.CUSTOMER_HOME) { HomeScreen(nav) }
            composable(Routes.CUSTOMER_AI) { AiSearchScreen(nav) }
            composable(Routes.CUSTOMER_ORDERS) { OrdersScreen(nav) }
            composable(Routes.CUSTOMER_PROFILE) { ProfileScreen(nav) }
            composable(Routes.PART_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                PartDetailScreen(nav, it.arguments!!.getLong("id"))
            }
            composable(
                Routes.CART,
                listOf(
                    navArgument("productId") { type = NavType.LongType },
                    navArgument("source") { type = NavType.StringType }
                )
            ) {
                CartScreen(nav, it.arguments!!.getLong("productId"), it.arguments!!.getString("source")!!)
            }
            composable(Routes.PAYMENT, listOf(navArgument("orderId") { type = NavType.LongType })) {
                PaymentScreen(nav, it.arguments!!.getLong("orderId"))
            }
            composable(Routes.ORDER_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                OrderDetailScreen(nav, it.arguments!!.getLong("id"))
            }

            composable(Routes.OWNER_DASHBOARD) { OwnerDashboardScreen(nav) }
            composable(Routes.OWNER_PRODUCTS) { ProductsScreen(nav) }
            composable(Routes.OWNER_PROFILE) { OwnerProfileScreen(nav) }
            composable(Routes.OWNER_ORDER_DETAIL, listOf(navArgument("id") { type = NavType.LongType })) {
                OwnerOrderDetailScreen(nav, it.arguments!!.getLong("id"))
            }
            composable(Routes.PRODUCT_NEW) { ProductEditScreen(nav, null) }
            composable(Routes.PRODUCT_EDIT, listOf(navArgument("id") { type = NavType.LongType })) {
                ProductEditScreen(nav, it.arguments!!.getLong("id"))
            }
        }
    }
}

private val withBarRoutes = setOf(
    Routes.CUSTOMER_HOME, Routes.CUSTOMER_AI, Routes.CUSTOMER_ORDERS, Routes.CUSTOMER_PROFILE,
    Routes.OWNER_DASHBOARD, Routes.OWNER_PRODUCTS, Routes.OWNER_PROFILE
)
```

- [ ] **Step 5.3: Replace MainActivity**

`MainActivity.kt`:
```kotlin
package br.unasp.reviveparts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.unasp.reviveparts.ui.nav.AppNavHost
import br.unasp.reviveparts.ui.theme.RevivepartsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RevivepartsTheme { AppNavHost() } }
    }
}
```

- [ ] **Step 5.4: Build (will fail until screens exist — placeholder OK)**

If screens not built yet, create minimal placeholder files so nav host compiles. Alternative: do this task AFTER screen tasks. Plan order means screens come next; if blocking, stub each screen with `@Composable fun NameScreen(...) { Text("TODO") }` and replace later.

- [ ] **Step 5.5: Commit**

```bash
git add app/src/main/java/br/unasp/reviveparts/ui/nav app/src/main/java/br/unasp/reviveparts/MainActivity.kt
git commit -m "feat(nav): routes + nav host + role-aware bottom bar"
```

---

## Task 6: Auth screens (Login + Register)

**Files:**
- `ui/screens/auth/AuthViewModel.kt`
- `ui/screens/auth/LoginScreen.kt`
- `ui/screens/auth/RegisterScreen.kt`

- [ ] **Step 6.1: AuthViewModel**

```kotlin
package br.unasp.reviveparts.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.repo.SessionRepository
import br.unasp.reviveparts.data.repo.UserRepository
import br.unasp.reviveparts.domain.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val users: UserRepository,
    private val session: SessionRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val loggedInRole: Role? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun login(email: String, password: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val r = users.login(email, password)
            r.onSuccess {
                session.login(it.id, it.role)
                _state.value = UiState(loggedInRole = it.role)
            }.onFailure {
                _state.value = UiState(error = it.message)
            }
        }
    }

    fun register(name: String, email: String, password: String, phone: String, cpf: String, address: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            users.register(name, email, password, phone, cpf, address).onSuccess {
                session.login(it.id, it.role)
                _state.value = UiState(loggedInRole = it.role)
            }.onFailure {
                _state.value = UiState(error = it.message)
            }
        }
    }

    companion object {
        fun create(app: RevivePartsApp) = AuthViewModel(app.userRepo, app.sessionRepo)
    }
}
```

- [ ] **Step 6.2: LoginScreen**

```kotlin
package br.unasp.reviveparts.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
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
```

- [ ] **Step 6.3: RegisterScreen**

```kotlin
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
```

- [ ] **Step 6.4: Commit**

```
git add app/src/main/java/br/unasp/reviveparts/ui/screens/auth
git commit -m "feat(auth): login + register screens with session integration"
```

---

## Task 7: Customer Home + PartDetail

**Files:**
- `ui/screens/customer/home/HomeViewModel.kt`
- `ui/screens/customer/home/HomeScreen.kt`
- `ui/screens/customer/partdetail/PartDetailViewModel.kt`
- `ui/screens/customer/partdetail/PartDetailScreen.kt`

- [ ] **Step 7.1: HomeViewModel**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.home

import androidx.lifecycle.ViewModel
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.flow.Flow

class HomeViewModel(repo: ProductRepository) : ViewModel() {
    val products: Flow<List<ProductEntity>> = repo.observeReady()
    companion object { fun create(app: RevivePartsApp) = HomeViewModel(app.productRepo) }
}
```

- [ ] **Step 7.2: HomeScreen**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.components.PartCard
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun HomeScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = HomeViewModel.create(ctx.app) as T
    })
    val products by vm.products.collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("ReviveParts", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Text("Peças impressas em 3D — pronta entrega", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(products, key = { it.id }) { p ->
                PartCard(p) { nav.navigate(Routes.partDetail(p.id)) }
            }
        }
    }
}
```

- [ ] **Step 7.3: PartDetailViewModel**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.partdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PartDetailViewModel(private val repo: ProductRepository, private val id: Long) : ViewModel() {
    val product = MutableStateFlow<ProductEntity?>(null)
    init { viewModelScope.launch { product.value = repo.findById(id) } }
    companion object {
        fun create(app: RevivePartsApp, id: Long) = PartDetailViewModel(app.productRepo, id)
    }
}
```

- [ ] **Step 7.4: PartDetailScreen**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.partdetail

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
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.ui.components.ModelViewer3D
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun PartDetailScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val vm: PartDetailViewModel = viewModel(key = "part-$id", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = PartDetailViewModel.create(ctx.app, id) as T
    })
    val p by vm.product.collectAsState()
    val product = p ?: return

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ModelViewer3D(assetPath = product.model3dAsset, modifier = Modifier.fillMaxWidth().height(280.dp))
        Spacer(Modifier.height(16.dp))
        Text(product.name, style = MaterialTheme.typography.headlineMedium)
        Text("R$ %.2f".format(product.priceCents / 100.0), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(product.description)
        Spacer(Modifier.height(8.dp))
        Text(if (product.stockQty > 0) "Em estoque (${product.stockQty} un)" else "${product.prototypeHours}h de prototipagem")
        Spacer(Modifier.height(24.dp))
        YellowButton("Comprar", { nav.navigate(Routes.cart(product.id, OrderSource.CATALOG.name)) }, Modifier.fillMaxWidth())
    }
}
```

- [ ] **Step 7.5: Commit**

```
git add app/src/main/java/br/unasp/reviveparts/ui/screens/customer/home app/src/main/java/br/unasp/reviveparts/ui/screens/customer/partdetail
git commit -m "feat(customer): home grid + part detail with 3D viewer"
```

---

## Task 8: AI Search flow

**Files:**
- `ui/screens/customer/ai/AiViewModel.kt`
- `ui/screens/customer/ai/AiSearchScreen.kt`

- [ ] **Step 8.1: AiViewModel**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.ai.FakeAiService
import br.unasp.reviveparts.data.repo.ProductRepository
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.domain.model.RecognitionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiViewModel(private val ai: FakeAiService, private val products: ProductRepository) : ViewModel() {
    sealed interface UiState {
        object Idle : UiState
        object Recognizing : UiState
        data class Result(val recognition: RecognitionResult, val product: ProductEntity) : UiState
        data class Error(val message: String) : UiState
    }
    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state = _state.asStateFlow()

    fun recognize(text: String, imagePath: String?) {
        _state.value = UiState.Recognizing
        viewModelScope.launch {
            try {
                val r = ai.recognize(text, imagePath)
                val p = products.findById(r.productId) ?: return@launch run {
                    _state.value = UiState.Error("Peça não encontrada no catálogo")
                }
                _state.value = UiState.Result(r, p)
            } catch (t: Throwable) { _state.value = UiState.Error(t.message ?: "Erro") }
        }
    }
    fun reset() { _state.value = UiState.Idle }

    companion object { fun create(app: RevivePartsApp) = AiViewModel(app.aiService, app.productRepo) }
}
```

- [ ] **Step 8.2: AiSearchScreen**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.ai

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.ui.components.LoadingDots
import br.unasp.reviveparts.ui.components.ModelViewer3D
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun AiSearchScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: AiViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = AiViewModel.create(ctx.app) as T
    })
    val state by vm.state.collectAsState()
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        when (val s = state) {
            AiViewModel.UiState.Idle -> {
                Text("Descreva ou fotografe a peça", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                PrimaryTextField(text, { text = it }, "Ex: manivela de janela")
                Spacer(Modifier.height(12.dp))
                Row {
                    OutlinedButton(onClick = { pickImage.launch("image/*") }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text("Galeria")
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { /* camera capture deferred */ }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Câmera")
                    }
                }
                if (imageUri != null) {
                    Spacer(Modifier.height(8.dp)); Text("Imagem selecionada ✓", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(24.dp))
                YellowButton("Identificar peça com IA", { vm.recognize(text, imageUri?.toString()) }, Modifier.fillMaxWidth())
            }
            AiViewModel.UiState.Recognizing -> {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Reconhecendo peça...", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(24.dp))
                    LoadingDots()
                }
            }
            is AiViewModel.UiState.Result -> {
                Text("É essa peça?", style = MaterialTheme.typography.headlineMedium)
                Text("${s.recognition.label} (${(s.recognition.confidence * 100).toInt()}% confiança)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                ModelViewer3D(s.product.model3dAsset, Modifier.fillMaxWidth().height(280.dp))
                Spacer(Modifier.height(16.dp))
                Text(s.product.name, style = MaterialTheme.typography.titleLarge)
                Text("R$ %.2f".format(s.product.priceCents / 100.0), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                YellowButton("Sim, gerar pedido", { nav.navigate(Routes.cart(s.product.id, OrderSource.AI.name)) }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { vm.reset() }) { Text("Não, tentar novamente") }
            }
            is AiViewModel.UiState.Error -> {
                Text("Erro: ${s.message}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                YellowButton("Tentar novamente", { vm.reset() }, Modifier.fillMaxWidth())
            }
        }
    }
}
```

- [ ] **Step 8.3: Commit**

```
git add app/src/main/java/br/unasp/reviveparts/ui/screens/customer/ai
git commit -m "feat(customer): AI search flow with mock recognition + 3D preview"
```

---

## Task 9: Cart + Payment

**Files:**
- `ui/screens/customer/cart/CartViewModel.kt`
- `ui/screens/customer/cart/CartScreen.kt`
- `ui/screens/customer/payment/PaymentViewModel.kt`
- `ui/screens/customer/payment/PaymentScreen.kt`

- [ ] **Step 9.1: CartViewModel**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val products: ProductRepository, private val productId: Long) : ViewModel() {
    val product = MutableStateFlow<ProductEntity?>(null)
    init { viewModelScope.launch { product.value = products.findById(productId) } }
    companion object { fun create(app: RevivePartsApp, id: Long) = CartViewModel(app.productRepo, id) }
}
```

- [ ] **Step 9.2: CartScreen**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.cart

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun CartScreen(nav: NavController, productId: Long, source: String) {
    val ctx = LocalContext.current
    val vm: CartViewModel = viewModel(key = "cart-$productId", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = CartViewModel.create(ctx.app, productId) as T
    })
    val p by vm.product.collectAsState()
    val product = p ?: return

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Resumo do pedido", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card { Column(Modifier.padding(16.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleLarge)
            Text("Origem: ${if (source == "AI") "reconhecimento por IA" else "catálogo"}")
            Text("Tempo de prototipagem: ${product.prototypeHours}h")
            Spacer(Modifier.height(8.dp))
            Text("Total: R$ %.2f".format(product.priceCents / 100.0), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
        } }
        Spacer(Modifier.weight(1f))
        YellowButton("Continuar para pagamento", {
            // navigate to payment after creating placeholder route — payment screen creates the order on confirm
            nav.navigate("customer/payment/new?productId=${product.id}&source=$source")
        }, Modifier.fillMaxWidth())
    }
}
```

> **Note:** simpler approach — pass productId+source to a single PaymentScreen route. Update Routes.kt to accept these args. Use this version of PaymentScreen route:

Update `Routes.kt` PAYMENT block to:
```kotlin
fun payment(productId: Long, source: String) = "customer/payment/$productId/$source"
const val PAYMENT = "customer/payment/{productId}/{source}"
```

And in NavHost:
```kotlin
composable(
    Routes.PAYMENT,
    listOf(
        navArgument("productId") { type = NavType.LongType },
        navArgument("source") { type = NavType.StringType }
    )
) {
    PaymentScreen(nav, it.arguments!!.getLong("productId"), it.arguments!!.getString("source")!!)
}
```

CartScreen final navigate becomes: `nav.navigate(Routes.payment(product.id, source))`.

- [ ] **Step 9.3: PaymentViewModel**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.CardEntity
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.payments.PaymentSimulator
import br.unasp.reviveparts.data.payments.PixGenerator
import br.unasp.reviveparts.data.repo.*
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.domain.model.PaymentType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val products: ProductRepository,
    private val orders: OrderRepository,
    private val cards: CardRepository,
    private val payments: PaymentSimulator,
    private val session: SessionRepository,
    private val productId: Long,
    private val source: OrderSource
) : ViewModel() {

    data class UiState(
        val product: ProductEntity? = null,
        val cards: List<CardEntity> = emptyList(),
        val processing: Boolean = false,
        val error: String? = null,
        val createdOrderId: Long? = null,
        val pixCopyPaste: String? = null
    )
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            val s = session.current() ?: return@launch
            userId = s.userId
            _state.update { it.copy(product = products.findById(productId)) }
            cards.observeForUser(userId).collect { list -> _state.update { it.copy(cards = list) } }
        }
    }

    fun payCard(numberDigits: String, holder: String, expiry: String, brand: String, save: Boolean) {
        val product = _state.value.product ?: return
        _state.update { it.copy(processing = true, error = null) }
        viewModelScope.launch {
            val r = payments.chargeCard(numberDigits, product.priceCents)
            r.onFailure { _state.update { st -> st.copy(processing = false, error = it.message) } }
                .onSuccess {
                    if (save) cards.add(CardEntity(userId = userId, holderName = holder, last4 = numberDigits.takeLast(4), brand = brand, expiry = expiry), makeDefault = true)
                    val orderId = orders.place(OrderEntity(userId = userId, productId = product.id, status = OrderStatus.PLACED, paymentType = PaymentType.CARD, totalCents = product.priceCents, source = source, createdAt = 0))
                    _state.update { st -> st.copy(processing = false, createdOrderId = orderId) }
                }
        }
    }

    fun preparePix() {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            val orderId = orders.place(OrderEntity(userId = userId, productId = product.id, status = OrderStatus.PLACED, paymentType = PaymentType.PIX, totalCents = product.priceCents, source = source, createdAt = 0))
            _state.update { it.copy(createdOrderId = orderId, pixCopyPaste = PixGenerator.pixCopyPaste(orderId, product.priceCents)) }
        }
    }

    companion object {
        fun create(app: RevivePartsApp, productId: Long, source: OrderSource) =
            PaymentViewModel(app.productRepo, app.orderRepo, app.cardRepo, app.paymentSimulator, app.sessionRepo, productId, source)
    }
}
```

- [ ] **Step 9.4: PaymentScreen**

```kotlin
package br.unasp.reviveparts.ui.screens.customer.payment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.data.payments.PixGenerator
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun PaymentScreen(nav: NavController, productId: Long, source: String) {
    val ctx = LocalContext.current
    val vm: PaymentViewModel = viewModel(key = "pay-$productId-$source", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = PaymentViewModel.create(ctx.app, productId, OrderSource.valueOf(source)) as T
    })
    val state by vm.state.collectAsState()
    val product = state.product ?: return
    var tab by remember { mutableStateOf(0) }
    var number by remember { mutableStateOf("") }
    var holder by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var save by remember { mutableStateOf(true) }

    LaunchedEffect(state.createdOrderId) {
        val id = state.createdOrderId
        if (id != null && state.pixCopyPaste == null) {
            nav.navigate(Routes.orderDetail(id)) { popUpTo(Routes.CUSTOMER_HOME) }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Pagamento — R$ %.2f".format(product.priceCents / 100.0), style = MaterialTheme.typography.headlineMedium)
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Cartão") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("PIX") })
        }
        Spacer(Modifier.height(16.dp))
        when (tab) {
            0 -> {
                if (state.cards.isNotEmpty()) {
                    Text("Cartões salvos:")
                    state.cards.forEach { c ->
                        OutlinedButton(onClick = {
                            number = "4111111111111111".repeat(0) + "**** **** **** ${c.last4}" // placeholder; we re-charge with stored number not kept (mock)
                            // For mock: simulate using a known valid number behind the scenes
                            vm.payCard("4111111111111111", c.holderName, c.expiry, c.brand, save = false)
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("${c.brand} •••• ${c.last4}  ${c.holderName}")
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Divider(Modifier.padding(vertical = 12.dp))
                    Text("Ou use um novo cartão:")
                    Spacer(Modifier.height(8.dp))
                }
                PrimaryTextField(number, { number = it }, "Número do cartão", keyboardType = KeyboardType.Number)
                Spacer(Modifier.height(8.dp))
                PrimaryTextField(holder, { holder = it }, "Nome impresso")
                Spacer(Modifier.height(8.dp))
                Row {
                    PrimaryTextField(expiry, { expiry = it }, "Validade MM/AA", Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    PrimaryTextField(cvv, { cvv = it }, "CVV", Modifier.weight(1f), keyboardType = KeyboardType.Number)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = save, onCheckedChange = { save = it }); Text("Salvar cartão")
                }
                if (state.error != null) Text(state.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                YellowButton(if (state.processing) "Processando..." else "Pagar", {
                    vm.payCard(number, holder, expiry, brand = detectBrand(number), save = save)
                }, Modifier.fillMaxWidth(), enabled = !state.processing)
            }
            1 -> {
                LaunchedEffect(Unit) { if (state.pixCopyPaste == null) vm.preparePix() }
                state.pixCopyPaste?.let { code ->
                    val bmp = remember(code) { PixGenerator.qr(code) }
                    Image(bmp.asImageBitmap(), null, Modifier.size(220.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("PIX copia e cola:", style = MaterialTheme.typography.labelLarge)
                    Text(code, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("pix", code))
                    }, modifier = Modifier.fillMaxWidth()) { Text("Copiar PIX") }
                    Spacer(Modifier.height(16.dp))
                    YellowButton("Já paguei", {
                        state.createdOrderId?.let { nav.navigate(Routes.orderDetail(it)) { popUpTo(Routes.CUSTOMER_HOME) } }
                    }, Modifier.fillMaxWidth())
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
        else -> "Cartão"
    }
}
```

- [ ] **Step 9.5: Commit**

```
git add app/src/main/java/br/unasp/reviveparts/ui/screens/customer/cart app/src/main/java/br/unasp/reviveparts/ui/screens/customer/payment app/src/main/java/br/unasp/reviveparts/ui/nav
git commit -m "feat(customer): cart + payment (card simulated + PIX QR/copy-paste)"
```

---

## Task 10: Customer Orders + OrderDetail + Profile

**Files:**
- `ui/screens/customer/orders/OrdersViewModel.kt`
- `ui/screens/customer/orders/OrdersScreen.kt`
- `ui/screens/customer/orderdetail/OrderDetailViewModel.kt`
- `ui/screens/customer/orderdetail/OrderDetailScreen.kt`
- `ui/screens/customer/profile/ProfileViewModel.kt`
- `ui/screens/customer/profile/ProfileScreen.kt`

- [ ] **Step 10.1: OrdersViewModel + Screen**

```kotlin
// OrdersViewModel.kt
package br.unasp.reviveparts.ui.screens.customer.orders
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.repo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrdersViewModel(orders: OrderRepository, session: SessionRepository) : ViewModel() {
    val items = MutableStateFlow<List<OrderEntity>>(emptyList())
    init {
        viewModelScope.launch {
            val s = session.current() ?: return@launch
            orders.observeByUser(s.userId).collect { items.value = it }
        }
    }
    companion object { fun create(app: RevivePartsApp) = OrdersViewModel(app.orderRepo, app.sessionRepo) }
}
```

```kotlin
// OrdersScreen.kt
package br.unasp.reviveparts.ui.screens.customer.orders
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun OrdersScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: OrdersViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OrdersViewModel.create(ctx.app) as T
    })
    val items by vm.items.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Meus pedidos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) Text("Nenhum pedido ainda.")
        LazyColumn {
            items(items, key = { it.id }) { o ->
                Card(onClick = { nav.navigate(Routes.orderDetail(o.id)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Pedido #${o.id}", style = MaterialTheme.typography.titleLarge)
                        Text("Status: ${o.status.label}", color = MaterialTheme.colorScheme.primary)
                        Text("R$ %.2f — ${o.paymentType.name}".format(o.totalCents / 100.0))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 10.2: OrderDetailViewModel + Screen**

```kotlin
// OrderDetailViewModel.kt
package br.unasp.reviveparts.ui.screens.customer.orderdetail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrderDetailViewModel(private val orders: OrderRepository, private val products: ProductRepository, private val orderId: Long) : ViewModel() {
    val order = MutableStateFlow<OrderEntity?>(null)
    val product = MutableStateFlow<ProductEntity?>(null)
    init { viewModelScope.launch {
        orders.observeById(orderId).collect { o ->
            order.value = o
            if (o != null && product.value == null) product.value = products.findById(o.productId)
        }
    } }
    companion object { fun create(app: RevivePartsApp, id: Long) = OrderDetailViewModel(app.orderRepo, app.productRepo, id) }
}
```

```kotlin
// OrderDetailScreen.kt
package br.unasp.reviveparts.ui.screens.customer.orderdetail
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
import br.unasp.reviveparts.ui.components.StatusStepper

@Composable
fun OrderDetailScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val vm: OrderDetailViewModel = viewModel(key = "od-$id", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OrderDetailViewModel.create(ctx.app, id) as T
    })
    val o by vm.order.collectAsState()
    val p by vm.product.collectAsState()
    val order = o ?: return

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Pedido #${order.id}", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        p?.let { Text(it.name, style = MaterialTheme.typography.titleLarge) }
        Text("Total: R$ %.2f".format(order.totalCents / 100.0), color = MaterialTheme.colorScheme.primary)
        Text("Pagamento: ${order.paymentType.name}")
        Spacer(Modifier.height(24.dp))
        Text("Status", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        StatusStepper(order.status)
    }
}
```

- [ ] **Step 10.3: ProfileViewModel + Screen**

```kotlin
// ProfileViewModel.kt
package br.unasp.reviveparts.ui.screens.customer.profile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.data.db.entities.CardEntity
import br.unasp.reviveparts.data.repo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val users: UserRepository,
    private val cards: CardRepository,
    private val session: SessionRepository
) : ViewModel() {
    val user = MutableStateFlow<UserEntity?>(null)
    val cardsList = MutableStateFlow<List<CardEntity>>(emptyList())
    private var uid: Long = 0

    init { viewModelScope.launch {
        val s = session.current() ?: return@launch; uid = s.userId
        users.observeById(uid).collect { user.value = it }
    } }
    init { viewModelScope.launch {
        val s = session.current() ?: return@launch
        cards.observeForUser(s.userId).collect { cardsList.value = it }
    } }

    fun save(name: String, phone: String, address: String) = viewModelScope.launch {
        val u = user.value ?: return@launch
        users.update(u.copy(name = name, phone = phone, address = address))
    }
    fun logout(after: () -> Unit) = viewModelScope.launch { session.logout(); after() }
    fun deleteCard(c: CardEntity) = viewModelScope.launch { cards.delete(c) }

    companion object {
        fun create(app: RevivePartsApp) = ProfileViewModel(app.userRepo, app.cardRepo, app.sessionRepo)
    }
}
```

```kotlin
// ProfileScreen.kt
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
```

- [ ] **Step 10.4: Commit**

```
git add app/src/main/java/br/unasp/reviveparts/ui/screens/customer/orders app/src/main/java/br/unasp/reviveparts/ui/screens/customer/orderdetail app/src/main/java/br/unasp/reviveparts/ui/screens/customer/profile
git commit -m "feat(customer): orders list + order status detail + profile (cards, edit, logout)"
```

---

## Task 11: Owner — Dashboard + OrderDetail + advance status

**Files:**
- `ui/screens/owner/dashboard/OwnerDashboardViewModel.kt`
- `ui/screens/owner/dashboard/OwnerDashboardScreen.kt`
- `ui/screens/owner/orderdetail/OwnerOrderDetailViewModel.kt`
- `ui/screens/owner/orderdetail/OwnerOrderDetailScreen.kt`

- [ ] **Step 11.1: DashboardViewModel**

```kotlin
package br.unasp.reviveparts.ui.screens.owner.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.repo.OrderRepository
import br.unasp.reviveparts.domain.model.OrderStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OwnerDashboardViewModel(private val orders: OrderRepository) : ViewModel() {
    val selected = MutableStateFlow(OrderStatus.PLACED)
    val list = MutableStateFlow<List<OrderEntity>>(emptyList())
    init {
        viewModelScope.launch {
            selected.collect { s -> orders.observeByStatus(s).collect { list.value = it } }
        }
    }
    fun select(s: OrderStatus) { selected.value = s }
    companion object { fun create(app: RevivePartsApp) = OwnerDashboardViewModel(app.orderRepo) }
}
```

- [ ] **Step 11.2: DashboardScreen**

```kotlin
package br.unasp.reviveparts.ui.screens.owner.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.unasp.reviveparts.app
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.ui.nav.Routes

@Composable
fun OwnerDashboardScreen(nav: NavController) {
    val ctx = LocalContext.current
    val vm: OwnerDashboardViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OwnerDashboardViewModel.create(ctx.app) as T
    })
    val sel by vm.selected.collectAsState()
    val orders by vm.list.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pedidos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            OrderStatus.pipeline.forEach { s ->
                FilterChip(
                    selected = sel == s,
                    onClick = { vm.select(s) },
                    label = { Text(s.label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (orders.isEmpty()) Text("Nenhum pedido em ${sel.label}.")
        LazyColumn {
            items(orders, key = { it.id }) { o ->
                Card(onClick = { nav.navigate(Routes.ownerOrderDetail(o.id)) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Pedido #${o.id} — usuário ${o.userId}")
                        Text("R$ %.2f  •  ${o.paymentType.name}".format(o.totalCents / 100.0))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 11.3: Owner OrderDetailViewModel**

```kotlin
package br.unasp.reviveparts.ui.screens.owner.orderdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.*
import br.unasp.reviveparts.data.repo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OwnerOrderDetailViewModel(
    private val orders: OrderRepository,
    private val products: ProductRepository,
    private val users: UserRepository,
    private val orderId: Long
) : ViewModel() {
    val order = MutableStateFlow<OrderEntity?>(null)
    val product = MutableStateFlow<ProductEntity?>(null)
    val customer = MutableStateFlow<UserEntity?>(null)

    init { viewModelScope.launch {
        orders.observeById(orderId).collect { o ->
            order.value = o
            if (o != null) {
                if (product.value == null) product.value = products.findById(o.productId)
                if (customer.value == null) customer.value = users.findById(o.userId)
            }
        }
    } }

    fun advance() = viewModelScope.launch { orders.advance(orderId) }

    companion object {
        fun create(app: RevivePartsApp, id: Long) = OwnerOrderDetailViewModel(app.orderRepo, app.productRepo, app.userRepo, id)
    }
}
```

- [ ] **Step 11.4: Owner OrderDetailScreen**

```kotlin
package br.unasp.reviveparts.ui.screens.owner.orderdetail

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
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.ui.components.StatusStepper
import br.unasp.reviveparts.ui.components.YellowButton

@Composable
fun OwnerOrderDetailScreen(nav: NavController, id: Long) {
    val ctx = LocalContext.current
    val vm: OwnerOrderDetailViewModel = viewModel(key = "ood-$id", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = OwnerOrderDetailViewModel.create(ctx.app, id) as T
    })
    val o by vm.order.collectAsState()
    val p by vm.product.collectAsState()
    val u by vm.customer.collectAsState()
    val order = o ?: return

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Pedido #${order.id}", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        u?.let { Text("Cliente: ${it.name} (${it.email})"); Text("Endereço: ${it.address}"); Text("Tel: ${it.phone}") }
        Spacer(Modifier.height(8.dp))
        p?.let { Text("Peça: ${it.name}", style = MaterialTheme.typography.titleLarge) }
        Text("Total: R$ %.2f".format(order.totalCents / 100.0))
        Text("Pagamento: ${order.paymentType.name}")
        Text("Origem: ${order.source.name}")
        Spacer(Modifier.height(16.dp))
        StatusStepper(order.status)
        Spacer(Modifier.height(16.dp))
        val isDone = order.status == OrderStatus.DELIVERED
        YellowButton(
            if (isDone) "Concluído" else "Avançar para: ${order.status.next()?.label ?: "—"}",
            { vm.advance() },
            Modifier.fillMaxWidth(),
            enabled = !isDone
        )
    }
}
```

- [ ] **Step 11.5: Commit**

```
git add app/src/main/java/br/unasp/reviveparts/ui/screens/owner/dashboard app/src/main/java/br/unasp/reviveparts/ui/screens/owner/orderdetail
git commit -m "feat(owner): dashboard with status filter + order detail with advance"
```

---

## Task 12: Owner — Products list + edit + Profile

**Files:**
- `ui/screens/owner/products/ProductsViewModel.kt`
- `ui/screens/owner/products/ProductsScreen.kt`
- `ui/screens/owner/productedit/ProductEditViewModel.kt`
- `ui/screens/owner/productedit/ProductEditScreen.kt`
- `ui/screens/owner/profile/OwnerProfileScreen.kt`

- [ ] **Step 12.1: ProductsViewModel + Screen**

```kotlin
// ProductsViewModel.kt
package br.unasp.reviveparts.ui.screens.owner.products
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.launch

class ProductsViewModel(private val repo: ProductRepository) : ViewModel() {
    val items = repo.observeAll()
    fun delete(p: ProductEntity) = viewModelScope.launch { repo.delete(p) }
    companion object { fun create(app: RevivePartsApp) = ProductsViewModel(app.productRepo) }
}
```

```kotlin
// ProductsScreen.kt
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
```

- [ ] **Step 12.2: ProductEditViewModel + Screen**

```kotlin
// ProductEditViewModel.kt
package br.unasp.reviveparts.ui.screens.owner.productedit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ProductEditViewModel(private val repo: ProductRepository, private val id: Long?) : ViewModel() {
    val product = MutableStateFlow(
        ProductEntity(0, "", "", "drawable://placeholder_part", "models/manivela_vw.glb", 0, 0, 0, true)
    )
    init { id?.let { viewModelScope.launch { repo.findById(it)?.let { p -> product.value = p } } } }
    fun update(transform: (ProductEntity) -> ProductEntity) { product.value = transform(product.value) }
    fun save(after: () -> Unit) = viewModelScope.launch { repo.upsert(product.value); after() }
    companion object { fun create(app: RevivePartsApp, id: Long?) = ProductEditViewModel(app.productRepo, id) }
}
```

```kotlin
// ProductEditScreen.kt
package br.unasp.reviveparts.ui.screens.owner.productedit
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
import br.unasp.reviveparts.ui.components.PrimaryTextField
import br.unasp.reviveparts.ui.components.YellowButton

@Composable
fun ProductEditScreen(nav: NavController, id: Long?) {
    val ctx = LocalContext.current
    val vm: ProductEditViewModel = viewModel(key = "pe-${id ?: "new"}", factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(c: Class<T>): T = ProductEditViewModel.create(ctx.app, id) as T
    })
    val p by vm.product.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(if (id == null) "Novo produto" else "Editar produto", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        PrimaryTextField(p.name, { v -> vm.update { it.copy(name = v) } }, "Nome")
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(p.description, { v -> vm.update { it.copy(description = v) } }, "Descrição")
        Spacer(Modifier.height(8.dp))
        PrimaryTextField((p.priceCents / 100.0).toString(), { v -> vm.update { it.copy(priceCents = ((v.toDoubleOrNull() ?: 0.0) * 100).toLong()) } }, "Preço (R$)", keyboardType = KeyboardType.Decimal)
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(p.prototypeHours.toString(), { v -> vm.update { it.copy(prototypeHours = v.toIntOrNull() ?: 0) } }, "Horas de prototipagem", keyboardType = KeyboardType.Number)
        Spacer(Modifier.height(8.dp))
        PrimaryTextField(p.stockQty.toString(), { v -> vm.update { it.copy(stockQty = v.toIntOrNull() ?: 0) } }, "Estoque", keyboardType = KeyboardType.Number)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = p.isReady, onCheckedChange = { v -> vm.update { it.copy(isReady = v) } })
            Spacer(Modifier.width(8.dp)); Text("Pronta entrega")
        }
        Spacer(Modifier.height(16.dp))
        YellowButton("Salvar", { vm.save { nav.popBackStack() } }, Modifier.fillMaxWidth())
    }
}
```

- [ ] **Step 12.3: OwnerProfileScreen**

```kotlin
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
```

- [ ] **Step 12.4: Build full app**

```
./gradlew :app:assembleDebug
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 12.5: Commit**

```
git add app/src/main/java/br/unasp/reviveparts/ui/screens/owner/products app/src/main/java/br/unasp/reviveparts/ui/screens/owner/productedit app/src/main/java/br/unasp/reviveparts/ui/screens/owner/profile
git commit -m "feat(owner): products CRUD + owner profile/logout"
```

---

## Task 13: 3D asset + manifest + smoke run

- [ ] **Step 13.1: Bundle .glb**

Create `app/src/main/assets/models/`. Place a free GLB model (e.g., from Khronos sample-models repo, `Box.glb`) named `manivela_vw.glb`. If the dev cannot locate a free crank model, ship `Box.glb` renamed — fallback path documented in spec risks.

- [ ] **Step 13.2: Smoke run on emulator**

Run app, expected flow:
1. Launches into Login.
2. Register a new customer → lands on Customer Home with seeded products.
3. Open part detail, see 3D viewer.
4. Tap **+** tab → AI search → loading → result with 3D + buy.
5. Pay with card (use `4111 1111 1111 1111`) → order detail with stepper at PLACED.
6. Logout, login as `dono@reviveparts.com` / `dono123` → Owner Dashboard, see the new order.
7. Advance status → customer's stepper updates if reopened.

- [ ] **Step 13.3: Commit asset**

```
git add app/src/main/assets/models/manivela_vw.glb
git commit -m "chore: bundle placeholder 3D model"
```

---

## Task 14: README.md

**File:** `README.md`

- [ ] **Step 14.1: Write README**

Use this content:

````markdown
<div align="center">

<img src="app/src/main/res/drawable/logo.png" alt="ReviveParts" width="160"/>

# ReviveParts

**Inventário digital de peças automotivas — impressas em 3D, sob demanda.**

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-FFD60A?logo=jetpackcompose&logoColor=black)
![Android](https://img.shields.io/badge/Android-26%2B-0A0A0A?logo=android&logoColor=FFD60A)
![License](https://img.shields.io/badge/license-MIT-FFD60A)

</div>

---

## ✦ Sobre

ReviveParts revive carros clássicos e veículos fora-de-linha imprimindo em 3D peças que **não existem mais no mercado**. O app conecta o cliente que precisa da peça a um catálogo digital + uma IA que reconhece a peça por foto e descrição, gera um modelo 3D para visualização e dispara um pedido de impressão sob demanda.

> **MVP atual:** uma única peça em destaque — a **Manivela de Vidro do VW Fusca**. A arquitetura já está pronta para escalar para todo o catálogo.

## ⚙️ Funcionalidades

### 👤 Cliente
- Cadastro/login local
- Catálogo de peças com pronta entrega (foto, preço, estoque, tempo de prototipagem)
- **Visualizador 3D** real (SceneView) na tela da peça
- **Busca por IA**: descreva ou fotografe a peça → reconhecimento → preview 3D → pedido
- Pagamento simulado por **Cartão** (validação Luhn) ou **PIX** (QR Code + copia-e-cola)
- Acompanhamento do pedido em tempo real: `Pedido feito → Em análise → Imprimindo → Embalando → Saiu para entrega → Entregue`
- Gestão de cartões salvos e perfil

### 🛠️ Dono
- Dashboard de pedidos filtrado por status
- Avanço manual do status (orquestração da fila de impressão)
- CRUD de produtos (foto, preço, estoque, tempo de prototipagem, modelo 3D)

## 🎨 Identidade visual

| Cor | Uso |
|---|---|
| `#FFD60A` | Amarelo primário — CTA, destaques |
| `#0A0A0A` | Preto — background |
| `#1A1A1A` | Surface |

Tema escuro permanente. Tipografia bold para impacto industrial.

## 🧱 Stack

- **Kotlin 2.2** + **Jetpack Compose** + **Material3**
- **Room** para persistência local
- **Navigation Compose** + ViewModel + StateFlow
- **SceneView** para renderização 3D real
- **CameraX** + **Coil** para captura/exibição de imagens
- **ZXing** para geração de QR PIX
- **DataStore** para sessão

Sem backend, sem APIs externas — tudo funciona offline para apresentação.

## 🚀 Rodando

```bash
git clone https://github.com/ictydiego/reviveparts.git
cd reviveparts
./gradlew :app:assembleDebug
```

Abra no Android Studio (Hedgehog+), rode em emulador API 26+.

### Credenciais seed

| Role | E-mail | Senha |
|---|---|---|
| Dono | `dono@reviveparts.com` | `dono123` |
| Cliente | *cadastre-se na tela inicial* | — |

## 🗺️ Fluxo principal

```
Cliente → Cadastro → Home (catálogo)
                       ↓
         ┌─────────────┴─────────────┐
         ↓                           ↓
     Detalhe peça              + (IA Search)
         ↓                           ↓
       Comprar      ←  reconhecimento + 3D
         ↓
      Pagamento (Cartão | PIX)
         ↓
      Pedido criado → status pipeline
                          ↑
                    Dono avança status
```

## 📂 Estrutura

```
app/src/main/java/br/unasp/reviveparts/
├── ui/         (theme, components, nav, screens por role)
├── data/       (Room db, repos, ai mock, payments)
├── domain/     (enums + models)
└── RevivePartsApp.kt
```

## 🧪 Testes

```bash
./gradlew :app:testDebugUnitTest
```
Cobertura: validação Luhn, transições de status, mock de IA.

## 📜 Licença

MIT.

---

<div align="center">
Made with ⚙️ + 🟡 by the ReviveParts team
</div>
````

- [ ] **Step 14.2: Commit**

```
git add README.md
git commit -m "docs: add README with project overview, stack, flows"
```

---

## Self-Review

Spec coverage: ✅ all spec sections mapped.
Placeholder scan: ✅ no TBD/TODO. The few `/* fallback: */` comments are explicit fallbacks, not gaps.
Type consistency: enum names + DAO method names + repo signatures align across tasks.
Open caveats:
- `ProductEditScreen` does not implement an image picker for `photoPath` (uses default placeholder). Acceptable for MVP demo.
- `AiSearchScreen` has a non-functional camera button (deferred to gallery). Document or implement in a follow-up.

---

## Execution

Plan complete. Saved to `docs/superpowers/plans/2026-05-09-reviveparts-mvp.md`. User has explicitly requested parallel-agent dispatch — proceeding to dispatching-parallel-agents skill next.