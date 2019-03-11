package com.strumenta.funnel

import org.drools.runtime.StatefulKnowledgeSession
import java.time.LocalDate
import java.time.Month

fun loadDataIntoSession(ksession: StatefulKnowledgeSession, dayToConsider: LocalDate) : EmailScheduler {
    val products = listOf(
            Product("My book", 20.0f),
            Product("Video course", 100.0f),
            Product("Consulting package", 500.0f)
    )
    val persons = listOf(
            Subscriber("Mario", LocalDate.of(2019, Month.JANUARY, 1), "Italy"),
            Subscriber("Amelie", LocalDate.of(2019, Month.FEBRUARY, 1), "France"),
            Subscriber("Bernd", LocalDate.of(2019, Month.APRIL, 18), "Germany"),
            Subscriber("Eric", LocalDate.of(2018, Month.OCTOBER, 1), "USA"),
            Subscriber("Albert", LocalDate.of(2016, Month.OCTOBER, 12), "USA")
    )
    val sequences = listOf(
            EmailSequence("Present book",
                    listOf(
                            Email("Present book 1", "Here is the book...", tags= listOf("book_explanation")),
                            Email("Present book 2", "Here is the book...", tags= listOf("book_explanation")),
                            Email("Present book 3", "Here is the book...", tags= listOf("book_explanation"))
                    )),
            EmailSequence("Present course",
                    listOf(
                            Email("Present course 1", "Here is the course...", tags= listOf("course_explanation")),
                            Email("Present course 2", "Here is the course...", tags= listOf("course_explanation")),
                            Email("Present course 3", "Here is the course...", tags= listOf("course_explanation"))
                    ))
    )
    ksession.insert(Email("Question to user", "Do you..."))
    ksession.insert(Email("Interesting topic A", "Do you..."))
    ksession.insert(Email("Interesting topic B", "Do you..."))
    ksession.insert(Email("Suggest book", "I wrote a book...", tags= listOf("book_offer")))
    ksession.insert(Email("Suggest course", "I wrote a course...", tags= listOf("course_offer")))
    ksession.insert(Email("Suggest consulting", "I offer consulting...", tags= listOf("consulting_offer")))

    // assign emails to vars, so that I can specify that some users have already received them

    val emailScheduler = EmailScheduler(ksession)
    ksession.setGlobal("scheduler", emailScheduler)
    ksession.setGlobal("day", dayToConsider)

    ksession.insert(products)
    persons.forEach {
        ksession.insert(it)
    }
    sequences.forEach {
        ksession.insert(it)
    }
    return emailScheduler
}