package com.itechsolution.mufasapay.di

import androidx.room.Room
import com.itechsolution.mufasapay.data.local.AppDatabase
import com.itechsolution.mufasapay.util.Constants
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5
            )
            .build()
    }

    // DAOs
    single { get<AppDatabase>().smsMessageDao() }
    single { get<AppDatabase>().senderDao() }
    single { get<AppDatabase>().senderTemplateDao() }
    single { get<AppDatabase>().webhookConfigDao() }
    single { get<AppDatabase>().deliveryLogDao() }
}
