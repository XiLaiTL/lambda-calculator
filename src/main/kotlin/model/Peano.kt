package model

import model.Action.BetaReduce.Companion.betaReduce
import model.Action.Normalize.Companion.normalize

class Peano(
    val identifier: String,
    val zero: Value,
    val succ: Value,
): ScopeValue{
    private val cache:MutableMap<Int,Value> = mutableMapOf(0 to zero)
    operator fun get(n:Int):Value{
        if(cache.containsKey(n)) return cache[n]?:zero;
        for(i in 1 ..  n){
            if(!cache.containsKey(i)){
                cache[i]= if(succ is Value.Function) succ.betaReduce(cache[i-1]?:zero).normalize()
                    else Value.Application.create(succ,cache[i-1]?:zero)
            }
        }
        return cache[n]?:zero
    }

    companion object{
        fun Scope.getPeano(identifier: String,number:Int) = when(val scopeValue = this[identifier]){
            is Peano->scopeValue[number]
            else->null
        }
    }
}