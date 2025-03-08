package bochenchleba.com.findcatpair.application

import android.app.Application
import bochenchleba.com.findcatpair.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class FindCatPairApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FindCatPairApplication)
            modules(appModule)
        }
    }
}