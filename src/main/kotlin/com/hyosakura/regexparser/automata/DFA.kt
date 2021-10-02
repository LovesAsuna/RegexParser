package com.hyosakura.regexparser.automata

/**
 * @author LovesAsuna
 **/
open class DFA(
    startState: State,
    acceptStates: Set<State>,
    stateMap: Map<Int, State>
) : AbstractAutoMata(startState, acceptStates, stateMap) {
    private var currentState: State = startState

    override fun nextStates(state: State, c: Char?): Set<State> {
        for (edge in state.edges) {
            if (c in edge.symbol) {
                return setOf(getState(edge.next))
            }
        }
        return emptySet()
    }

    override fun test(string: String): Boolean {
        var state: State = currentState
        for (c in string) {
            state = nextStates(state, c).first()
        }
        return state in acceptStates
    }

    override fun toString(): String = "DFA"
}

