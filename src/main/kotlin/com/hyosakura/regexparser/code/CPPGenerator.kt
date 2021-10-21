package com.hyosakura.regexparser.code

import com.hyosakura.regexparser.automata.AutoMata
import com.hyosakura.regexparser.automata.Edge
import com.hyosakura.regexparser.automata.State
import java.util.stream.Collectors

/**
 * @author LovesAsuna
 **/
class CPPGenerator : CodeGenerator {
    private val builder = StringBuilder()

    override fun generate(automata: AutoMata<State, Edge>): String {
        generateStart()
        generateBinarySearch()
        generateMain(automata)
        return builder.toString()
    }

    private fun generateStart() {
        builder.append(
            """
            #include <iostream>
            #include <string>

            using namespace std;


        """.trimIndent()
        )
    }

    private fun generateBinarySearch() {
        builder.append(
            """
                bool find(int target, const int array[], int length) {
                    if (length == 0) {
                        return true;
                    }
                    int left = 0;
                    int right = length;
                    // [left, right)
                    while (left < right) {
                        int mid = left + (right - left) / 2;
                        if (array[mid] >= target) {
                            right = mid;
                        } else if (array[mid] < target) {
                            left = mid + 1;
                        }
                    }
                    if (left >= length || array[left] != target) {
                        return false;
                    }
                    return true;
                }

                int *cast(const string &str) {
                    const char *data = str.c_str();
                    int length = str.length();
                    int *array = new int[length];
                    for (int i = 0; i < length; i++) {
                        array[i] = data[i];
                    }
                    return array;
                }


            """.trimIndent()
        )
    }

    private fun <T> collectionToArray(collection: Collection<T>): String {
        return collection
            .toString()
            .replace("[", "{")
            .replace("]", "}")
    }

    private fun generateMain(automata: AutoMata<State, Edge>) {
        val accept = automata.acceptStates
        builder.append(
            """
int main() {
    char ch;
    int state = 1;
    int accept[${accept.size}] = ${collectionToArray(accept.stream().map { it.id }.sorted().collect(Collectors.toList()))};
    while (cin.get(ch) && ch != '\n') {
        ${generateWhile(automata)}
    }
    if (find(state, accept, ${accept.size})) {
        cout << "accept!" << endl;
    } else {
        cout << "error!";
    }
    return 0;
}
        """.trimIndent()
        )
    }

    private fun generateWhile(automata: AutoMata<State, Edge>): String {
        val builder = StringBuilder()
        for (id in automata.stateMap.values.stream().map { it.id }) {
            builder.append(generateCase(automata, id))
        }
        return """switch (state) {$builder
            default: {
                break;
            }
       }"""
    }

    private fun generateCase(automata: AutoMata<State, Edge>, id : Int): String {
        val edges = automata.getEdges(id)
        fun `if`(edge : Edge) : String {
            return """    
                if (find(ch, cast("${edge.symbol.joinToString("")}"), ${if (edge.symbol.contains(null)) 0 else edge.symbol.size})) {
                    state = ${edge.next};
                }
            """.trim()
        }
        fun elseIf(edge : Edge) : String {
            return """

|                else if (find(ch, cast("${edge.symbol.joinToString("")}"), ${if (edge.symbol.contains(null)) 0 else edge.symbol.size})) {
                    state = ${edge.next};
                }
            """.trimMargin()
        }
        fun `else`(next : Int) : String {
            return """

|                else {
                    state = $next;
                }
            """.trimMargin()
        }
        val builder = StringBuilder()
        val iterator = edges.iterator()
        if (iterator.hasNext()) {
            builder.append(`if`(iterator.next()))
        }
        if (edges.size > 1) {
            while (iterator.hasNext()) {
                builder.append(elseIf(iterator.next()))
            }
            builder.append(`else`(-1))
        }
        return """
            case $id: {
                $builder
                break;
            }"""
    }
}