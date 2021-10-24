package com.hyosakura.regexparser.regex

import com.hyosakura.regexparser.automata.AbstractBuilder
import com.hyosakura.regexparser.automata.NFA

/**
 * @author LovesAsuna
 **/
class SingleChar(private val c: Char?) : AbstractPattern() {
    override fun toNFA(): NFA {
        val builder = AbstractBuilder.NFABuilder()
        val start = 1
        val end = 2
        return builder.setStartState(start)
            .setAcceptStates(setOf(end))
            .addEdge(start, setOf(c), end)
            .build()
    }

    override fun toString(): String {
        return when (c) {
            null -> {
                ""
            }
            0.toChar() -> {
                "[.]"
            }
            else -> {
                c.toString()
            }
        }
    }
}