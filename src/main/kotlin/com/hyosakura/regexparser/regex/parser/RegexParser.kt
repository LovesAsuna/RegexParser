package com.hyosakura.regexparser.regex.parser

import com.hyosakura.regexparser.regex.*
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author LovesAsuna
 **/
class RegexParser(patternString: String) {
    private val tokenizer: Tokenizer = Tokenizer(patternString)
    private val patternStack: Stack<AbstractPattern> = Stack()
    private val opStack: Stack<Char> = Stack()
    private var bracket = false
    private var choose = false
    private var numInBracket = 0
    private var numInChoose = 0

    fun parse(): AbstractPattern {
        var token = advance()
        while (token != null) {
            when (token.tokenType) {
                Token.TokenType.LEFT_PAREN -> {
                    bracket = true
                    opStack.push(token.value)
                }
                Token.TokenType.RIGHT_PAREN -> {
                    if (opStack.isEmpty()) throw RuntimeException("Brackets do not match")
                    var op = opStack.pop()
                    if (op == '|') {
                        val after = patternStack.pop()
                        val pre = patternStack.pop()
                        patternStack.push(Union(pre, after))
                        op = opStack.pop()
                        numInBracket--
                    }
                    if (op != '(') throw RuntimeException("Brackets do not match")
                    concatAll(false)
                }
                Token.TokenType.LEFT_SQUARE -> {
                    choose = true
                    opStack.push(token.value)
                }
                Token.TokenType.RIGHT_SQUARE -> {
                    if (opStack.isEmpty()) throw RuntimeException("Square brackets do not match")
                    val op = opStack.pop()
                    if (op != '[') throw RuntimeException("Square brackets do not match")
                    chooseAll()
                }
                Token.TokenType.CLOSURE -> {
                    val topPattern = patternStack.pop()
                    val closure = Closure(topPattern)
                    patternStack.push(closure)
                }
                Token.TokenType.POSITIVE_CLOSURE -> {
                    val topPattern = patternStack.pop()
                    val positiveClosure = PositiveClosure(topPattern)
                    patternStack.push(positiveClosure)
                }
                Token.TokenType.ONLY -> {
                    val topPattern = patternStack.pop()
                    val only = Only(topPattern)
                    patternStack.push(only)
                }
                Token.TokenType.UNION -> {
                    opStack.push(token.value)
                }
                Token.TokenType.ESCAPE -> {
                    val next = advance() ?: throw RuntimeException("esc empty char")
                    patternStack.push(SingleChar(next.value))
                }
                Token.TokenType.CHAR -> {
                    if (opStack.isNotEmpty()) {
                        val topOP = opStack.peek()
                        if (topOP == '|') {
                            if (patternStack.isEmpty()) throw RuntimeException("The last character of union operation is empty")
                            val prePattern = patternStack.pop()
                            val union = Union(prePattern, SingleChar(token.value.let { if (it == '.') 0.toChar() else it }))
                            patternStack.push(union)
                            opStack.pop()
                            token = advance()
                            continue
                        }
                    }
                    if (choose && token.value == '-') {
                        val prePattern = patternStack.pop().toString()
                        // This basically does not happen
                        if (prePattern.length != 1) {
                            throw RuntimeException("Unknown error")
                        }
                        val pre = prePattern[0]
                        val after = advance()?.let {
                            if (it.tokenType == Token.TokenType.ESCAPE) {
                                advance()?.value ?: throw RuntimeException("Empty after hyphen")
                            } else {
                                if (!isAlphabet(it.value)) throw RuntimeException("Not a char after hyphen")
                                it.value
                            }
                        } ?: throw RuntimeException("Empty after hyphen")
                        if (after < pre) throw IllegalArgumentException("Range values reversed. Start char code is greater than end char code.")
                        val list = mutableListOf<SingleChar>()
                        (pre..after).forEach {
                            list.add(SingleChar(it))
                        }
                        patternStack.push(Choose(list))
                        choose = false
                        numInChoose = 0
                        token = advance()
                        continue
                    }
                    if (bracket) numInBracket++
                    if (choose) numInChoose++
                    patternStack.push(SingleChar(token.value.let { if (it == '.') 0.toChar() else it }))
                }
            }
            token = advance()
        }
        concatAll(true)
        if (patternStack.isEmpty()) throw IllegalArgumentException("wrong pattern string")
        return patternStack.pop()
    }

    private fun isAlphabet(c: Char): Boolean = (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9')

    private fun concatAll(force: Boolean) {
        var pattern: AbstractPattern
        while ((force && patternStack.size > 1) || numInBracket-- > 0) {
            if (patternStack.isNotEmpty()) {
                pattern = patternStack.pop()
                if (force || (patternStack.isNotEmpty() && numInBracket != 0)) {
                    val concat = Concat(patternStack.pop(), pattern)
                    patternStack.push(concat)
                } else {
                    patternStack.push(pattern)
                    break
                }
            }
        }
        bracket = false
    }

    private fun chooseAll() {
        if (!choose) return
        val patterns = mutableListOf<AbstractPattern>()
        while (numInChoose-- > 0) {
            patterns.add(patternStack.pop())
        }
        patternStack.push(Choose(patterns.reversed()))
        choose = false
    }

    private fun advance(): Token? = tokenizer.advance()

    companion object {
        val log = LoggerFactory.getLogger(RegexParser::class.java)
    }
}