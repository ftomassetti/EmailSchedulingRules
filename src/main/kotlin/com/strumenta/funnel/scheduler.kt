package com.strumenta.funnel

import org.drools.runtime.StatefulKnowledgeSession
import java.time.LocalDate

class EmailScheduler(val ksession: StatefulKnowledgeSession) {

    @JvmOverloads
    fun schedule(email: Email, person: Person, date: LocalDate, importance: Double,
                 timeSensitive: Boolean = false) {
        val scheduling = EmailScheduling(EmailSending(email, person, date), importance, timeSensitive)
        ksession.insert(scheduling)
        println("Scheduling ${email.title} for ${person.name}")
        //System.out.println("Start sequence " + sequence.getTitle() + " for " + person.getName());
    }

    fun block(scheduling: EmailScheduling) {
        println("Blocking ${scheduling.sending.email.title} for ${scheduling.sending.person.name}")
        scheduling.blocked = true
    }

    fun selectScheduling(localDate: LocalDate): List<EmailScheduling> {
        // We select all the scheduling that were not blocked and at most one for each person per day
        // We do that just for the day considered (typically today) as things could change, so the scheduling
        // should always be redone
        val schedulings = ksession.objects.filterIsInstance(EmailScheduling::class.java)
        return schedulings.filter { !it.blocked }
//        val schedulingsByPerson = schedulings.groupBy { it.sending.person }
//        return schedulingsByPerson.keys.map {
//            val possibleSchedulings = schedulingsByPerson[it]!!.filter { !it.blocked }
//            if (possibleSchedulings.isEmpty()) {
//                null
//            } else if (possibleSchedulings.any { it.timeSensitive }) {
//                possibleSchedulings.filter { !it.timeSensitive }.sortedBy { it.importance }.last()
//            } else {
//                possibleSchedulings.last()
//            }
//        }.toList().filterNotNull()
    }
}
