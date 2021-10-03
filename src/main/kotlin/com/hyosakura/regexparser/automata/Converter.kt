package com.hyosakura.regexparser.automata

import java.util.*
import java.util.stream.Collectors

/**
 * @author LovesAsuna
 **/
interface Converter {
    fun convert(automata: AutoMata<State, Edge>): AutoMata<State, Edge>

    fun minimize(automata: AutoMata<State, Edge>): AutoMata<State, Edge>

    // default converter: convert NFA to DFA(might not minimize DFA)
    class DefaultConverter : Converter {
        private var idGenerator: Int = 1
        private val reversedMap = mutableMapOf<Set<State>, Int>()

        override fun convert(automata: AutoMata<State, Edge>): AutoMata<State, Edge> {
            automata as NFA
            val builder = object : AbstractBuilder<DFA>(
                stateCreator = { id, edges ->
                    StateWrapper(id, mutableSetOf(), edges)
                }
            ) {
                fun addState(wrapper: StateWrapper): AbstractBuilder<DFA> {
                    stateMap.putIfAbsent(wrapper.id, wrapper)
                    computeAccept(wrapper.id)
                    return this
                }

                fun contains(state: State): Boolean = stateMap.values.contains(state)

                fun computeAccept(id: Int): AbstractBuilder<DFA> {
                    val wrapper = stateMap[id] as StateWrapper
                    wrapper.accept = LinkedList(wrapper.states).also { it.retainAll(automata.acceptStates) }.size > 0
                    return this
                }

                override fun build(): DFA = DFA(
                    startState,
                    acceptStates,
                    stateMap
                )
            }
            // calculate available char
            val availableChar = getAvailableChar(automata)
            // init start state
            val start =
                StateWrapper(idGenerator++, automata.getFreeStates(mutableSetOf(automata.startState)).toMutableSet())
            builder.addState(start).setStartState(start.id)
            reversedMap[start.states] = start.id
            val acceptStates = mutableSetOf<Int>()
            if (start.accept) {
                acceptStates.add(start.id)
            }
            // start BFS
            val queue = LinkedList<State>() as Queue<State>
            queue.offer(start)
            while (true) {
                val wrapper = queue.poll()
                var hasNew = false
                for (c in availableChar) {
                    val transferWrapper = getTransferWrapper(wrapper as StateWrapper, c, automata)
                    if (!builder.contains(transferWrapper)) {
                        hasNew = true
                        builder.addState(transferWrapper)
                        if (transferWrapper.accept) {
                            acceptStates.add(transferWrapper.id)
                        }
                        queue.offer(transferWrapper)
                    }
                    builder.addEdge(wrapper.id, c, transferWrapper.id)
                }
                if (!hasNew) break
            }
            return builder.setAcceptStates(acceptStates).build()
        }

        override fun minimize(automata: AutoMata<State, Edge>): AutoMata<State, Edge> {
            // automata must be a DFA
            val builder = AbstractBuilder.DFABuilder()
            val transferTable = mutableMapOf<MutableMap<Set<Char?>, Boolean>, PriorityQueue<Int>>()
            // construct the transfer table
            for ((index, state) in automata.stateMap) {
                val map = mutableMapOf<Set<Char?>, Boolean>()
                for (edge in state.edges) {
                    val transferState = automata.nextStates(state, edge.symbol).first() as AttachState
                    map[edge.symbol] = transferState.accept
                }
                transferTable.computeIfAbsent(map) { PriorityQueue() }.add(index)
            }
            // start build minimize DFA

            return builder.setStartState(automata.startState.id).setAcceptStates(setOf(1)).build()
        }

        private fun getAvailableChar(automata: AutoMata<State, Edge>): Set<Set<Char?>> {
            return automata.stateMap.values.stream().flatMap {
                it.edges.stream()
            }.map {
                it.symbol
            }.filter {
                !it.contains(null)
            }.collect(Collectors.toSet())
        }

        private fun getTransferWrapper(wrapper: StateWrapper, symbol: Set<Char?>, automata: NFA): StateWrapper {
            val transferSet = mutableSetOf<State>()
            for (state in wrapper.states) {
                for (c in symbol) {
                    transferSet.addAll(automata.getFreeStates(automata.nextStates(state, c).toMutableSet()))
                }
            }
            val id = reversedMap[transferSet]
            return if (id != null) {
                StateWrapper(id, transferSet)
            } else {
                StateWrapper(idGenerator++, transferSet).also {
                    reversedMap[transferSet] = it.id
                }
            }
        }
    }
}

class StateWrapper(
    override var id: Int,
    override val states: MutableSet<State>,
    override val edges: MutableList<Edge> = mutableListOf(),
    override var accept: Boolean = false
) : AttachState {

    override fun toString(): String {
        return "StateWrapper{id: $id, accept: $accept, state: $states}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StateWrapper

        if (states != other.states) return false
        return true
    }

    override fun hashCode(): Int {
        return states.hashCode()
    }
}