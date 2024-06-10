package model

sealed class Value:ScopeValue {

    override fun equals(other: Any?): Boolean = this.toString() == other.toString()
    companion object{
        fun Scope.getValue(name:String):Value? = when(val scopeValue=this[name]){
            is Value->scopeValue
            else->null
        }

        fun Lambda(parameter:Identifier, body: (Identifier) -> Value) = Function.create(parameter,body)
        fun Lambda(parameter:String,body: (Identifier) -> Value) = Function.create(Identifier(parameter),body)

        fun Apply(function: Value,argument: Value) = Application.create(function,argument)
    }

    sealed class Application(
        open val function: Value,
        open val argument: Value
    ):Value(){

        companion object {
            fun create(function: Value,argument: Value):Application{
                return when(function){
                    is Function->FunctionApplication(function,argument)
                    is Identifier->if(argument is Identifier) IdentifierApplication(function,argument)
                    else NormalApplication(function,argument)
                    else -> NormalApplication(function,argument)
                }
            }
        }

        override fun toString(): String = when(this.argument){
            is Application ->"$function ($argument)"
            else ->"$function $argument"
        }

        data class NormalApplication(
            override val function: Value,
            override val argument: Value
        ): Application(function,argument){
            override fun toString(): String = super<Application>.toString()
        }

        data class FunctionApplication(
            override val function: Function,
            override val argument: Value
        ):Application(function,argument){
            override fun toString(): String = super<Application>.toString()
        }

        data class IdentifierApplication(
            override val function: Identifier,
            override val argument: Identifier
        ):Application(function,argument){
            override fun toString(): String = super<Application>.toString()
        }
    }

    data class Identifier(
        val originIdentifier:String
    ):Value() {
        var identifier:String = originIdentifier
        fun restore(){ identifier = originIdentifier }
        fun rename(newName:String){ identifier = newName }

        override fun hashCode(): Int = originIdentifier.hashCode()
        override fun equals(other: Any?): Boolean =
            other is Identifier && this.identifier == other.identifier

        override fun toString(): String = identifier

        companion object{
            private var count = 0
            fun resetCount(){count=0}
            fun tempName() = "T_{${count++}}"
            fun tempIdentifier() = Identifier(tempName())
        }
    }

    data class ExpressionIdentifier(
        val identifier:String
    ):Value() {
        override fun toString(): String = identifier
    }

    data class Replace(
        val expression : Value,
        val argument: Value,
        val replaceHolder: Identifier
    ): Value() {
        override fun toString(): String = when(expression){
            is Application-> "($expression)[$argument/$replaceHolder]"
            else-> "$expression[$argument/$replaceHolder]"
        }
    }


    data class Function(
        val parameter: Identifier,
        val body: Value
    ): Value() {

        override fun toString(): String = "(λ${parameter}.${body})"
        companion object{
            fun create(parameter: Identifier, body: (Identifier)->Value):Function{
                return Function(parameter,body(parameter))
            }
        }
    }

}