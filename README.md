# Art Marketplace (Android)

## Project Setup

1. **Clone & Open**
   - Clone this repository and open the `Art_Marketplace` directory in Android Studio (Giraffe or newer).

2. **Android Studio Configuration**


3. **Firebase Configuration**
   - Create a Firebase project in the [Firebase console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.example.artmarketplace` and download the generated `google-services.json` file.
   - Place the `google-services.json` file inside `app/`.
   - Enable **Authentication** (Email/Password and Anonymous) and **Cloud Firestore** in your Firebase project.

4. **Sync & Build**
   - In Android Studio, trigger **Sync Project with Gradle Files** to download dependencies.
   - Build and run the project to verify the initial scaffold launches successfully.

### Build Troubleshooting


- Proxy-restricted networks may block Gradle from fetching dependencies; configure Android Studio's proxy settings under **Appearance & Behavior ▸ System Settings ▸ HTTP Proxy** if needed.

## Role Selection Navigation

- The launcher activity is now `RoleSelectActivity`, presenting customer and provider entry points.
- Tapping **I'm a Customer** opens `CustomerHomeActivity`; tapping **I'm a Provider** opens `ProviderHomeActivity`.
- Both destination screens currently show placeholder content until their respective feature work is implemented.

## Data Layer

- Added model classes for `ArtItem`, `Order`, and `UserProfile` to match the Firestore schema described in the project brief.
- Implemented `FirestoreRepo` to encapsulate CRUD operations for art listings and orders, exposing Task-based helpers for Activities to observe.
- Introduced `AuthRepo` as a thin wrapper around `FirebaseAuth` with helpers for anonymous and email/password sign-in flows.

## Customer Art Browsing

- `CustomerHomeActivity` now shows a Firestore-backed list of art using `RecyclerView` and a lightweight `ArtAdapter`.
- Tapping an item opens `ArtDetailActivity`, which surfaces the listing title, price, and description alongside an **Add to Cart** action leading into the checkout flow.
- Loading progress, empty states, and failure toasts help users understand what's happening while Firestore requests complete.

## Mock Checkout Flow

- `CartActivity`, `CheckoutActivity`, and `ReceiptActivity` complete the customer purchase path for a single art item.
- Checkout persists a Firestore order with `PENDING` status, then simulates payment by transitioning through `PAID_SIM` to `CONFIRMED`.
- The receipt screen summarizes the order ID, total paid, and final status while offering a quick return to the art feed.

## Provider Listing Management

- `ProviderHomeActivity` now surfaces quick actions plus a RecyclerView of the signed-in provider's listings with edit affordances.
- `CreateListingActivity` captures title, price, description, and optional image URL before persisting a Firestore `art` document.
- `EditListingActivity` supports updating or deleting existing listings while keeping provider ownership metadata intact.
- `ProviderOrdersActivity` lists incoming orders for the provider, highlighting status and totals for easy fulfillment tracking.

## AI Chat Assistant

- Added `ChatActivity` with a lightweight conversational UI that posts to the `/api/chat-gemini` Cloud Function via Retrofit.
- Configure the Cloud Function host by updating the `chat_base_url` string resource (must include the trailing slash).
- Customers can launch the chat directly from the art feed using the new **Chat with AI** button.

## Developer Utilities & Sample Data

- `SeedDataUtil` seeds a set of sample `art` documents on debug builds when the collection is empty, accelerating local testing.
- `Validators` and `Formatters` centralize price parsing, URL checks, and currency rendering for both customer and provider flows.
- Toast feedback indicates when sample data is being inserted to avoid confusion during first-run testing.

## Next Steps

- Harden Firestore security rules before launch and gate the seed utility behind a developer toggle.
- Expand the checkout flow with real payment provider integration when available.
- Add instrumentation/UI tests to cover the chat handoff and order state transitions.

