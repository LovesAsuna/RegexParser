package com.hyosakura.regexparser.regex

import com.hyosakura.regexparser.automata.AbstractBuilder
import com.hyosakura.regexparser.automata.NFA
import java.util.stream.Collectors

/**
 * @author LovesAsuna
 **/
class Concat(private val first: AbstractPattern, private val second: AbstractPattern) : AbstractPattern() {
    override fun toNFA(): NFA {
        val firstNFA = first.toNFA()
        val secondNFA = second.toNFA()
        val builder = AbstractBuilder.NFABuilder()
        builder.addFromAutoMata(firstNFA, -1)
            .addFromAutoMata(secondNFA, firstNFA.size)
            .setStartState(firstNFA.startState.id)
            .setAcceptStates(secondNFA.acceptStates.stream().map {
                it.id
            }.collect(Collectors.toList()))
        firstNFA.acceptStates.forEach {
            builder.addEdge(it.id, setOf(null), secondNFA.startState.id)
        }
        return builder.build()
    }

    override fun toString(): String {
        return "${first}${second}"
    }
}