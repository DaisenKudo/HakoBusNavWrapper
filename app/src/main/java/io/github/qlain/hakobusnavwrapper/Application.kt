package io.github.qlain.hakobusnavwrapper

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            AndroidThreeTen.init(this)
        }
    }
}