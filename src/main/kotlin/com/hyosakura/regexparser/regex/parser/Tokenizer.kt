package com.hyosakura.regexparser.regex.parser

/**
 * @author LovesAsuna
 **/
class Tokenizer(private val string: String) {
    private var index = 0

    fun advance(): Token? {
        if (index == string.length) return null
        val c = string[index++]
        return when (c) {
            '(' -> Token.LEFT_PAREN
            ')' -> Token.RIGHT_PAREN
            '[' -> Token.LEFT_SQUARE
            ']' -> Token.RIGHT_SQUARE
            '*' -> Token.CLOSURE
            '+' -> Token.POSITIVE_CLOSURE
            '?' -> Token.ONLY
            '|' -> Token.UNION
            '\\' -> Token.ESCAPE
            else -> Token(Token.TokenType.CHAR, c)
        }
    }
}