package com.storefront.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Hilt entry point. Firebase initialises itself from google-services.json. */
@HiltAndroidApp
class StorefrontApplication : Application()
