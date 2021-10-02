package com.hyosakura.regexparser.regex.parser


/**
 * @author LovesAsuna
 **/
class Token(val tokenType: TokenType, val value: Char) {
    enum class TokenType {
        CHAR,
        LEFT_PAREN,
        RIGHT_PAREN,
        LEFT_SQUARE,
        RIGHT_SQUARE,
        UNION,
        CLOSURE,
        POSITIVE_CLOSURE,
        ONLY,
        ESCAPE
    }

    override fun toString(): String = value.toString()

    companion object {
        val LEFT_PAREN = Token(TokenType.LEFT_PAREN, '(')
        val RIGHT_PAREN = Token(TokenType.RIGHT_PAREN, ')')
        val LEFT_SQUARE = Token(TokenType.LEFT_SQUARE, '[')
        val RIGHT_SQUARE = Token(TokenType.RIGHT_SQUARE, ']')
        val ONLY = Token(TokenType.ONLY, '?')
        val UNION = Token(TokenType.UNION, '|')
        val CLOSURE = Token(TokenType.CLOSURE, '*')
        val POSITIVE_CLOSURE = Token(TokenType.POSITIVE_CLOSURE, '+')
        val ESCAPE = Token(TokenType.ESCAPE, '\\')
    }
}