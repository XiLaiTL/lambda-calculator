package tree

import model.Action.Companion.collect
import kotlin.reflect.KClass

class LambdaReduceException : RuntimeException {
    constructor() : super() {}
    constructor(message: String) : super(message) {
        collect(message)
    }

    companion object {
        fun <T : Any> at(e: KClass<T>): LambdaReduceException {
            return LambdaReduceException(e::qualifiedName.name)
        }
    }
}