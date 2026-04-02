package com.itechsolution.mufasapay.di

import com.itechsolution.mufasapay.domain.usecase.ForwardSmsToWebhookUseCase
import com.itechsolution.mufasapay.domain.usecase.ProcessIncomingSmsUseCase
import com.itechsolution.mufasapay.domain.usecase.dashboard.GetDeliveryStatsUseCase
import com.itechsolution.mufasapay.domain.usecase.history.DeleteSmsTransactionUseCase
import com.itechsolution.mufasapay.domain.usecase.history.GetSmsHistoryUseCase
import com.itechsolution.mufasapay.domain.usecase.history.RetryFailedDeliveryUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.AddSenderUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.GetAllSendersUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.RemoveSenderUseCase
import com.itechsolution.mufasapay.domain.usecase.sender.ToggleSenderStatusUseCase
import com.itechsolution.mufasapay.domain.usecase.webhook.GetWebhookConfigUseCase
import com.itechsolution.mufasapay.domain.usecase.webhook.SaveWebhookConfigUseCase
import com.itechsolution.mufasapay.domain.usecase.webhook.TestWebhookConnectionUseCase
import com.itechsolution.mufasapay.data.remote.WebhookClientFactory
import com.itechsolution.mufasapay.ui.util.PreferencesManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Utilities
    single { PreferencesManager(androidContext()) }

    // Core use cases
    factory { ForwardSmsToWebhookUseCase(androidContext(), get(), get(), get(), get(), get(), get()) }
    factory { ProcessIncomingSmsUseCase(get(), get(), get()) }

    // Sender management use cases
    factory { AddSenderUseCase(get()) }
    factory { RemoveSenderUseCase(get()) }
    factory { ToggleSenderStatusUseCase(get()) }
    factory { GetAllSendersUseCase(get()) }

    // Webhook configuration use cases
    factory { SaveWebhookConfigUseCase(get()) }
    factory { GetWebhookConfigUseCase(get()) }
    factory { TestWebhookConnectionUseCase(get(), get()) }

    // Dashboard and history use cases
    factory { GetDeliveryStatsUseCase(get(), get(), get()) }
    factory { GetSmsHistoryUseCase(get()) }
    factory { RetryFailedDeliveryUseCase(get(), get()) }
    factory { DeleteSmsTransactionUseCase(get(), get(), get(), get()) }
}
