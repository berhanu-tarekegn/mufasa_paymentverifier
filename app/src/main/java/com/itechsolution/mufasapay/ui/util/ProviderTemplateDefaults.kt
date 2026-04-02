package com.itechsolution.mufasapay.ui.util

data class TemplatePreset(
    val label: String,
    val pattern: String
)

object ProviderTemplateDefaults {

    fun forSender(senderId: String): List<TemplatePreset> = defaults[senderId].orEmpty()

    private val defaults = mapOf(
        "CBE BIRR" to listOf(
            TemplatePreset(
                label = "Money received",
                pattern = "Dear {name}, you received {amount}Br. from {ignore} on {datetime},Txn ID {transaction}.Your CBE Birr account balance is {balance}Br.{ignore}"
            )
        ),
        "127" to listOf(
            TemplatePreset(
                label = "Money received",
                pattern = "Dear {name} \nYou have received ETB {amount} from {ignore} on {datetime}. Your transaction number is {transaction}. Your current E-Money Account balance is ETB {balance}.{ignore}"
            )
        ),
        "MPESA" to listOf(
            TemplatePreset(
                label = "Money received",
                pattern = "Dear {name}, you have received {amount} Birr from {ignore} on {datetime}. Transaction number {transaction}. Your current M-PESA balance is {balance} Birr.{ignore}"
            )
        ),
        "CBE" to listOf(
            TemplatePreset(
                label = "Account credited",
                pattern = "Dear {name} your Account {account} has been Credited with ETB {amount} from {ignore}, on {datetime} with Ref No {transaction} Your Current Balance is ETB {balance}.{ignore}"
            )
        )
    )
}
