package model

import kotlin.collections.ArrayList

interface ScopeValue

class Scope {
    private val stack: MutableList<MutableMap<String,ScopeValue>> =ArrayList(listOf(HashMap()))
    fun getScopeInner() = stack

    fun clear(){
        stack.clear()
        stack.add(HashMap())
    }
    fun pushStack() {
        stack.add(HashMap())
    }

    fun popStack() {
        if (stack.size > 1) {
            stack.removeAt(stack.size - 1)
        }
    }

    operator fun set(name: String, value: ScopeValue) {
        if (containsCurrent(name)) {
            throw RuntimeException("declare twice")
        } else {
            stack[stack.size - 1][name] = value
        }
    }

    operator fun get(name: String): ScopeValue? {
        for (i in stack.indices.reversed()) {
            if (stack[i].containsKey(name)) {
                return stack[i][name]
            }
        }
        return null
    }

    fun getOrThrow(name: String): ScopeValue {
        return get(name) ?: throw RuntimeException("not such identifier")
    }

    // 查询是否重复声明？
    fun containsCurrent(name: String): Boolean {
        return stack[stack.size - 1].containsKey(name)
    }

    fun checkDuplication(name: String) {
        if (containsCurrent(name)) {
            throw RuntimeException("duplication error")
        }
    }

    val isGlobal: Boolean
        get() = stack.size == 1
}

