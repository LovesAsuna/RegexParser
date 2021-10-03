package com.hyosakura.regexparser.regex

import com.hyosakura.regexparser.automata.AbstractBuilder
import com.hyosakura.regexparser.automata.NFA

/**
 * @author LovesAsuna
 **/
class Union(private val first: AbstractPattern, private val second: AbstractPattern) : AbstractPattern() {
    override fun toNFA(): NFA {
        val firstNFA = first.toNFA()
        val secondNFA = second.toNFA()
        val builder = AbstractBuilder.NFABuilder()
        val startID = 1
        builder.addFromAutoMata(firstNFA, startID)
            .addFromAutoMata(secondNFA, startID + firstNFA.size)
        val endID = startID + firstNFA.size + secondNFA.size + 1
        builder.setStartState(startID)
            .setAcceptStates(listOf(endID))
        builder.addEdge(startID, setOf(null), firstNFA.startState.id)
        builder.addEdge(startID, setOf(null), secondNFA.startState.id)
        firstNFA.acceptStates.forEach {
            builder.addEdge(it.id, setOf(null), endID)
        }
        secondNFA.acceptStates.forEach {
            builder.addEdge(it.id, setOf(null), endID)
        }
        return builder.build()
    }

    override fun toString(): String {
        return "$first|$second"
    }
}