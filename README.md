# StoreFront-Android

A native Android shopping app built with **Kotlin**, **Jetpack Compose** and **Firebase**
Shoppers browse a catalogue, fill a cart and check out; staff manage stock and accounts from a built-in back office

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase&logoColor=black)
![minSdk](https://img.shields.io/badge/minSdk-29-3DDC84?logo=android&logoColor=white)

## Features

| Area | What you can do |
| --- | --- |
| **Catalogue** | Search by name, category or brand; filter from the side drawer; swipeable photo gallery; discount and low-stock badges |
| **Cart** | Add, change quantity (capped at available stock), remove, clear; live total and item-count badge |
| **Checkout** | Delivery address and payment method; stock is reserved and the order saved in one atomic transaction |
| **Orders** | Order history with status chips and a detail view showing the prices paid |
| **Account** | Email/password sign-up and sign-in, session restored on relaunch, editable profile |
| **Back office** | Admins list customers, change roles, restock products or zero them out |

## Screens

### Shopping

| Catalogue | Product details | Cart | Checkout |
| :---: | :---: | :---: | :---: |
| <img src="docs/screenshots/catalog.png" width="190"> | <img src="docs/screenshots/product-details.png" width="190"> | <img src="docs/screenshots/cart.png" width="190"> | <img src="docs/screenshots/checkout.png" width="190"> |

### Account and orders

| Sign in | Sign up | Orders | Order details | Profile |
| :---: | :---: | :---: | :---: | :---: |
| <img src="docs/screenshots/sign-in.png" width="150"> | <img src="docs/screenshots/sign-up.png" width="150"> | <img src="docs/screenshots/orders.png" width="150"> | <img src="docs/screenshots/order-details.png" width="150"> | <img src="docs/screenshots/profile.png" width="150"> |

### Back office (admins only)

| Admin hub | Inventory | Customers |
| :---: | :---: | :---: |
| <img src="docs/screenshots/admin.png" width="190"> | <img src="docs/screenshots/stock-management.png" width="190"> | <img src="docs/screenshots/user-management.png" width="190"> |

## Architecture

The app follows a unidirectional **MVI-style** flow. Each feature owns a *contract* (`UiState` + `Action`), a `ViewModel`
that reduces actions into new state, and stateless Compose screens that render it. Repositories are the only code that talks to Firebase.

```mermaid
flowchart LR
    subgraph UI["Compose UI (feature/*)"]
        S["Screens / Composables"]
        A["User Actions"]
        S --> A
    end

    subgraph VMs["ViewModels (Hilt-injected)"]
        VM["Catalog / Cart / Orders / Account ViewModels"]
        State["UiState (StateFlow)"]
    end

    subgraph Data["Data Layer"]
        R["Catalog / Cart / Order / Account Repositories"]
    end

    subgraph Cloud["Firebase Services"]
        FS[("Cloud Firestore")]
        AU["Firebase Authentication"]
    end

    A -->|"Events / callbacks"| VM
    VM -->|"Updates"| State
    State -->|"CollectAsStateWithLifecycle"| S

    VM -->|"Coroutines / suspend calls"| R
    R --> FS
    R --> AU

    classDef ui fill:#e8f2ff,stroke:#3973ac,color:#222
    classDef vm fill:#e9f7ef,stroke:#39845a,color:#222
    classDef data fill:#fff3d6,stroke:#b38b25,color:#222
    classDef cloud fill:#f1e8ff,stroke:#8055aa,color:#222

    class S,A ui
    class VM,State vm
    class R data
    class FS,AU cloud
```

Dependencies point inward: `feature` → `data` → `domain`. The `domain` package is plain Kotlin (models, pricing rules,
order construction) so it is unit-testable on the JVM

### Navigation

```mermaid
flowchart TD
    Home(["Shop / Home"]) --> Detail["Product Details"]
    Home --> Cart["Shopping Cart"]

    Cart --> AuthCheck{"Signed in?"}
    AuthCheck -->|Yes| Checkout["Checkout"]
    AuthCheck -->|No| SignIn["Sign In"]

    SignIn -->|Success| Checkout
    Checkout -->|Order placed| Orders["My Orders"]
    Orders --> OrderDetail["Order Details"]

    SignIn <--> SignUp["Sign Up"]
    SignIn -->|Success| Profile["Profile"]
    SignUp -->|Account created| SignIn

    Profile --> Edit["Edit Profile"]

    Profile -.->|Role = ADMIN| Admin["Admin Dashboard"]
    Admin --> Users["Manage Customers"]
    Users --> CustomerProfile["Customer Profile"]
    Admin --> Stock["Inventory Management"]

    classDef admin fill:#ffe5e5,stroke:#cc3333,color:#222
    class Admin,Users,CustomerProfile,Stock admin
```

Routes that need a session (checkout, orders, profile) or the `ADMIN` role (back office) are wrapped in a guard that redirects to sign-in

## Data model

Firestore collections and their relationships:

```mermaid
erDiagram
    USERS ||--o{ ORDERS : places
    ORDERS ||--|{ ORDER_LINES : contains
    PRODUCTS ||--o{ ORDER_LINES : references

    USERS {
        string userId PK "Firebase Auth UID"
        string fullName
        string email
        string phoneNumber
        json address
        bigint createdAt
        string role "ADMIN CUSTOMER DELIVERY"
    }

    PRODUCTS {
        string id PK
        string title
        float price
        float discountPercentage
        float rating
        int stock
        datetime restockDate
        string brand
        string category
        string thumbnail
        json images
    }

    ORDERS {
        string orderId PK
        string userId FK
        datetime orderDate
        float totalAmount
        float shippingFee
        json deliveryAddress
        string paymentMethod
        string status "PENDING CONFIRMED SHIPPED DELIVERED CANCELLED"
    }

    ORDER_LINES {
        string orderLineId PK
        string orderId FK
        string productId FK
        int quantity
        float unitPrice "Price frozen at purchase"
        float discountApplied "Discount frozen at purchase"
        json productSnapshot "Product copy at purchase time"
    }
```

Order lines embed a copy of the product with the price and discount **as paid**, so later catalogue edits never rewrite history

## Key flows

### Checkout

Stock is checked and decremented inside a Firestore transaction together with the order write, so two shoppers can't buy the last unit

```mermaid
sequenceDiagram
    actor Shopper
    participant UI as CheckoutScreen
    participant VM as OrdersViewModel
    participant Repo as OrderRepository
    participant DB as Firestore

    Shopper->>UI: Place order
    UI->>VM: Place(order)
    VM->>Repo: place(order)
    Repo->>DB: begin transaction
    Repo->>DB: read stock of every product
    alt enough stock for all lines
        Repo->>DB: decrement stock, write order
        DB-->>Repo: committed
        Repo-->>VM: success
        VM-->>UI: placedOrderId
        UI-->>Shopper: clear cart, show Orders
    else a product ran out
        Repo-->>VM: InsufficientStockException
        VM-->>UI: placeError
        UI-->>Shopper: message, cart kept
    end
```

### Sign-in and session

```mermaid
sequenceDiagram
    participant App as App launch
    participant VM as AccountViewModel
    participant Auth as Firebase Auth
    participant DB as Firestore

    App->>VM: init
    VM->>Auth: restored session?
    opt session exists
        VM->>DB: load users/{uid}
        DB-->>VM: profile (with role)
    end
    Note over VM: isSignedIn and isAdmin are derived from the profile
```

## Tech stack

| Concern | Choice |
| --- | --- |
| Language / UI | Kotlin 2.0, Jetpack Compose, Material 3 |
| Navigation | Navigation Compose with typed `Destination` objects |
| DI | Hilt (KSP) |
| Backend | Firebase Authentication and Cloud Firestore |
| Images | Coil |
| Async | Coroutines + `StateFlow` |
| Tests | JUnit 4 (pure-JVM unit tests) |

Dependency versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml)

