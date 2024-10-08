package org.cubewhy.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

interface Store {
    fun send(action: Action)
    fun removeAll()

    val stateFlow: StateFlow<State>
    val state get() = stateFlow.value
}

fun CoroutineScope.createStore(): Store {
    val mutableStateFlow = MutableStateFlow(State())
    val channel: Channel<Action> = Channel(Channel.UNLIMITED)

    return object : Store {
        init {
            launch {
                channel.consumeAsFlow().collect { action ->
                    mutableStateFlow.value = chatReducer(mutableStateFlow.value, action)
                }
            }
        }

        override fun send(action: Action) {
            launch {
                channel.send(action)
            }
        }

        override fun removeAll() {
            mutableStateFlow.value = mutableStateFlow.value.copy(messages = emptyList())
        }

        override val stateFlow: StateFlow<State> = mutableStateFlow
    }
}