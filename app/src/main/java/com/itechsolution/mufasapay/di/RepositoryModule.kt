package com.itechsolution.mufasapay.di

import com.itechsolution.mufasapay.data.repository.DeliveryRepositoryImpl
import com.itechsolution.mufasapay.data.repository.SenderRepositoryImpl
import com.itechsolution.mufasapay.data.repository.SmsRepositoryImpl
import com.itechsolution.mufasapay.data.repository.WebhookRepositoryImpl
import com.itechsolution.mufasapay.domain.repository.DeliveryRepository
import com.itechsolution.mufasapay.domain.repository.SenderRepository
import com.itechsolution.mufasapay.domain.repository.SmsRepository
import com.itechsolution.mufasapay.domain.repository.WebhookRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SmsRepository> { SmsRepositoryImpl(get()) }
    single<SenderRepository> { SenderRepositoryImpl(get()) }
    single<WebhookRepository> { WebhookRepositoryImpl(get(), get()) }
    single<DeliveryRepository> { DeliveryRepositoryImpl(get()) }
}
