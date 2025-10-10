# 🎨 Art Marketplace — Project Plan & Development Outline

## 📘 Overview
The **Art Marketplace** app connects artists and buyers through a unified mobile platform.  
It will include both **Customer** and **Provider (Artist)** sides within the same Android app.

Built with **Java**, **Android Studio**, and **Firebase**, this app will allow:
- Artists to upload and manage artwork.
- Customers to browse, favorite, and purchase art.
- A chatbot assistant to help users with questions or recommendations.

---

## 🧩  Project Setup & Firebase Integration

### 🔹 Objectives
- Initialize Android Studio project (`Empty Views Activity` template).
- Connect project to Firebase (Auth, Firestore, Functions).
- Create GitHub repository and ensure all members can push/pull.
- Establish consistent folder structure for development.

### 🔹 Deliverables
- Functional Gradle build.
- Firebase connected with `google-services.json`.
- Shared GitHub repo with proper branching workflow (`main`, `feature/*`).

### 🔹 Expected Files
/ArtMarketplace/
│
├── app/
│ ├── src/main/java/com/example/artmarketplace/
│ ├── src/main/res/layout/
│ ├── build.gradle.kts
│ └── google-services.json



---

## 🧩 Authentication & User Flow

### 🔹 Purpose
Enable users to **sign up, log in, and select their role** (Customer or Provider).

### 🔹 Activities & Layouts
| Activity | XML Layout | Description |
|-----------|-------------|--------------|
| `SplashActivity` | `activity_splash.xml` | Shows logo, loads Firebase, navigates to login or home. |
| `LoginActivity` | `activity_login.xml` | Allows users to log in via email/password. |
| `RegisterActivity` | `activity_register.xml` | New user registration; includes role selection (Artist/Customer). |
| `RoleSelectionActivity` | `activity_role_selection.xml` | Saves chosen user type to Firestore for later logic. |

### 🔹 Data
Firestore collections:
users/
└── userId/
├── name
├── email
├── role ("customer" or "provider")



---

## 🧩  Customer Side (Browsing & Buying)

### 🔹 Purpose
Allow customers to browse artwork, view details, add to cart, and purchase.

### 🔹 Activities & Layouts
| Activity | XML Layout | Description |
|-----------|-------------|--------------|
| `CustomerHomeActivity` | `activity_customer_home.xml` | Displays artwork feed with images and basic info. |
| `ArtDetailActivity` | `activity_art_detail.xml` | Shows selected art details (artist, price, description). |
| `CartActivity` | `activity_cart.xml` | Lists items the user intends to purchase. |
| `CheckoutActivity` | `activity_checkout.xml` | Simulates payment process and order confirmation. |

### 🔹 Data
Firestore collections:
artworks/
└── artId/
├── title
├── description
├── price
├── imageUrl
├── providerId

yaml
Copy code

---

## 🧩 Provider Side (Uploading & Managing Art)

### 🔹 Purpose
Enable artists to add, edit, and manage their artwork listings.

### 🔹 Activities & Layouts
| Activity | XML Layout | Description |
|-----------|-------------|--------------|
| `ProviderDashboardActivity` | `activity_provider_dashboard.xml` | Displays summary of provider’s listings. |
| `UploadArtActivity` | `activity_upload_art.xml` | Form for adding new artwork with title, description, price, and image upload. |
| `ManageArtActivity` | `activity_manage_art.xml` | Allows artist to update or remove existing listings. |

### 🔹 Data Flow
- Images uploaded to Firebase Storage.
- Metadata (title, price, description) saved to Firestore under `artworks/`.

---

## 🧩 AI Chatbot Integration

### 🔹 Purpose
Integrate a chatbot for guidance, help, and art recommendations.

### 🔹 Backend
- Implement Firebase Cloud Function (`functions/index.js`).
- Connect it to **Gemini API** (or OpenAI) using secure key storage in Firebase config.
- Expose endpoint:  
  `https://us-central1-[project-id].cloudfunctions.net/api/chat-gemini`

### 🔹 Frontend (Android)
| Component | XML | Description |
|------------|------|--------------|
| `ChatActivity` | `activity_chat.xml` | UI for sending/receiving messages between user and chatbot. |
| `chat_item_left.xml` / `chat_item_right.xml` | | Message bubbles for bot and user. |

---

## 🧩  UI Enhancements & Navigation

### 🔹 Purpose
Improve UX and ensure smooth navigation across app components.

### 🔹 Shared Components
| Component | XML | Description |
|------------|------|-------------|
| `BottomNavigationView` | `nav_customer.xml`, `nav_provider.xml` | Tabs for switching between sections. |
| `AppToolbar` | Included layout in all screens for branding and logout. |
| `RecyclerView Adapters` | `ArtAdapter.java`, `CartAdapter.java` | Used for displaying lists of artworks or cart items. |

---

## 🧩 Testing & Finalization

### 🔹 Purpose
Ensure all features work correctly across both user types.

### 🔹 Tasks
- Validate Firebase Auth and Firestore read/write operations.
- Test all navigation paths and UI transitions.
- Verify chat functionality with the backend.
- Final polish (icons, color theme, strings.xml cleanup).

---

## 🧩 Documentation & Delivery

### 🔹 Deliverables
- Final GitHub repository with:
  - Complete source code  
  - Working README  
  - Screenshots or short demo GIF  
- Firebase project with Functions and Firestore deployed.
- Presentation slides outlining features and team contributions.

---

## 🧠 Summary of Planned Activities

| Area | Key Activities |
|------|----------------|
| **Core UI** | Splash, Login, Register, Customer Home, Provider Dashboard |
| **Database** | Firestore for user/art data, Storage for images |
| **Backend** | Firebase Cloud Functions for chatbot |
| **AI** | Gemini-powered chatbot for help & recommendations |
| **Testing** | Unit + manual validation for both user roles |

---

## 🧭 Development Notes
- Follow consistent naming: `ActivityNameActivity` and `activity_name.xml`
- Use meaningful commit messages and create feature branches for each module.
- All new XML layouts must follow a responsive design approach.
- Use Firebase test mode for early development.
