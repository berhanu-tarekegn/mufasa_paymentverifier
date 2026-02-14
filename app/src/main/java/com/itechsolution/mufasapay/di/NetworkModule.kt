package com.itechsolution.mufasapay.di

import com.itechsolution.mufasapay.data.remote.WebhookClientFactory
import com.itechsolution.mufasapay.data.remote.api.WebhookApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    // Moshi JSON converter
    single {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // Base OkHttp client (for general use or testing)
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Base Retrofit (for general use or testing)
    single {
        Retrofit.Builder()
            .baseUrl("https://api.placeholder.com/") // Placeholder base URL
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    // WebhookClientFactory - Creates configured Retrofit instances for webhook forwarding
    single { WebhookClientFactory(get()) }

    // Default WebhookApiService (for testing without config)
    single { get<Retrofit>().create(WebhookApiService::class.java) }
}
