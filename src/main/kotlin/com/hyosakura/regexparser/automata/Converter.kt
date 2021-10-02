package com.hyosakura.regexparser.automata

import java.util.*
import java.util.stream.Collectors

/**
 * @author LovesAsuna
 **/
interface Converter {
    fun convert(automata: AutoMata<State, Edge>): AutoMata<State, Edge>

    fun minimize(automata: AutoMata<State, Edge>): AutoMata<State, Edge>

    class AttachDFA(
        startState: State,
        acceptStates: Set<State>,
        stateMap: Map<Int, State>,
        val attachMap: Map<Int, Set<Int>>
    ) : DFA(
        startState,
        acceptStates,
        stateMap
    )

    // default converter: convert NFA to DFA(might not minimize DFA)
    class DefaultConverter : Converter {
        private var idGenerator: Int = 1
        private val attachMap = mutableMapOf<Int, Set<Int>>()
        private val reversedMap = mutableMapOf<Set<Int>, Int>()

        override fun convert(automata: AutoMata<State, Edge>): AutoMata<State, Edge> {
            automata as NFA
            val builder = object : AbstractBuilder<AttachDFA>() {
                override fun build(): AttachDFA = AttachDFA(
                    startState,
                    acceptStates,
                    stateMap,
                    attachMap
                )
            }
            // calculate available char
            val availableChar = getAvailableChar(automata)
            val start = StateWrapper(idGenerator++, automata.getFreeStates(mutableSetOf(automata.startState)), automata)
            val acceptStates = mutableSetOf<Int>()
            builder.setStartState(start.id)
            attachWrapper(attachMap, start)
            val queue = LinkedList<StateWrapper>() as Queue<StateWrapper>
            val allWrapper = mutableSetOf<StateWrapper>()
            allWrapper.add(start)
            // start BFS
            queue.offer(start)
            while (true) {
                val wrapper = queue.poll()
                var hasNew = false
                for (c in availableChar) {
                    val transferWrapper = getTransferWrapper(wrapper, c, automata)
                    if (!allWrapper.contains(transferWrapper)) {
                        builder.addEdge(wrapper.id, c, transferWrapper.id)
                        attachWrapper(attachMap, transferWrapper)
                        attachMap[transferWrapper.id]?.also {
                            if (it canRetain automata.acceptStates) acceptStates.add(transferWrapper.id)
                        }
                        allWrapper.add(transferWrapper)
                        hasNew = true
                        queue.offer(transferWrapper)
                    } else {
                        builder.addEdge(wrapper.id, c, transferWrapper.id)
                    }
                }
                if (!hasNew) break
            }
            return builder.setAcceptStates(acceptStates).build()
        }

        override fun minimize(automata: AutoMata<State, Edge>): AutoMata<State, Edge> {
            TODO("Not yet implemented")
        }

        private fun getIDSetFromWrapper(wrapper: StateWrapper): Set<Int> =
            wrapper.states.stream().map { it.id }.collect(Collectors.toSet())

        private fun getIDSetFromStates(states: Set<State>): Set<Int> =
            states.stream().map { it.id }.collect(Collectors.toSet())

        private fun attachWrapper(attachMap: MutableMap<Int, Set<Int>>, wrapper: StateWrapper) {
            val set = getIDSetFromWrapper(wrapper)
            attachMap[wrapper.id] = set
            reversedMap[set] = wrapper.id
        }

        private infix fun Set<Int>.canRetain(other: Set<State>): Boolean {
            val set = LinkedHashSet(this)
            return set.let {
                it.retainAll(getIDSetFromStates(other))
                it.size > 0
            }
        }

        private fun getAvailableChar(automata: NFA): List<List<Char?>> {
            return automata.stateMap.values.stream().flatMap {
                it.edges.stream()
            }.map {
                it.symbol
            }.filter {
                !it.contains(null)
            }.collect(Collectors.toList())
        }

        private fun getTransferWrapper(wrapper: StateWrapper, symbol: List<Char?>, automata: NFA): StateWrapper {
            val transferSet = mutableSetOf<State>()
            for (state in wrapper.states) {
                for (c in symbol) {
                    transferSet.addAll(automata.getFreeStates(automata.nextStates(state, c).toMutableSet()))
                }
            }
            val id = reversedMap[getIDSetFromStates(transferSet)]
            return if (id != null) {
                StateWrapper(id, transferSet, automata)
            } else {
                StateWrapper(idGenerator++, transferSet, automata)
            }
        }
    }
}

class StateWrapper(
    val id: Int,
    val states: Set<State>,
    automata: NFA
) {
    var accept = false

    init {
        val temp = HashSet(states)
        accept = temp.also { it.retainAll(automata.acceptStates) }.size > 0
    }

    override fun toString(): String {
        return "accept: $accept, state: $states"
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