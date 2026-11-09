# Income and Expense Tracker Android App

A complete, production-ready native Android app built with **Kotlin**, **Jetpack Compose**, **Room Database**, and **WorkManager** with Google Drive automated cloud backup.

## Features
### Income Management
1. **Sl. No.** (Auto-incremented)
2. **Customer ID** (Auto-formatted, e.g., `CUS-001`, `CUS-002`)
3. **Date** (Auto-populated with current timestamp)
4. **Customer Name**
5. **Customer Mobile Number**
6. **Material**
7. **Item**
8. **Payment Method** (Dropdown: `CASH`, `UPI`, `CREDIT`, `OTHER_PAYMENTS`)
9. **Credit Deadline Date** (Conditionally visible & enforced when `CREDIT` is chosen)
10. **Customer Grouping**: Tab view grouping all income entries by their Customer ID.

### Expense Management
1. **Sl. No.** (Auto-incremented)
2. **Date** (Auto-populated)
3. **Purchase materials**
4. **Quantity**
5. **Unit** (Dropdown with standard units: Nos, Kg, Bags, Litres, Tons, etc.)
6. **Vendor Name**
7. **Total Amount**
8. **Payment Method** (`CASH`, `UPI`, `CREDIT`, `OTHER_PAYMENTS`)
9. **Credit Deadline Date** (Mandatory date picker on selecting `CREDIT`)

### Automated Google Drive Backup
- **WorkManager Periodic Work**: Uploads the Room SQLite database file (`income_expense_tracker_db`) to the authenticated Google Drive `appDataFolder` every 24 hours when connected to the internet and battery is not low.
- **Top App Bar Drive Sign-in**: One-click Google sign-in integration.

## How to Open and Run
1. Unzip the project.
2. Open **Android Studio** (Hedgehog, Iguana, or later).
3. Select **Open an existing project** and pick the unzipped directory.
4. Allow Gradle Sync to finish.
5. Create your OAuth2 Android Client ID in Google Cloud Console with your app package `com.example.incometracker` and your debug keystore SHA-1.
6. Run on an Android Device or Emulator running Android 8.0+ (API 26+).
