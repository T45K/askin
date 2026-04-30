package io.github.t45k.askin

import android.app.Application
import io.github.t45k.askin.data.local.AppDatabase
import io.github.t45k.askin.data.local.seed.InitialMasterSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AskinApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            InitialMasterSeeder(database).seedIfNeeded()
        }
    }
}
