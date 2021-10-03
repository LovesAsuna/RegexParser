package com.hyosakura.regexparser.regex

import com.hyosakura.regexparser.automata.AbstractBuilder
import com.hyosakura.regexparser.automata.NFA

/**
 * @author LovesAsuna
 **/
class Closure(private val pattern: AbstractPattern) : AbstractPattern() {
    override fun toNFA(): NFA {
        val nfa = pattern.toNFA()
        val builder = AbstractBuilder.NFABuilder()
        val startID = 1
        builder.addFromAutoMata(nfa, startID)
        val endID = nfa.acceptStates.stream().map { it.id }.max { o1, o2 -> o1 - o2 }.get() + 1
        builder.setStartState(startID)
            .setAcceptStates(setOf(endID))
        nfa.acceptStates.forEach {
            builder.addEdge(it.id, setOf(null), nfa.startState.id)
            builder.addEdge(it.id, setOf(null), endID)
        }
        builder.addEdge(startID, setOf(null), nfa.startState.id)
        builder.addEdge(startID, setOf(null), endID)
        return builder.build()
    }

    override fun toString(): String {
        return if (pattern is SingleChar) {
            "$pattern*"
        } else {
            "($pattern)*"
        }
    }
}