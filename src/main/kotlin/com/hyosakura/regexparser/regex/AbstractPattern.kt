package com.hyosakura.regexparser.regex

import com.hyosakura.regexparser.automata.NFA
import com.hyosakura.regexparser.regex.parser.RegexParser

/**
 * @author LovesAsuna
 **/
abstract class AbstractPattern {
    abstract fun toNFA(): NFA

    fun test(string: String): Boolean = toNFA().test(string)

    companion object {
        init {
            System.setProperty("log4j.skipJansi", "false")
        }

        fun compile(patternString: String): AbstractPattern  {
            try {
                val pattern = RegexParser(patternString).parse()
                RegexParser.log.info("parse completed: {}", pattern)
                return pattern
            } catch (e : Exception) {
                RegexParser.log.error(e.message, e)
                throw e
            }
        }
    }
}