## Project structure

```
app/src/main/java/com/storefront/app/
├── StorefrontApplication.kt      Hilt entry point
├── MainActivity.kt               hosts the four activity-scoped ViewModels
├── core/
│   ├── firebase/                 Hilt module, Task.awaitResult()
│   ├── format/                   price, rating and date formatting
│   ├── theme/                    colours and Material 3 theme
│   ├── ui/                       shared composables (loading/empty/error, price tag, form fields…)
│   └── util/                     attempt{}, validation
├── domain/model/                 Product, CartLine, Order, User… plus pricing rules and OrderFactory
├── data/                         CatalogRepository, OrderRepository, AccountRepository
├── navigation/                   Destination, NavHost, scaffold (drawer, top and bottom bars)
└── feature/
    ├── catalog/                  list, details, gallery, inventory screen
    ├── cart/
    ├── orders/                   history, details, checkout
    ├── account/                  sign-in/up, profile, edit, customer directory
    └── admin/                    back-office hub

tools/seed/                       Node scripts: load demo products, grant roles
firestore.rules                   recommended security rules
docs/                             screenshots
```

## Getting started

**Requirements:** Android Studio (recent stable), JDK 17, a Firebase project

1. **Create a Firebase project** and enable **Authentication → Email/Password** and **Cloud Firestore**
2. **Register an Android app** in Firebase with the package name `com.storefront.app`
   (or change `applicationId` and `namespace` in `app/build.gradle.kts` to your own and register that instead)
3. **Download `google-services.json`** and place it in `app/`. It is git-ignored
4. **Deploy the rules** from [`firestore.rules`](firestore.rules) (Firebase console → Firestore → Rules)
5. **Load demo data** (optional but recommended):
   ```bash
   cd tools/seed
   npm install
   export GOOGLE_APPLICATION_CREDENTIALS=/absolute/path/to/service-account.json   # keep this file outside the repo
   npm run seed
   ```
6. **Run** the `app` configuration on an emulator or device (API 29+)
7. **Create your first admin:** sign up in the app, then
   ```bash
   npm run set-role -- you@example.com ADMIN
   ```
   Sign out and back in to see the Admin tab
