package com.strumenta.funnel

import org.drools.KnowledgeBase
import org.drools.KnowledgeBaseFactory

import org.drools.builder.KnowledgeBuilderFactory
import org.drools.builder.ResourceType

import org.drools.io.ResourceFactory
import org.drools.runtime.StatefulKnowledgeSession

import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.util.*

data class Product(val name: String, val price: Float)
data class Purchase(val product: Product, val price: Float, val date: LocalDate)
data class Person(val name: String,
                  val subscriptionDate: LocalDate,
                  val country: String,
                  val email: String = "$name@foo.com",
                  val tags: List<String> = emptyList(),
                  val purchases: List<Purchase> = emptyList(),
                  val emailReceived: List<EmailSending> = emptyList()) {
    fun isInSequence(emailSequence: EmailSequence) = hasReceived(emailSequence.first) && !hasReceived(emailSequence.last)
    fun hasReceived(email: Email) = emailReceived.any { it.email == email }
    fun hasReceivedEmailsInLastDays(nDays: Long) : Boolean {
        val today = LocalDate.now()
        return emailReceived.any { it.date.isAfter(today.minusDays(nDays)) }
    }
    fun isOnHolidays(date: LocalDate) : Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }
}
data class Email(val title: String, val content: String, val tags: List<String> = emptyList())
data class EmailSequence(val title: String, val emails: List<Email>, val tags: List<String> = emptyList()) {
    val first = emails.first()
    val last = emails.last()

    init {
        require(emails.isNotEmpty())
    }
}
data class EmailSending(val email: Email, val person: Person, val date: LocalDate)
data class EmailScheduling(val sending: EmailSending, val importance: Double,
                           val timeSensitive: Boolean = false,
                           var blocked: Boolean = false)

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
}

fun loadDataIntoSession(ksession: StatefulKnowledgeSession) : EmailScheduler {
    val products = listOf(
            Product("My book", 20.0f),
            Product("Video course", 100.0f),
            Product("Consulting package", 500.0f)
    )
    val persons = listOf(
            Person("Mario", LocalDate.of(2019, Month.JANUARY, 1), "Italy"),
            Person("Amelie", LocalDate.of(2019, Month.FEBRUARY, 1), "France"),
            Person("Eric", LocalDate.of(2018, Month.OCTOBER, 1), "USA")
    )
    val sequences = listOf(
            EmailSequence("Present book",
                    listOf(
                            Email("Present book 1", "Here is the book..."),
                            Email("Present book 2", "Here is the book..."),
                            Email("Present book 3", "Here is the book...")
                    ))
    )
    val suggestBook = Email("Suggest book", "I wrote a book...")
    val suggestVideoCourse = Email("Suggest video course", "I recorded a videoCourse...")
    val suggestConsulting = Email("Suggest consulting", "I sell consulting...")
    val emailScheduler = EmailScheduler(ksession)
    ksession.setGlobal("scheduler", emailScheduler)

    //val interestingTopic = Email("")
    products.forEach {
        ksession.insert(it)
    }
    persons.forEach {
        ksession.insert(it)
    }
    sequences.forEach {
        ksession.insert(it)
    }
    return emailScheduler
}

fun showSending(emailScheduler: EmailScheduler) {
    println("Showing email scheduling")
}

// Rules:
// Send certain emails only after sending x emails with interesting stuff
// Send certain emails on certain week days
// Send certain emails with certain interval
// Never re-send the same email
// Do not send certain emails to people who bought something
// Send certain emails to people who bought something
// Rules regarding a certain sequence
// Rules for emails that have to be sent within a certain date

fun main(args: Array<String>) {
    try {
        val kbase = readKnowledgeBase()
        val ksession = kbase.newStatefulKnowledgeSession()
        val emailScheduler = loadDataIntoSession(ksession)

        ksession.fireAllRules()

        showSending(emailScheduler)

    } catch (t: Throwable) {
        t.printStackTrace()
    }

}

private fun readKnowledgeBase(): KnowledgeBase {

    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder()

    kbuilder.add(ResourceFactory.newFileResource(File("rules/generic.drl")), ResourceType.DRL)
    //kbuilder.add(ResourceFactory.newFileResource(File("rules/book.drl")), ResourceType.DRL)

//        kbuilder.add(ResourceFactory.newClassPathResource("Pune.drl"), ResourceType.DRL)
//        kbuilder.add(ResourceFactory.newClassPathResource("Nagpur.drl"), ResourceType.DRL)

    val errors = kbuilder.errors

    if (errors.size > 0) {
        for (error in errors) {
            System.err.println(error)
        }
        throw IllegalArgumentException("Could not parse knowledge.")
    }

    val kbase = KnowledgeBaseFactory.newKnowledgeBase()
    kbase.addKnowledgePackages(kbuilder.knowledgePackages)

    return kbase
}
