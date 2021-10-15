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
            idGenerator = 1
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
                    if (transferWrapper != null) {
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
                }
                if (!hasNew) break
            }
            // self loop
            while (queue.isNotEmpty()) {
                val wrapper = queue.poll()
                for (c in availableChar) {
                    getTransferWrapper(wrapper as StateWrapper, c, automata)?.apply {
                        builder.addEdge(wrapper.id, c, id)
                    }
                }
            }
            return builder.setAcceptStates(acceptStates).build()
        }

        override fun minimize(automata: AutoMata<State, Edge>): AutoMata<State, Edge> {
            // automata must be a DFA
            if (automata !is DFA) throw IllegalArgumentException("Automata must be a DFA")
            val list: MutableList<Set<State>> = mutableListOf()
            val accept: Set<State> = automata.acceptStates
            val notAccept: Set<State> = automata.stateMap.values.subtract(accept)
            accept.apply {
                if (isNotEmpty()) {
                    list.addAll(group(accept, automata))
                }
            }
            notAccept.apply {
                if (isNotEmpty()) {
                    list.addAll(group(notAccept, automata))
                }
            }
            // merge similar states
            fun merge(stateSet: Set<State>): State {
                var minId = Int.MAX_VALUE
                val idSet = stateSet.stream().map {
                    minId = minId.coerceAtMost(it.id)
                    it.id
                }.collect(Collectors.toSet())
                val iterator = automata.stateMap.values.iterator() as MutableIterator
                while (iterator.hasNext()) {
                    val state = iterator.next()
                    for (edge in state.edges) {
                        if (edge.next in idSet) {
                            edge.next = minId
                        }
                    }
                    // remove redundant state
                    if (state.id != minId && state.id in idSet) {
                        iterator.remove()
                    }
                }
                return automata.getState(minId)
            }
            for (stateSet in list) {
                if (stateSet.size > 1) {
                    merge(stateSet)
                }
            }
            return automata
        }


        private fun group(stateSet: Set<State>, automata: AutoMata<State, Edge>): List<Set<State>> {
            val list = mutableListOf<Set<State>>()
            val availableChar = getAvailableChar(automata)
            val queue: Queue<Set<State>> = LinkedList()
            queue.offer(stateSet)
            for (char in availableChar) {
                for (i in 0 until queue.size) {
                    val groupList = groupSingleChar(queue.poll(), automata, char)
                    queue.addAll(groupList)
                }
            }
            return list.also {
                it.addAll(queue)
            }
        }

        private fun groupSingleChar(
            stateSet: Set<State>,
            automata: AutoMata<State, Edge>,
            char: Set<Char?>
        ): List<Set<State>> {
            val acceptSet = mutableSetOf<State>()
            val nonAcceptSet = mutableSetOf<State>()
            val emptySet = mutableSetOf<State>()
            for (state in stateSet) {
                val nextState = automata.nextStates(state, char).firstOrNull() as? AttachState
                if (nextState != null) {
                    if (nextState.accept) {
                        acceptSet.add(state)
                    } else {
                        nonAcceptSet.add(state)
                    }
                } else {
                    emptySet.add(state)
                }
            }
            return mutableListOf<Set<State>>().also {
                if (acceptSet.isNotEmpty()) {
                    it.add(acceptSet)
                }
                if (nonAcceptSet.isNotEmpty()) {
                    it.add(nonAcceptSet)
                }
                if (emptySet.isNotEmpty()) {
                    it.add(emptySet)
                }
            }
        }

        fun getAvailableChar(automata: AutoMata<State, Edge>): Set<Set<Char?>> {
            return automata.stateMap.values.stream().flatMap {
                it.edges.stream()
            }.map {
                it.symbol
            }.filter {
                !it.contains(null)
            }.collect(Collectors.toSet())
        }

        private fun getTransferWrapper(wrapper: StateWrapper, symbol: Set<Char?>, automata: NFA): StateWrapper? {
            val transferSet = mutableSetOf<State>()
            for (state in wrapper.states) {
                for (c in symbol) {
                    transferSet.addAll(automata.getFreeStates(automata.nextStates(state, c).toMutableSet()))
                }
            }
            if (transferSet.isEmpty()) {
                return null
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