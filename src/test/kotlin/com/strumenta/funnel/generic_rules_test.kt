package com.strumenta.funnel

import org.drools.KnowledgeBase
import org.drools.KnowledgeBaseFactory
import org.drools.builder.KnowledgeBuilderFactory
import org.drools.builder.ResourceType
import org.drools.io.ResourceFactory
import org.drools.runtime.StatefulKnowledgeSession
import java.io.File
import java.time.LocalDate
import java.time.Month
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test as test

class GenericRulesTest {

    private fun prepareKnowledgeBase(files: List<File>, rulesToKeep: List<String>): KnowledgeBase {
        val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder()

        files.forEach { kbuilder.add(ResourceFactory.newFileResource(it), ResourceType.DRL) }

        val errors = kbuilder.errors

        if (errors.size > 0) {
            for (error in errors) {
                System.err.println(error)
            }
            throw IllegalArgumentException("Could not parse knowledge.")
        }

        val kbase = KnowledgeBaseFactory.newKnowledgeBase()
        kbase.addKnowledgePackages(kbuilder.knowledgePackages)
        kbase.knowledgePackages.forEach { kp ->
            kp.rules.forEach { r ->
                if (r.name !in rulesToKeep) {
                    kbase.removeRule(kp.name, r.name)
                }
            }
        }

        return kbase
    }

    fun loadDataIntoSession(ksession: StatefulKnowledgeSession,
                            dayToConsider: LocalDate, dataTransformer: ((Subscriber, Email) -> Unit)? = null)
            : EmailScheduler {

        val amelie = Subscriber("Amelie",
                LocalDate.of(2019, Month.FEBRUARY, 1),
                "France")
        val bookSeqEmail1 = Email("Present book 1", "Here is the book...",
                tags= listOf("book_explanation"))

        val products = listOf(
                Product("My book", 20.0f),
                Product("Video course", 100.0f),
                Product("Consulting package", 500.0f)
        )
        val persons = listOf(amelie)
        val sequences = listOf(
                EmailSequence("Present book", listOf(
                        bookSeqEmail1,
                        Email("Present book 2", "Here is the book...",
                                tags= listOf("book_explanation")),
                        Email("Present book 3", "Here is the book...",
                                tags= listOf("book_explanation"))
                ))
        )
        dataTransformer?.invoke(amelie, bookSeqEmail1)

        ksession.insert(Email("Question to user",
                "Do you..."))
        ksession.insert(Email("Interesting topic A",
                "Do you..."))
        ksession.insert(Email("Interesting topic B",
                "Do you..."))
        ksession.insert(Email("Suggest book",
                "I wrote a book...",
                tags= listOf("book_offer")))
        ksession.insert(Email("Suggest course",
                "I wrote a course...",
                tags= listOf("course_offer")))
        ksession.insert(Email("Suggest consulting",
                "I offer consulting...",
                tags= listOf("consulting_offer")))

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

    private fun setupSessionAndFireRules(dayToConsider: LocalDate, rulesToKeep: List<String>,
                                         dataTransformer: ((Subscriber, Email) -> Unit)? = null) : List<EmailScheduling> {
        val kbase = prepareKnowledgeBase(listOf(File("rules/generic.drl")), rulesToKeep)
        val ksession = kbase.newStatefulKnowledgeSession()
        val emailScheduler = loadDataIntoSession(ksession, dayToConsider, dataTransformer)

        ksession.fireAllRules()

        return emailScheduler.selectScheduling(dayToConsider)
    }

    @test fun startSequencePositiveCase() {
        val schedulings = setupSessionAndFireRules(
                LocalDate.of(2019, Month.MARCH, 17), listOf("Start sequence"))
        assertEquals(1, schedulings.size)
        assertNotNull(schedulings.find {
            it.sending.email.title == "Present book 1"
                    && it.sending.subscriber.name == "Amelie" })
    }

    @test fun startSequenceWhenFirstEmailReceived() {
        val schedulings = setupSessionAndFireRules(
                LocalDate.of(2019, Month.MARCH, 17),
                listOf("Start sequence")) { amelie, bookSeqEmail1 ->
            amelie.emailsReceived.add(
                    EmailSending(bookSeqEmail1, amelie,
                            LocalDate.of(2018, Month.NOVEMBER, 12)))
        }

        assertEquals(0, schedulings.size)
    }

}
