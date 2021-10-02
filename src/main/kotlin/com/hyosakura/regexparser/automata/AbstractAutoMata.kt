package com.hyosakura.regexparser.automata

/**
 * @author LovesAsuna
 **/
abstract class AbstractAutoMata(
    override var startState: State,
    override val acceptStates: Set<State>,
    override var stateMap: Map<Int, State>
) : AutoMata<State, Edge> {
    override fun getState(id: Int): State {
        return stateMap[id] ?: throw IllegalArgumentException("Id does not exist")
    }

    override fun getEdges(id: Int): Set<Edge> {
        val state = stateMap[id] ?: throw IllegalArgumentException("Id does not exist")
        return state.edges.toSet()
    }

    fun nextStates(states: Collection<State>, c: Char?): Set<State> {
        val stateSet = mutableSetOf<State>()
        for (state in states) {
            stateSet.addAll(nextStates(state, c))
        }
        return stateSet
    }
}