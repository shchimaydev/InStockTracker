# RevenueCat Setup Guide

This guide explains how to set up and use RevenueCat for in-app purchases in the InStockTracker Android app.

## Prerequisites

1. **RevenueCat Account**: Sign up at [https://www.revenuecat.com/](https://www.revenuecat.com/)
2. **Google Play Console**: Set up your app in Google Play Console with in-app products configured
3. **RevenueCat Project**: Create a new project in RevenueCat dashboard

## Setup Steps

### 1. Configure RevenueCat Dashboard

1. **Create a new app** in RevenueCat dashboard
   - Select "Android" as the platform
   - Enter your app's package name: `com.ist.instocktracker`

2. **Connect to Google Play**
   - In RevenueCat dashboard, go to your app settings
   - Navigate to "Google Play" section
   - Upload your Google Play service credentials JSON file
   - Follow RevenueCat's guide: [https://www.revenuecat.com/docs/creating-play-service-credentials](https://www.revenuecat.com/docs/creating-play-service-credentials)

3. **Create Products**
   - Go to "Products" tab in RevenueCat dashboard
   - Add your subscription products (they should match your Google Play Console products)
   - Example: `premium_monthly`, `premium_yearly`

4. **Create Entitlements**
   - Go to "Entitlements" tab
   - Create an entitlement (e.g., "premium")
   - Attach your products to this entitlement

5. **Configure Offerings**
   - Go to "Offerings" tab
   - Create an offering (e.g., "default")
   - Add packages with your products
   - This is what will be displayed in the paywall

6. **Set up Paywall**
   - Go to "Paywalls" section in RevenueCat dashboard
   - Create a new paywall or customize the default one
   - Configure the design, copy, and layout
   - Attach the paywall to your offering

### 2. Get API Key

1. In RevenueCat dashboard, navigate to **Project Settings → API Keys**
2. Copy the **Public API Key** for Android
3. This key is safe to include in your app code

### 3. Update Your Code

Open `composeApp/src/androidMain/kotlin/com/ist/instocktracker/MainActivity.kt` and replace the placeholder API key:

```kotlin
// Replace this line:
val revenueCatApiKey = "YOUR_REVENUECAT_API_KEY_HERE"

// With your actual API key:
val revenueCatApiKey = "rc_android_xxxxxxxxxxxx"
```

**Alternative: Using BuildConfig (Recommended for production)**

For better security, you can store the API key in `local.properties`:

1. Add to `local.properties`:
   ```properties
   revenuecat.api.key=rc_android_xxxxxxxxxxxx
   ```

2. Update `composeApp/build.gradle.kts`:
   ```kotlin
   android {
       // ... other config

       defaultConfig {
           // ... other config

           // Load from local.properties
           val properties = Properties()
           properties.load(project.rootProject.file("local.properties").inputStream())
           buildConfigField("String", "REVENUECAT_API_KEY", "\"${properties.getProperty("revenuecat.api.key")}\"")
       }

       buildFeatures {
           buildConfig = true
       }
   }
   ```

3. Update MainActivity.kt:
   ```kotlin
   val revenueCatApiKey = BuildConfig.REVENUECAT_API_KEY
   ```

## Usage

### Showing the Paywall

Navigate to the paywall screen from anywhere in your app:

```kotlin
navController.navigate(Route.Paywall)
```

### Checking Subscription Status

Use the `SubscriptionViewModel` to check subscription status:

```kotlin
val subscriptionViewModel: SubscriptionViewModel = viewModel()
val subscriptionState by subscriptionViewModel.subscriptionState.collectAsState()

if (subscriptionState.isPremium) {
    // User has premium access
    // Show premium features
} else {
    // User doesn't have premium
    // Show upgrade button or limited features
}
```

### Identifying Users

When a user logs in, identify them with RevenueCat:

```kotlin
import com.ist.instocktracker.billing.RevenueCatManager

// After user authentication
RevenueCatManager.identifyUser(userId = "user_unique_id")
```

### Logging Out Users

When a user logs out:

```kotlin
RevenueCatManager.logoutUser()
```

### Restoring Purchases

If a user wants to restore their purchases:

```kotlin
val subscriptionViewModel: SubscriptionViewModel = viewModel()
subscriptionViewModel.restorePurchases()
```

## Testing

### Test Purchases

1. **Add test user** in Google Play Console:
   - Go to Google Play Console → Settings → License Testing
   - Add your test email addresses

2. **Use sandbox environment**:
   - RevenueCat automatically uses sandbox for debug builds
   - No special configuration needed

3. **Test purchase flow**:
   - Install the debug build on your test device
   - Navigate to the paywall
   - Make a test purchase with your test account

### Debug Logging

Debug logs are automatically enabled for debug builds. Check Logcat for:
- `RevenueCatManager`: SDK initialization and purchase operations
- `PaywallScreen`: Paywall UI events
- `SubscriptionViewModel`: Subscription state changes

Filter Logcat by tag:
```
adb logcat -s RevenueCatManager PaywallScreen SubscriptionViewModel
```

## Architecture Overview

### Files Added

1. **RevenueCatManager.kt** (`billing/RevenueCatManager.kt`)
   - Singleton manager for RevenueCat SDK
   - Handles initialization, purchases, and subscription status
   - Provides coroutine-based API

2. **PaywallScreen.kt** (`ui/billing/PaywallScreen.kt`)
   - Composable screen using RevenueCat Paywall UI
   - Handles purchase callbacks and error states
   - Integrates with navigation

3. **SubscriptionViewModel.kt** (`ui/billing/SubscriptionViewModel.kt`)
   - ViewModel for managing subscription state
   - Observes CustomerInfo updates
   - Provides subscription status to UI layer

4. **Route.kt** (updated)
   - Added `Paywall` route for navigation

5. **AppNavigation.kt** (updated)
   - Added paywall composable to navigation graph

## Important Notes

1. **API Key Security**:
   - The public API key is safe to include in your app
   - Never commit `local.properties` to version control
   - Add `local.properties` to `.gitignore`

2. **Entitlement IDs**:
   - The default entitlement ID used in code is `"premium"`
   - Make sure this matches your RevenueCat entitlement configuration
   - Update in `SubscriptionViewModel.kt` if using a different name

3. **Android-Only Implementation**:
   - All RevenueCat code is in `androidMain` source set
   - This is NOT a KMP implementation
   - iOS would need separate implementation if required

4. **Google Play Billing**:
   - RevenueCat handles all Google Play Billing integration
   - You don't need to add Google Play Billing dependencies directly
   - RevenueCat SDK includes them automatically

## Troubleshooting

### "Unable to connect to RevenueCat"
- Check your internet connection
- Verify API key is correct
- Check RevenueCat dashboard for service status

### "No offerings found"
- Ensure you've created offerings in RevenueCat dashboard
- Verify products are properly configured in Google Play Console
- Check that products are attached to entitlements and offerings

### Purchases not working
- Verify Google Play service credentials are uploaded to RevenueCat
- Check that products exist in Google Play Console
- Ensure test account is added to license testing
- Try clearing Google Play Store cache

## Resources

- [RevenueCat Documentation](https://www.revenuecat.com/docs)
- [RevenueCat Android SDK](https://github.com/RevenueCat/purchases-android)
- [RevenueCat Paywall UI](https://www.revenuecat.com/docs/displaying-products#paywalls)
- [Google Play Billing](https://developer.android.com/google/play/billing)

## Support

For issues specific to RevenueCat integration:
- [RevenueCat Support](https://community.revenuecat.com/)
- [RevenueCat Status Page](https://status.revenuecat.com/)
