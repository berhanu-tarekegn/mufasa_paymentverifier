package com.itechsolution.mufasapay.di

import com.itechsolution.mufasapay.ui.screens.dashboard.DashboardViewModel
import com.itechsolution.mufasapay.ui.screens.history.SmsHistoryViewModel
import com.itechsolution.mufasapay.ui.screens.onboarding.ProviderSelectionViewModel
import com.itechsolution.mufasapay.ui.screens.permissions.PermissionViewModel
import com.itechsolution.mufasapay.ui.screens.senders.SenderManagementViewModel
import com.itechsolution.mufasapay.ui.screens.webhook.WebhookConfigViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for UI ViewModels
 */
val viewModelModule = module {

    // Permissions ViewModel
    viewModel { PermissionViewModel() }

    // Provider Selection (Onboarding) ViewModel
    viewModel {
        ProviderSelectionViewModel(
            addSenderUseCase = get(),
            senderRepository = get(),
            preferencesManager = get()
        )
    }

    // Dashboard ViewModel
    viewModel {
        DashboardViewModel(
            getDeliveryStatsUseCase = get(),
            getSmsHistoryUseCase = get()
        )
    }

    // Sender Management ViewModel
    viewModel {
        SenderManagementViewModel(
            getAllSendersUseCase = get(),
            toggleSenderStatusUseCase = get(),
            addSenderUseCase = get(),
            removeSenderUseCase = get(),
            senderRepository = get()
        )
    }

    // Webhook Configuration ViewModel
    viewModel {
        WebhookConfigViewModel(
            getWebhookConfigUseCase = get(),
            saveWebhookConfigUseCase = get(),
            testWebhookConnectionUseCase = get()
        )
    }

    // SMS History ViewModel
    viewModel {
        SmsHistoryViewModel(
            getSmsHistoryUseCase = get(),
            retryFailedDeliveryUseCase = get(),
            deleteSmsTransactionUseCase = get()
        )
    }
}
