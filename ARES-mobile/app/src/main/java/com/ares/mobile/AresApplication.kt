package com.ares.mobile

import android.app.Application

class AresApplication : Application() {
	val appContainer by lazy { AppContainer(this) }
}