package com.strumenta.funnel

import org.kie.api.runtime.KieSession
import java.time.LocalDate

class EmailScheduler(val ksession: KieSession) {

    @JvmOverloads
    fun schedule(email: Email, subscriber: Subscriber,
                 date: LocalDate, importance: Double,
                 timeSensitive: Boolean = false) {
        val scheduling = EmailScheduling(
                EmailSending(email, subscriber, date),
                importance, timeSensitive)
        ksession.insert(scheduling)
        println("Scheduling ${email.title} for ${subscriber.name}")
    }

    fun block(scheduling: EmailScheduling) {
        println("Blocking ${scheduling.sending.email.title} for ${scheduling.sending.subscriber.name}")
        scheduling.blocked = true
    }

    fun selectScheduling(localDate: LocalDate): List<EmailScheduling> {
        // We select all the scheduling that were not blocked and at most one for each subscriber per day
        // We do that just for the day considered (typically today) as things could change, so the scheduling
        // should always be redone
        val schedulings = ksession.objects.filterIsInstance(EmailScheduling::class.java)
        return schedulings.filter { !it.blocked }
    }
}
