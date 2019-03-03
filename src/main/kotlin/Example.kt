package com.sample

import java.math.BigDecimal

import org.drools.KnowledgeBase
import org.drools.KnowledgeBaseFactory

import org.drools.builder.KnowledgeBuilder
import org.drools.builder.KnowledgeBuilderError
import org.drools.builder.KnowledgeBuilderErrors
import org.drools.builder.KnowledgeBuilderFactory
import org.drools.builder.ResourceType

import org.drools.io.ResourceFactory
import org.drools.runtime.StatefulKnowledgeSession

//import com.sample.ItemCity.City
//import com.sample.ItemCity.Type
//import org.kie.internal.KnowledgeBaseFactory
//import org.kie.internal.builder.KnowledgeBuilder
//import org.kie.internal.builder.KnowledgeBuilderError
//import org.kie.internal.builder.KnowledgeBuilderErrors
//import org.kie.internal.builder.KnowledgeBuilderFactory
//import org.kie.internal.runtime.StatefulKnowledgeSession

import com.sun.javafx.css.StyleManager.getErrors
import org.kie.api.io.ResourceType.DRL
import org.kie.internal.persistence.jpa.JPAKnowledgeService.newStatefulKnowledgeSession

data class Team(val name: String, val lastYearRank: Int, val city: String, val stadium: String)

fun main(args: Array<String>) {
    try {

        // load up the knowledge base
        val kbase = readKnowledgeBase()
        val ksession = kbase.newStatefulKnowledgeSession()


        val teams = listOf(
                Team("Torino", 7, "Torino", "Grande Torino"),
                Team("Inter", 3, "Milano", "Meazza"),
                Team("Milan", 4, "Milano", "Meazza"))

        teams.forEach { ksession.insert(it) }

//            val item1 = ItemCity()
//            item1.setPurchaseCity(City.PUNE)
//            item1.setTypeofItem(Type.MEDICINES)
//            item1.setSellPrice(BigDecimal(10))
//            ksession.insert(item1)
//
//            val item2 = ItemCity()
//            item2.setPurchaseCity(City.PUNE)
//            item2.setTypeofItem(Type.GROCERIES)
//            item2.setSellPrice(BigDecimal(10))
//            ksession.insert(item2)
//
//            val item3 = ItemCity()
//            item3.setPurchaseCity(City.NAGPUR)
//            item3.setTypeofItem(Type.MEDICINES)
//            item3.setSellPrice(BigDecimal(10))
//            ksession.insert(item3)
//
//            val item4 = ItemCity()
//            item4.setPurchaseCity(City.NAGPUR)
//            item4.setTypeofItem(Type.GROCERIES)
//            item4.setSellPrice(BigDecimal(10))
//            ksession.insert(item4)

        ksession.fireAllRules()

//            println(item1.getPurchaseCity().toString() + " "
//                    + item1.getLocalTax().intValue())
//
//            println(item2.getPurchaseCity().toString() + " "
//                    + item2.getLocalTax().intValue())
//
//            println(item3.getPurchaseCity().toString() + " "
//                    + item3.getLocalTax().intValue())
//
//            println(item4.getPurchaseCity().toString() + " "
//                    + item4.getLocalTax().intValue())

    } catch (t: Throwable) {
        t.printStackTrace()
    }

}

private fun readKnowledgeBase(): KnowledgeBase {

    val kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder()

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
