# ReviveParts — MVP Design Spec

**Date:** 2026-05-09
**Status:** Approved (brainstorming)
**Repo:** https://github.com/ictydiego/reviveparts.git

## Summary

Android app (Kotlin + Jetpack Compose) for a 3D-printed auto-parts startup. Single hero part for MVP: VW crank handle (manivela). Two roles (Customer, Owner) sharing one app. Customer browses ready-printed parts, or uses an AI search flow (text + photo) that recognizes the part and starts a print order. Owner manages incoming orders through a fixed status pipeline and maintains the product catalog. All persistence local (Room). AI and payments mocked. 3D viewer real (SceneView).

## Goals

- Demo-ready MVP showing complete buyer + seller loop
- Yellow + black themed UI, logo from `res/drawable/logo.png`
- Local-only data (no backend); seedable via initial DB callback
- Realistic 3D viewer experience for the recognized part

## Non-Goals

- Real AI vision (mocked)
- Real payment gateway (simulated cards + simulated PIX)
- Multi-product AI recognition (always returns manivela VW)
- Cloud sync, push notifications, multi-device

## Tech Stack

| Concern | Choice |
|---|---|
| UI | Jetpack Compose + Material3 |
| Min SDK | 26 (existing) |
| Navigation | androidx.navigation:navigation-compose |
| DB | Room (KSP) + Flow |
| State | ViewModel + StateFlow |
| Images | Coil |
| Camera | CameraX |
| 3D Viewer | `io.github.sceneview:sceneview` |
| QR Code | `com.google.zxing:core` + custom Compose canvas |
| Session | DataStore Preferences |
| DI | Manual (singletons via Application class) — no Hilt for MVP |

## Theme

```
Primary:        #FFD60A  (yellow)
OnPrimary:      #000000
Background:     #0A0A0A  (near-black)
Surface:        #1A1A1A
SurfaceVariant: #262626
OnSurface:      #FAFAFA
Outline:        #3A3A3A
Error:          #FF5252
```

Dark theme only for MVP. Typography: default Material3 with Poppins (or system sans) bold for display.

## Architecture

```
ui/
  theme/        Color, Type, Theme.kt
  components/   YellowButton, PartCard, StatusStepper, BottomBar, etc
  screens/
    auth/       LoginScreen, RegisterScreen
    customer/   HomeScreen, PartDetailScreen, AiSearchScreen,
                CartScreen, PaymentScreen, OrdersScreen,
                OrderDetailScreen, ProfileScreen
    owner/      OwnerDashboardScreen, OwnerOrderDetailScreen,
                ProductsScreen, ProductEditScreen, OwnerProfileScreen
  nav/          AppNavHost, Routes, BottomBarHost
data/
  db/           AppDatabase, daos, entities
  repo/         UserRepository, ProductRepository, OrderRepository,
                CardRepository, SessionRepository
  ai/           FakeAiService
  payments/     PaymentSimulator, PixGenerator
domain/
  model/        Domain models (mirrors of entities w/ enum status)
RevivePartsApp.kt (Application — wires DB + repos)
```

One-direction flow: Screen → ViewModel → Repository → DAO. Screens collect StateFlow.

## Data Model (Room)

```kotlin
@Entity User(id PK auto, name, email UNIQUE, password, phone, cpf, address, role: "CUSTOMER"|"OWNER")
@Entity Product(id PK auto, name, description, photoPath, model3dAsset, priceCents: Long, prototypeHours: Int, stockQty: Int, isReady: Boolean)
@Entity Card(id PK auto, userId FK, holderName, last4, brand, expiry, isDefault)
@Entity Order(id PK auto, userId FK, productId FK, status: enum, paymentType: "CARD"|"PIX", totalCents, source: "CATALOG"|"AI", createdAt)
@Entity OrderEvent(id PK auto, orderId FK, status, timestampMs)
```

Status enum (ordered): `PLACED → IN_REVIEW → PRINTING → PACKING → SHIPPED → DELIVERED`.

Seed on first run: 1 owner user (`dono@reviveparts.com` / `dono123`), 4-6 ready products including the manivela VW.

## Auth & Session

- Login screen: email + password.
- Register screen: full customer profile.
- On successful login, store `userId` + `role` in DataStore.
- Email match `dono@reviveparts.com` ⇒ role OWNER. Else CUSTOMER.
- App start reads session, routes to Login or appropriate role home.

## Navigation

Bottom bar visible inside role shell. Items differ per role.

**Customer bottom bar:** Home · Pedidos · **+** (center FAB) · Perfil
**Owner bottom bar:** Pedidos · Produtos · Perfil

