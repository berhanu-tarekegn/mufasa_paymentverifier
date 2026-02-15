package com.itechsolution.mufasapay.util

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsPatternExtractorTest {

    @Test
    fun `test mpesa template matching`() {
        val message = "Dear Eyob, you have received 3.00 Birr from Asirat Adane Bekele-251716***514 on 6/2/26 at 10:07 AM. Transaction number UB64FXVZ28. Your current M-PESA balance is 2,424.84 Birr. Get extra 20MB when you buy 100MB daily bundle via M-PESA for 5 birr. To buy click https://bit.ly/M-PESA-SAFARICOM-ETHIOPIA"
        val pattern = "Dear {name}, you have received {amount} Birr from {ignore} on {datetime}. Transaction number {transaction}. Your current M-PESA balance is {balance} Birr.{ignore}"
        
        val isMatch = SmsPatternExtractor.matchesPattern(message, pattern)
        assertTrue("M-PESA message should match pattern", isMatch)
    }

    @Test
    fun `test telebirr template matching`() {
        val message = "Dear Berhanu \nYou have received ETB 100.00 from kajela dera(2519****3056) on 15/02/2026 18:44:01. Your transaction number is DBF1T1YXMH. Your current E-Money Account balance is ETB 885.18.\nThank you for using telebirr\nEthio telecom"
        val pattern = "Dear {name} \nYou have received ETB {amount} from {ignore} on {datetime}. Your transaction number is {transaction}. Your current E-Money Account balance is ETB {balance}.{ignore}"
        
        val isMatch = SmsPatternExtractor.matchesPattern(message, pattern)
        assertTrue("Telebirr message should match pattern", isMatch)
    }

    @Test
    fun `test cbe template matching`() {
        val message = "Dear Birhanu your Account 1*****6171 has been Credited with ETB 300.00 from Nigus Solomon, on 11/02/2026 at 18:47:21 with Ref No FT26042XNSX4 Your Current Balance is ETB 2,496.38. Thank you for Banking with CBE! https://apps.cbe.com.et:100/?id=FT26042XNSX497906171"
        val pattern = "Dear {name} your Account {account} has been Credited with ETB {amount} from {ignore}, on {datetime} with Ref No {transaction} Your Current Balance is ETB {balance}.{ignore}"
        
        val isMatch = SmsPatternExtractor.matchesPattern(message, pattern)
        assertTrue("CBE message should match pattern", isMatch)
    }

    @Test
    fun `test cbe birr template matching`() {
        val message = "Dear birhanu, you received 42.00Br. from 0912296964 - FUAD RAHMETO SEMAN on 09/02/26 17:48,Txn ID DB9215LOOSO.Your CBE Birr account balance is 217.00Br. Thank you! For invoice https://cbepay1.cbe.com.et/aureceipt?TID=DB9215LOOSO&PH=0909040408"
        val pattern = "Dear {name}, you received {amount}Br. from {ignore} on {datetime},Txn ID {transaction}.Your CBE Birr account balance is {balance}Br.{ignore}"
        
        val isMatch = SmsPatternExtractor.matchesPattern(message, pattern)
        assertTrue("CBE Birr message should match pattern", isMatch)
    }

    @Test
    fun `test masked account extraction`() {
        val message = "Account 1*****6171 Credited"
        val pattern = "Account {account} Credited"
        
        val isMatch = SmsPatternExtractor.matchesPattern(message, pattern)
        assertTrue("Should match masked account", isMatch)
    }
}
