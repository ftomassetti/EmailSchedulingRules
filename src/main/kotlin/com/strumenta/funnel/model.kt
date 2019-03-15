package com.strumenta.funnel

import java.time.DayOfWeek
import java.time.LocalDate

data class Product(val name: String,
                   val price: Float)

data class Purchase(val product: Product,
                    val price: Float,
                    val date: LocalDate)

data class Subscriber(val name: String,
                      val subscriptionDate: LocalDate,
                      val country: String,
                      val email: String = "$name@foo.com",
                      val tags: List<String> = emptyList(),
                      val purchases: List<Purchase> = emptyList(),
                      val emailsReceived: List<EmailSending> = emptyList()) {

    fun isInSequence(emailSequence: EmailSequence) =
            hasReceived(emailSequence.first)
                    && !hasReceived(emailSequence.last)

    fun hasReceived(email: Email) = emailsReceived.any { it.email == email }

    fun hasReceivedEmailsInLastDays(nDays: Long, day: LocalDate)
            : Boolean {
        return emailsReceived.any {
            it.date.isAfter(day.minusDays(nDays))
        }
    }
    fun isOnHolidays(date: LocalDate) : Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY
                || date.dayOfWeek == DayOfWeek.SUNDAY
    }
    fun emailReceivedWithTag(tag: String) =
            emailsReceived.count { tag in it.email.tags }
}

data class Email(val title: String,
                 val content: String,
                 val tags: List<String> = emptyList())

data class EmailSequence(val title: String,
                         val emails: List<Email>,
                         val tags: List<String> = emptyList()) {

    val first = emails.first()
    val last = emails.last()

    init {
        require(emails.isNotEmpty())
    }
}

data class EmailSending(val email: Email,
                        val subscriber: Subscriber,
                        val date: LocalDate)

data class EmailScheduling(val sending: EmailSending,
                           val importance: Double,
                           val timeSensitive: Boolean = false,
                           var blocked: Boolean = false) {
    val id = ++nextId

    companion object {
        private var nextId = 0
    }
}
