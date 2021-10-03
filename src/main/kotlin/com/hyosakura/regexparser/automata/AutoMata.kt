package com.hyosakura.regexparser.automata

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author LovesAsuna
 **/
interface AutoMata<T : State, V : Edge> {
    var startState: T

    val acceptStates: Set<T>

    var stateMap: Map<Int, T>

    val size: Int
        get() = stateMap.size

    fun getState(id: Int): T

    fun getEdges(id: Int): Set<V>

    fun nextStates(state: T, c: Char?): Set<T>

    fun nextStates(state: T, cList: Set<Char?>): Set<T> {
        val next = mutableSetOf<T>()
        for (edge in state.edges) {
            if (cList == edge.symbol) {
                next.add(getState(edge.next))
            }
        }
        return next
    }

    fun test(string: String): Boolean

    companion object {
        val log: Logger = LoggerFactory.getLogger(AutoMata::class.java)
    }
}

fun AutoMata<State, Edge>.offset(value: Int) {
    AutoMata.log.debug("$this offset $value")
    val map = hashMapOf<Int, State>()
    val states = stateMap.values
    states.forEach { state ->
        state.id += value
        state.edges.forEach { edge ->
            edge.next += value
        }
        map[state.id] = state
    }
    stateMap = map
}

interface State {
    var id: Int
    val edges: MutableList<Edge>
}

interface AttachState : State {
    val states: MutableSet<State>
    var accept : Boolean
}

data class InternalState(
    override var id: Int,
    override val edges: MutableList<Edge> = mutableListOf()
) : State {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InternalState

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "state{id: $id}"
    }
}

interface Edge {
    val symbol: Set<Char?>
    var next: Int
}

data class InternalEdge(override val symbol: Set<Char?>, override var next: Int) : Edge

interface AutoMataBuilder<T : AutoMata<State, Edge>> {
    fun setStartState(stateID: Int): AutoMataBuilder<T>

    fun setAcceptStates(statesID: Collection<Int>): AutoMataBuilder<T>

    fun addState(stateID: Int): AutoMataBuilder<T>

    fun addEdge(fromStateID: Int, acceptChar: Set<Char?>, toStateID: Int): AutoMataBuilder<T>

    fun addFromAutoMata(autoMata: AutoMata<State, Edge>, offset: Int): AutoMataBuilder<T>

    fun build(): T
}

abstract class AbstractBuilder<T : AutoMata<State, Edge>>(
    val stateCreator: (id: Int, edges: MutableList<Edge>) -> State = { id, edges ->
        InternalState(id, edges)
    },
    val edgeCreator: (symbol: Set<Char?>, next: Int) -> Edge = { symbol, next ->
        InternalEdge(symbol, next)
    }
) : AutoMataBuilder<T> {
    private var maxID = 0
    protected val stateMap = mutableMapOf<Int, State>()
    protected lateinit var startState: State
    protected lateinit var acceptStates: MutableSet<State>

    override fun setStartState(stateID: Int): AbstractBuilder<T> {
        startState = stateMap.computeIfAbsent(stateID) {
            log.debug("set start state to $it")
            stateCreator(stateID, mutableListOf())
        }
        return this
    }

    override fun setAcceptStates(statesID: Collection<Int>): AbstractBuilder<T> {
        acceptStates = hashSetOf()
        for (id in statesID) {
            val state = stateMap.computeIfAbsent(id) {
                log.debug("add state $it to acceptSet")
                stateCreator(id, mutableListOf())
            }
            acceptStates.add(state)
        }
        return this
    }

    override fun addState(stateID: Int): AbstractBuilder<T> {
        if (stateMap.putIfAbsent(stateID, stateCreator(stateID, mutableListOf())) == null) {
            maxID = maxID.coerceAtLeast(stateID)
            log.debug("add State{$stateID}")
        }
        return this
    }

    override fun addEdge(fromStateID: Int, acceptChar: Set<Char?>, toStateID: Int): AbstractBuilder<T> {
        val from = stateMap.computeIfAbsent(fromStateID) { stateCreator(fromStateID, mutableListOf()) }
        stateMap.computeIfAbsent(toStateID) { stateCreator(toStateID, mutableListOf()) }
        maxID = maxID.coerceAtLeast(fromStateID).coerceAtLeast(toStateID)
        from.edges.add(edgeCreator(acceptChar, toStateID))
        log.debug("add Edge{from $fromStateID to $toStateID}")
        return this
    }

    override fun addFromAutoMata(autoMata: AutoMata<State, Edge>, offset: Int): AbstractBuilder<T> {
        autoMata.offset(maxID.coerceAtLeast(offset))
        autoMata.stateMap.values.forEach { state ->
            addState(state.id)
            state.edges.forEach { edge ->
                addEdge(state.id, edge.symbol, edge.next)
            }
        }
        return this
    }

    abstract override fun build(): T

    companion object {
        private val log = LoggerFactory.getLogger(AbstractBuilder::class.java)
    }

    class DFABuilder : AbstractBuilder<DFA>() {
        override fun build(): DFA = DFA(startState, acceptStates, stateMap)
    }

    class NFABuilder : AbstractBuilder<NFA>() {
        override fun build(): NFA = NFA(startState, acceptStates, stateMap)
    }
}
