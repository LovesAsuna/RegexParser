package com.hyosakura.regexparser.regex

import com.hyosakura.regexparser.automata.NFA

/**
 * @author LovesAsuna
 **/
class Choose(private val patterns: List<AbstractPattern>) : AbstractPattern() {
    override fun toNFA(): NFA {
        if (patterns.size < 2) return patterns[0].toNFA()
        var index = 2
        var union = Union(patterns[0], patterns[1])
        while (index < patterns.size) {
            union = Union(union, patterns[index])
            index++
        }
        return union.toNFA()
    }

    override fun toString(): String {
        return if (patterns.size == 1) {
            "${patterns[0]}"
        } else {
            val builder = StringBuilder()
            for (pattern in patterns) {
                builder.append(pattern.toString())
            }
            "[$builder]"
        }
    }
}