Routes:
```
auth/login, auth/register
customer/home, customer/part/{id}, customer/ai, customer/cart/{productId}/{source},
customer/payment/{orderId}, customer/orders, customer/order/{id}, customer/profile
owner/dashboard, owner/order/{id}, owner/products, owner/product/edit?id=, owner/profile
```

## Customer Flow Detail

### Home
Grid of `Product` rows where `isReady=true`. Card shows photo, name, price, stock badge ("em estoque" / "X h prototipagem").

### Part Detail
Hero 3D viewer (`SceneView`), specs, "Comprar" button → CartScreen.

### AI Search (the **+** flow)
1. Screen: text input ("descreva a peça"), button row [📷 câmera] [🖼️ galeria]
2. After submit: full-screen loading w/ animated dots (2.5 s `delay`)
3. `FakeAiService.recognize(text, imagePath)` → `RecognitionResult(productId=manivelaVwId, confidence=0.92f)`
4. Result screen: 3D viewer + "É essa peça? Sim / Não"
5. "Sim" → CartScreen with `source=AI`

### Cart / Payment
- Cart: product summary, total, "Continuar"
- Payment: tabs [Cartão] [PIX]
  - **Cartão**: list saved cards + "+ novo cartão" form (number, holder, expiry, CVV, Luhn validation). Selecting one + "Pagar" → 1.5 s simulated processing → success.
  - **PIX**: shows QR code (zxing), copia-e-cola string, "Já paguei" button → success.
- On success: insert Order(status=PLACED) + OrderEvent. Navigate to OrderDetail.

### Orders
List of user's orders sorted by createdAt desc. Tap → OrderDetail.

### OrderDetail (customer)
- Product summary
- StatusStepper component: 6 dots horizontal, current highlighted yellow, past = checked, future = grey
- Last update timestamp

### Profile
Editable: name, phone, address. Cards section (list, set default, delete). Logout.

## Owner Flow Detail

### Dashboard
Tabs by status (or filter chips). Each tab lists orders with customer name + product + age. Default tab = `PLACED`.

### Owner OrderDetail
Customer info, product, payment type, current status, "Avançar status" button (disabled at DELIVERED). Tap → insert OrderEvent + update Order.status to next enum.

### Products
List all. "+" FAB → ProductEdit. Each row has edit + delete.

### ProductEdit
Form: name, description, photo (gallery picker), model3dAsset (dropdown of bundled .glb assets), priceCents, prototypeHours, stockQty, isReady toggle.

## 3D Asset Strategy

Bundle `app/src/main/assets/models/manivela_vw.glb` (placeholder a free crank model or a basic primitive if needed). SceneView loads from assets path. Same asset reused for all AI-recognized results in MVP.

## Mock AI Service

```kotlin
class FakeAiService {
  suspend fun recognize(text: String?, imagePath: String?): RecognitionResult {
    delay(2500)
    return RecognitionResult(
      productId = SeedIds.MANIVELA_VW,
      confidence = 0.92f,
      label = "Manivela de vidro VW"
    )
  }
}
```

## Mock Payment

- Card: Luhn check on number; otherwise accept all. Simulated 1.5 s delay.
- PIX: generate fake BR Code string `"00020126...REVIVEPARTS{orderId}..."`, render QR via zxing into `BitMatrix` → Compose canvas. Copy-to-clipboard button.

## Error Handling

- Form validation inline (red helper text)
- Repository methods return `Result<T>` only where failure is meaningful (login, register-with-duplicate-email, payment)
- DB-level exceptions logged + show snackbar "Erro inesperado"

## Testing (MVP scope)

- Unit: Luhn validator, status transition logic, FakeAiService returns expected ID
- Skip Compose UI tests for MVP speed

## Out of Scope (future)

- Real AI vision API
- Real payment gateway
- Multi-part recognition
- Notifications, chat, reviews
- Multi-language

## File-Level Plan (high level)

| Module | Files (approx) |
|---|---|
| Theme | 3 |
| DB (entities + DAOs + DB class) | 10 |
| Repos | 5 |
| ViewModels | ~12 |
| Screens | ~15 |
| Components | ~10 |
| Nav | 2 |
| Mock services | 3 |

## Open Risks

1. **SceneView API drift** — pin a known version; have a fallback to a static image if init fails.
2. **`.glb` asset availability** — if no free model, ship a coloured cylinder primitive labelled "Manivela VW (preview)".
3. **CameraX permission UX** — request on first AI flow, fall back to gallery if denied.