package com.strumenta.funnel

import org.drools.core.impl.InternalKnowledgeBase
import org.drools.core.impl.KnowledgeBaseFactory
import org.kie.api.io.ResourceType
import org.kie.api.runtime.KieSession
import org.kie.internal.builder.KnowledgeBuilderFactory
import org.kie.internal.io.ResourceFactory
import java.io.File
import java.time.LocalDate


fun showSending(kieSession: KieSession) {
    println("Showing email scheduling")
    kieSession.selectScheduling(LocalDate.now()).forEach {
        println(it)
    }
}

private fun readKnowledgeBase(files: List<File>): InternalKnowledgeBase {
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
    kbase.addPackages(kbuilder.knowledgePackages)

    return kbase
}

fun KieSession.selectScheduling(localDate: LocalDate): List<EmailScheduling> {
    // We select all the scheduling that were not blocked and at most one for each subscriber per day
    // We do that just for the day considered (typically today) as things could change, so the scheduling
    // should always be redone
    val schedulings = this.objects.filterIsInstance(EmailScheduling::class.java)
    return schedulings.filter { !it.blocked }
}

fun main(args: Array<String>) {
    try {
        val kbase = readKnowledgeBase(listOf(
                File("rules/generic.drl"),
                File("rules/book.drl")))
        val ksession = kbase.newKieSession()
        // typically we want to consider today but we may decide to schedule
        // emails in the future or we may want to run tests using a different date
        val dayToConsider = LocalDate.now()
        loadDataIntoSession(ksession, dayToConsider)

        ksession.fireAllRules()

        showSending(ksession)
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}