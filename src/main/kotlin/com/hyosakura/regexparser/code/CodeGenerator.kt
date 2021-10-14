package com.hyosakura.regexparser.code

import com.hyosakura.regexparser.automata.AutoMata
import com.hyosakura.regexparser.automata.Edge
import com.hyosakura.regexparser.automata.State

/**
 * @author LovesAsuna
 **/
interface CodeGenerator {
    fun generate(automata : AutoMata<State, Edge>) : String
}