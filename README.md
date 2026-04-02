# MufasaPay SMS Gateway

An Android SMS gateway that intercepts incoming SMS messages from whitelisted financial providers, filters for money-received transactions, and forwards them to a configurable webhook endpoint in real-time.

Built for the Ethiopian financial ecosystem — supports CBE Birr, Telebirr, M-PESA, HelloCash, and more out of the box.

## How It Works

```
SMS Received → Sender Enabled? → Matches Any Enabled Template? → Save to DB → Forward to Webhook
                   ↓ No                    ↓ No / No Template                    ↓ Failed
                Discard                         Discard                   Retry via WorkManager
```

1. A `BroadcastReceiver` (priority 999) intercepts incoming SMS — non-invasively, the SMS still reaches the default SMS app
2. A foreground service validates the sender against a whitelist
3. The message body is checked against the enabled templates configured for that sender
4. Matching messages are saved to a local Room database
5. A webhook payload is built and sent to your configured endpoint via Retrofit
6. On failure, WorkManager schedules retries with exponential backoff
7. A daily cleanup worker removes messages older than 30 days

## Features

- **Sender Whitelisting** — only process SMS from configured senders
- **Multi-Template Matching** — attach multiple message templates to each sender and match against any enabled template
- **Webhook Forwarding** — POST/PUT/PATCH to any endpoint with Bearer, Basic, API Key, or no auth
- **Retry Logic** — automatic retries with exponential backoff via WorkManager
- **Delivery Tracking** — full audit trail with status (PENDING, SUCCESS, FAILED, RETRYING), HTTP response codes, and duration
- **Real-time Dashboard** — reactive stats powered by Room Flows
- **Connection Testing** — test your webhook endpoint before going live
- **Ethiopian Providers** — 10 pre-configured financial providers for quick onboarding

## Architecture

Clean Architecture with MVVM presentation layer:

```
┌─────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose + ViewModels)    │
├─────────────────────────────────────────────┤
│  Domain Layer (Use Cases + Models)          │
├─────────────────────────────────────────────┤
│  Data Layer (Repositories + DAOs + API)     │
└─────────────────────────────────────────────┘
```

### Project Structure

```
com.itechsolution.mufasapay/
├── di/                     # Koin dependency injection modules
├── domain/
│   ├── model/              # Business models (SmsMessage, Sender, WebhookConfig, DeliveryLog)
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Use cases organized by feature
│       ├── dashboard/      #   GetDeliveryStatsUseCase
│       ├── history/        #   GetSmsHistoryUseCase, RetryFailedDeliveryUseCase
│       ├── sender/         #   AddSender, RemoveSender, ToggleStatus, GetAll
│       └── webhook/        #   SaveConfig, GetConfig, TestConnection
├── data/
│   ├── local/              # Room database, DAOs, entities
│   ├── remote/             # Retrofit API service, DTOs, interceptors
│   ├── repository/         # Repository implementations
│   └── worker/             # WorkManager (SmsForwardWorker, SmsCleanupWorker)
├── ui/
│   ├── screens/            # Compose screens (dashboard, senders, webhook, history)
│   ├── components/         # Reusable components (StatCard, SenderListItem, etc.)
│   ├── navigation/         # NavGraph, Screen routes
│   ├── state/              # UiState sealed class
│   └── theme/              # Material 3 theme
├── service/
│   ├── SmsBroadcastReceiver.kt   # SMS interception
│   └── SmsReceiverService.kt     # Foreground processing service
└── util/
    ├── Constants.kt
    ├── Result.kt                  # Functional Result<T> wrapper
    ├── SmsPatternExtractor.kt     # Pattern extraction from sample messages
    └── DateTimeUtils.kt
```

## Tech Stack

| Component | Library | Version |
|---|---|---|
| Language | Kotlin | 2.0.21 |
| UI | Jetpack Compose (Material 3) | BOM 2024.09.00 |
| Database | Room | 2.6.1 |
| Networking | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| JSON | Moshi | 1.15.1 |
| DI | Koin | 4.0.1 |
| Background Work | WorkManager | 2.10.0 |
| Async | Kotlin Coroutines + Flow | 1.8.1 |
| Navigation | Navigation Compose | 2.8.5 |
| Logging | Timber | 5.0.1 |
| Code Generation | KSP | 2.0.21-1.0.28 |

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- Android SDK 36

### Build & Run

