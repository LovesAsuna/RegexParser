package com.hyosakura.regexparser.automata

/**
 * @author LovesAsuna
 **/
open class NFA(
    startState: State,
    acceptStates: Set<State>,
    stateMap: Map<Int, State>
) : AbstractAutoMata(startState, acceptStates, stateMap) {
    private var currentStates: Set<State> = getFreeStates(mutableSetOf(startState))

    override fun nextStates(state: State, c: Char?): Set<State> {
        val next = mutableSetOf<State>()
        for (edge in state.edges) {
            if (c in edge.symbol) {
                next.addAll(nextStates(state, edge.symbol))
            }
        }
        return next
    }

    fun getFreeStates(states: MutableCollection<State>): Set<State> {
        val stateSet = nextStates(states, null)
        return if (states.containsAll(stateSet)) {
            HashSet(states)
        } else {
            states.addAll(stateSet)
            getFreeStates(states)
        }
    }

    override fun test(string: String): Boolean {
        var states = currentStates
        for (c in string) {
            states = getFreeStates(nextStates(states, c).toMutableSet())
        }
        for (state in states) {
            if (acceptStates.contains(state)) {
                return true
            }
        }
        return false
    }

    override fun toString(): String = "NFA"
}