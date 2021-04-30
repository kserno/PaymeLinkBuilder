package sk.kserno.payme

import android.net.Uri
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class PayMeLinkBuilder(
    _iban: String,
    _amount: String,
    _currencyCode: String = "EUR",
    _version: Int = 1,
    private var validate: Boolean = true
) {

    private var paymentIdentification: String? = null
    private var message: String? = null
    private var creditorsName: String? = null

    private lateinit var amount: String
    private lateinit var iban: String
    private var version: Int = 1
    private lateinit var currencyCode: String

    private var dueDate: Date? = null

    init {
        setVersion(_version)
        setIban(_iban)
        setAmount(_amount)
        setCurrencyCode(_currencyCode)
    }

    private fun setVersion(version: Int) {
        checkVersion(version)
    }

    private fun checkVersion(version: Int) {
        if (validate && version != 1) {
            throw IllegalArgumentException("Only version 1 is supported")
        }
    }

    private var variableSymbol: String? = null
    private var specificSymbol: String? = null
    private var constantSymbol: String? = null


    fun setIban(iban: String): PayMeLinkBuilder {
        checkIban(iban)
        this.iban = iban
        return this
    }

    fun setCurrencyCode(currencyCode: String): PayMeLinkBuilder {
        checkCurrencyCode(currencyCode)
        this.currencyCode = currencyCode
        return this
    }

    fun setPaymentIdentification(paymentIdentification: String): PayMeLinkBuilder {
        checkPaymentIdentification(paymentIdentification)
        this.paymentIdentification = paymentIdentification
        return this
    }

    fun setCreditorsName(creditorsName: String): PayMeLinkBuilder {
        checkCreditorsName(creditorsName)
        this.creditorsName = creditorsName
        return this
    }

    fun setSpecificSymbol(specificSymbol: String): PayMeLinkBuilder {
        checkSpecificSymbol(specificSymbol)
        this.specificSymbol = specificSymbol
        return this
    }

    fun setVariableSymbol(variableSymbol: String): PayMeLinkBuilder {
        checkVariableSymbol(variableSymbol)
        this.variableSymbol = variableSymbol
        return this
    }

    fun setConstantSymbol(constantSymbol: String): PayMeLinkBuilder {
        checkConstantSymbol(constantSymbol)
        this.constantSymbol = constantSymbol
        return this
    }

    fun setMessage(message: String): PayMeLinkBuilder {
        checkMessage(message)
        this.message = message
        return this
    }

    fun setAmount(amount: String): PayMeLinkBuilder {
        checkAmount(amount)
        this.amount = amount
        return this
    }

    fun setAmount(amount: Double): PayMeLinkBuilder {
        return setAmount(currencyFormat.format(amount))
    }

    fun setDueDate(date: Date): PayMeLinkBuilder {
        this.dueDate = date
        return this
    }


    private fun checkPaymentIdentification(paymentIdentification: String) {
        if (validate && paymentIdentification.length > 35) {
            throw IllegalArgumentException("PI cannot be more than 35 char long")
        }
    }

    private fun checkAmount(amount: String) {
        if (validate && amount.length > 9) {
            throw IllegalArgumentException("Amount cannot be longer than 9 chars")
        }
    }

    private fun checkSpecificSymbol(specificSymbol: String) {
        if (validate && specificSymbol.length > 10) {
            throw IllegalArgumentException("Specific symbol cannot be more than 10 char long")
        }
    }

    private fun checkVariableSymbol(variableSymbol: String) {
        if (validate && variableSymbol.length > 10) {
            throw IllegalArgumentException("Variable symbol cannot be more than 10 char long")
        }
    }

    private fun checkConstantSymbol(constantSymbol: String) {
        if (validate && constantSymbol.length > 4) {
            throw IllegalArgumentException("Constant symbol cannot be more than 10 char long")
        }
    }

    private fun checkIban(iban: String) {
        if (validate && !isIbanValid(iban)) {
            throw IllegalArgumentException("IBAN incorrect format")
        }
    }

    private fun checkMessage(message: String?) {
        if (validate && message?.length ?: 0 > 140) {
            throw IllegalArgumentException("Message can be maximum 140 characters long")
        }
    }

    private fun checkCurrencyCode(currencyCode: String) {
        if (validate && version == 1 && currencyCode != "EUR") {
            throw IllegalArgumentException("In version 1, only EUR as currency code is supported.")
        }
    }

    private fun checkCreditorsName(creditorsName: String) {
        if (validate && creditorsName.length > 70) {
            throw IllegalArgumentException("CN cannot be longer than 70 chars")
        }
    }

    companion object {
        const val ibanPattern = "[A-Z]{2}[0-9]{2}[a-zA-Z0-9]{1,30}"

        const val PAYMENT_LINK_DOMAIN = "payme.sk"

        const val ATTR_VERSION = "V"
        const val ATTR_IBAN = "IBAN"
        const val ATTR_AMOUNT = "AM"
        const val ATTR_CURRENCY_CODE = "CC"
        const val ATTR_DUE_DATE = "DD"
        const val ATTR_PAYMENT_IDENTIFICATION = "PI"
        const val ATTR_MESSAGE = "MSG"
        const val ATTR_CREDITORS_NAME = "CN"

        const val ISO_8601_DATE_FORMAT = "yyyyMMdd"

        private val currencyFormat = DecimalFormat("#.##")
        private val dueDateFormat = SimpleDateFormat(ISO_8601_DATE_FORMAT, Locale.getDefault())

        fun isIbanValid(iban: String): Boolean {
            return Pattern.matches(ibanPattern, iban)
        }
    }


    fun build(): String {
        val builder = Uri.Builder()
            .scheme("https")
            .authority(PAYMENT_LINK_DOMAIN)
            .appendQueryParameter(ATTR_VERSION, version.toString())
            .appendQueryParameter(ATTR_AMOUNT, amount)
            .appendQueryParameter(ATTR_CURRENCY_CODE, currencyCode)

        if (iban.isNotEmpty()) {
            builder.appendQueryParameter(ATTR_IBAN, iban)
        }

        if (!getPaymentIdentification().isNullOrEmpty()) {
            builder.appendQueryParameter(ATTR_PAYMENT_IDENTIFICATION, getPaymentIdentification())
        }

        if (!message.isNullOrEmpty()) {
            builder.appendQueryParameter(ATTR_MESSAGE, message)
        }
        if (!creditorsName.isNullOrEmpty()) {
            builder.appendQueryParameter(ATTR_CREDITORS_NAME, creditorsName)
        }
        if (dueDate != null) {
            builder.appendQueryParameter(ATTR_DUE_DATE, dueDateFormat.format(dueDate))
        }

        return builder.build().toString()

    }

    private fun getPaymentIdentification(): String? {
        if (!paymentIdentification.isNullOrEmpty()) {
            return paymentIdentification
        }
        if (!variableSymbol.isNullOrEmpty()
            || !specificSymbol.isNullOrEmpty()
            || !constantSymbol.isNullOrEmpty()) {
            return "/VS$variableSymbol/SS$specificSymbol/KS$constantSymbol"
        }
        return null
    }

}