```bash
# Clone the repository
git clone https://github.com/your-username/MufasaPay.git
cd MufasaPay

# Build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Required Permissions

The app requests these permissions at runtime:

| Permission | Purpose |
|---|---|
| `RECEIVE_SMS` | Intercept incoming SMS messages |
| `READ_SMS` | Read SMS content for processing |
| `INTERNET` | Forward messages to webhook endpoint |
| `POST_NOTIFICATIONS` | Show foreground service and delivery notifications |
| `FOREGROUND_SERVICE` | Background SMS processing |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Prompt user to exempt the app from aggressive battery restrictions |

## Webhook Configuration

### Payload Format

Every forwarded SMS is sent as JSON:

```json
{
  "event": "sms.received",
  "timestamp": 1708000000000,
  "data": {
    "sender": "CBE BIRR",
    "message": "You have received ETB 1,500.00 from ABEBE KEBEDE",
    "receivedAt": 1707999999000
  },
  "metadata": {
    "deviceId": "a1b2c3d4e5f6g7h8",
    "appVersion": "1.0",
    "sdkVersion": 36,
    "forwardedAt": 1708000000010
  }
}
```

### Authentication Options

| Type | Header Sent |
|---|---|
| Bearer Token | `Authorization: Bearer <token>` |
| Basic Auth | `Authorization: Basic <base64(user:pass)>` |
| API Key | Custom header with your key |
| None | No auth header |

### Retry Behavior

When a webhook call fails:
- Retries are scheduled via WorkManager with exponential backoff
- Delay formula: `baseDelay * 2^attemptNumber`
- Default: 3 retries, 5-second base delay
- Both values are configurable per webhook

## Database Schema

Five Room tables with reactive Flow queries:

| Table | Purpose |
|---|---|
| `sms_messages` | Stored SMS with forwarding status |
| `senders` | Whitelisted senders |
| `sender_templates` | Multiple templates per sender |
| `webhook_config` | Singleton webhook configuration |
| `delivery_logs` | Delivery attempt audit trail (FK to sms_messages) |

Indexes on `timestamp`, `sender`, `isForwarded`, `status`, and the SMS uniqueness key support query performance and duplicate suppression. Sender templates cascade-delete with their parent sender.

## Pattern Matching

The app filters SMS using sender-specific templates:

1. **Per-sender templates** — Each sender can have multiple labeled templates, and an SMS is accepted when it matches any enabled template
2. **Placeholder support** — Templates support placeholders such as `{name}`, `{amount}`, `{account}`, `{transaction}`, `{datetime}`, `{balance}`, and `{ignore}`
3. **Sample-based bootstrapping** — Users can paste a sample SMS and auto-generate a starter template, then refine it manually

The `SmsPatternExtractor` utility handles both extraction and matching.

## Contributing

### Development Setup

1. Fork and clone the repo
2. Open in Android Studio
3. Sync Gradle — all dependencies resolve from Maven Central and Google's Maven
4. Run on a physical device (SMS reception requires a real SIM)

### Architecture Guidelines

- **Domain layer has no Android dependencies** — pure Kotlin models, repository interfaces, and use cases
- **Data flows upward** — DAOs return `Flow`, repositories expose them, ViewModels collect via `stateIn`
- **Error handling uses `Result<T>`** — never throw from repositories or use cases; wrap in `Result.success()` or `Result.error()`
- **DI is in `/di`** — all Koin modules are there; add new dependencies in the appropriate module
- **Use cases are single-responsibility** — one public `operator fun invoke()` per use case
- **Compose screens get ViewModels via `koinViewModel()`** — no manual construction

### Adding a New Feature

1. **Model** — add domain model in `domain/model/`
2. **DAO + Entity** — add Room entity and DAO queries in `data/local/`
3. **Repository** — define interface in `domain/repository/`, implement in `data/repository/`
4. **Use Case** — business logic in `domain/usecase/`
5. **ViewModel** — expose state as `StateFlow` in `ui/screens/`
6. **Screen** — Compose UI collecting from ViewModel
7. **DI** — register everything in the appropriate Koin module

### Code Style

- Follow existing patterns — look at a similar feature before writing new code
- No unnecessary abstractions — three similar lines beat a premature helper
- Use `Flow` for reactive data, `suspend` for one-shot operations
- Prefer `StateFlow` with `stateIn()` over `MutableStateFlow` when wrapping a data source
- Bottom sheets for user input, dialogs for confirmations only

### Key Classes to Understand

| Class | What It Does |
|---|---|
| `ProcessIncomingSmsUseCase` | Main SMS pipeline — whitelist check, pattern match, save, forward |
| `ForwardSmsToWebhookUseCase` | Builds payload, makes HTTP call, creates delivery log, schedules retry |
| `SmsPatternExtractor` | Extracts patterns from sample messages and matches incoming SMS |
| `WebhookClientFactory` | Creates Retrofit instances dynamically per webhook config |
| `SmsBroadcastReceiver` | Entry point — catches SMS_RECEIVED broadcasts |
| `SmsReceiverService` | Foreground service that runs the processing pipeline |
| `SmsForwardWorker` | WorkManager job for retrying failed deliveries |

## Build Configuration

| Property | Value |
|---|---|
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 15) |
| Compile SDK | 36 |
| Java Version | 11 |
| Compose | Enabled |
| Room Schema Export | Enabled (`app/schemas/`) |


MIT License
---

Built by [Asrat Adane](https://t.me/maaser_cody)
