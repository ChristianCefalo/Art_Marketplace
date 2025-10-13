Art Marketplace — Project README 

Goal: Build a mobile Art Marketplace app (Android, Java/XML) with Customer and Provider (Artist) roles, simulated payments, Firebase backend, and a real AI chatbot. Deliver live demos and a final report as specified.

1) Project Overview

Elevator pitch: A two-sided marketplace where artists upload artworks and buyers browse, chat with an AI assistant for guidance, add to cart, and complete a simulated checkout.

Primary users:

Customer: discovers art, chats with AI for recommendations, “purchases” via mock payment.

Provider (Artist): uploads artworks with images, sets price/quantity, chats with AI for pricing/titling advice.

Platforms: Android (emulator/physical), Firebase (Auth, Firestore, Storage, Functions/AI).

AI: Real chatbot using Firebase AI Logic extension or custom Cloud Function + Gemini

2) Features 


Register/Login (Email/Password; temporary Anonymous allowed during dev).

Role selection or role-assignment (Customer vs Provider).

Provider: upload artwork (image, title, price, qty) → Firestore + Storage.

Customer: browse feed, view details, add to cart.

Simulated payments (Luhn validation, approve/decline, order status updates).

AI Chatbot:

Two tailored bots via separate paths (Customer bot, Provider bot).

Simple chat UI with message history.

Basic analytics/logging (console + simple counters).

App settings: legal/terms, privacy notice, “About”.


Saved favorites, search/filter (price/style).

Provider dashboard (sales list, inventory decrement).

Customer ↔ Provider direct messaging.

Reviews/ratings.

Push notifications (order updates).

3) Architecture & Tech Stack

Client: Android (Java + XML)

Backend:

Firebase Auth (Email/Password),

Cloud Firestore (data),

Firebase Storage (images),

Cloud Functions (only if using custom AI or server logic),

Firebase Extensions (Firestore GenAI Chatbot).

AI Options (pick one):

A. Extension path: Install Firestore GenAI Chatbot twice (customer/provider).

B. Custom Function path: One callable Function that switches persona based on role.

Data model (proposed collections):

users/{uid} → { email, role, displayName, createdAt }

artworks/{artworkId} → { artistId, title, priceCents, qty, imageUrl, active, createdAt }

carts/{uid}/items/{itemId} → { artworkId, qty }

orders/{orderId} → { buyerId, items[], totalCents, status, createdAt }

payments/{paymentId} → { orderId, status, last4, createdAt }

customer_conversations/{uid}/messages/{msgId}

provider_conversations/{uid}/messages/{msgId}


5) Security & Rules (Dev → Harden)

Dev rules (simple, signed-in only) then tighten:

users: user can read/write own doc.

artworks: public read; only artistId can write/update.

carts: only owner can read/write.

orders/payments: only owner can read; writes restricted to order owner + server actions.

customer_conversations / provider_conversations: only owner uid.

Harden later

Field validation (price ≥ 0, qty ≥ 0).

Indexes for common queries (price, createdAt).

Minimal data exposure (no PII in public docs).

Rate limits in Functions (if using custom AI).

6) Payment Simulation

Why simulate: satisfies grading requirement without real gateway.

Flow: “Buy” → create orders (PENDING_PAYMENT) → mock card form → Luhn validate → write payments → set orders.status (PLACED or PENDING_PAYMENT).

Demo cards: Approved/Declined test numbers, documented in app.

7) AI Chatbot Design

Two tailored assistants

Customer bot: style/budget/size guidance, care/framing tips.

Provider bot: pricing, titles/descriptions, tags/SEO, policies/shipping.

Option A: Extension (no code backend)

Install two instances; set system prompts; watch two collection paths.

App writes {role:"user", content, createdAt}; reads back {role:"model"}.

Option B: Custom Cloud Function

One callable chatGemini(role, prompt); set persona by role.

App calls function; optional Firestore persistence.

Cost controls

Budgets & alerts (e.g., $5 cap), disable billing or delete project post-semester.

8) UI/UX Plan

Clean, accessible, no-nonsense screens:

Auth (email/password) → Role-aware Home (Customer/Provider).

Provider: Upload/Edit, My Artworks, Sales (basic).

Customer: Feed, Detail, Cart, Orders.

Chatbot: Enter via “Chatbot” button on each home screen; role auto-select.

Consistent typography, spacing; empty states and error toasts.

Image caching for artwork thumbnails.

9) Testing & QA

Unit tests (pure helpers: Luhn, formatters).

Manual tests: upload, list, detail, cart, payment sim, chats (both roles).

Firestore rules tests (try unauthorized writes).

Offline check (Firestore caching) for feed read.

Performance: image sizes, pagination (limit + load more).




