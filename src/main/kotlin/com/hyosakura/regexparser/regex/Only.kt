package com.hyosakura.regexparser.regex

import com.hyosakura.regexparser.automata.NFA

/**
 * @author LovesAsuna
 **/
class Only(private val pattern: AbstractPattern) : AbstractPattern() {
    override fun toNFA(): NFA {
        val empty = SingleChar(null)
        return Union(empty, pattern).toNFA()
    }

    override fun toString(): String {
        return if (pattern is SingleChar) {
            "$pattern?"
        } else {
            "($pattern)?"
        }
    }
}