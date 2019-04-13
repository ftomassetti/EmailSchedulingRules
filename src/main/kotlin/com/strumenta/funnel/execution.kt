package com.strumenta.funnel

import org.drools.core.impl.InternalKnowledgeBase
import org.drools.core.impl.KnowledgeBaseFactory
import org.kie.api.io.ResourceType
import org.kie.internal.builder.KnowledgeBuilderFactory
import org.kie.internal.io.ResourceFactory
import java.io.File
import java.time.LocalDate


fun showSending(emailScheduler: EmailScheduler) {
    println("Showing email scheduling")
    emailScheduler.selectScheduling(LocalDate.now()).forEach {
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

fun main(args: Array<String>) {
    try {
        val kbase = readKnowledgeBase(listOf(
                File("rules/generic.drl"),
                File("rules/book.drl")))
        val ksession = kbase.newKieSession()
        // typically we want to consider today but we may decide to schedule
        // emails in the future or we may want to run tests using a different date
        val dayToConsider = LocalDate.now()
        val emailScheduler = loadDataIntoSession(ksession, dayToConsider)

        ksession.fireAllRules()

        showSending(emailScheduler)
    } catch (t: Throwable) {
        t.printStackTrace()
    }
}