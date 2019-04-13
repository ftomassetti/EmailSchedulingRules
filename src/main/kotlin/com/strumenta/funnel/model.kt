package com.strumenta.funnel

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

enum class Priority {
    TRIVIAL,
    NORMAL,
    IMPORTANT,
    VITAL
}

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
                      val emailsReceived: MutableList<EmailSending> = LinkedList()) {

    val actualEmailsReceived
            get() = emailsReceived.map { it.email }

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

    fun next(emailsReceived: List<Email>) =
        emails.first { it !in emailsReceived }
}

data class EmailSending(val email: Email,
                        val subscriber: Subscriber,
                        val date: LocalDate) {
    override fun equals(other: Any?): Boolean {
        return if (other is EmailSending) {
            this.email === other.email && this.subscriber === other.subscriber && this.date == other.date
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return this.email.title.hashCode() * 7 + this.subscriber.name.hashCode() * 3 + this.date.hashCode()
    }
}

data class EmailScheduling(val sending: EmailSending,
                           val priority: Priority,
                           val timeSensitive: Boolean = false,
                           var blocked: Boolean = false) {
    val id = ++nextId

    companion object {
        private var nextId = 0
    }
}
