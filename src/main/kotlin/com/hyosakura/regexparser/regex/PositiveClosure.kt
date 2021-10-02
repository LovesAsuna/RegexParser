package com.hyosakura.regexparser.regex

import com.hyosakura.regexparser.automata.NFA

/**
 * @author LovesAsuna
 **/
class PositiveClosure(private val pattern: AbstractPattern) : AbstractPattern() {
    override fun toNFA(): NFA {
        val closure = Closure(pattern)
        val concat = Concat(pattern, closure)
        return concat.toNFA()
    }

    override fun toString(): String {
        return if (pattern is SingleChar) {
            "$pattern+"
        } else {
            "($pattern)+"
        }
    }
}