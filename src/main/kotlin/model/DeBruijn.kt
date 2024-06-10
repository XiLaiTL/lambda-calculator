package model

sealed class DeBruijn:ScopeValue {
    data class Application(val function: DeBruijn,val argument:DeBruijn): DeBruijn(){
        override fun toString(): String {
            val argumentString = when(this.argument){
                is Application, is Function ->"($argument)"
                else ->"$argument"
            }
            val functionString = when(this.function){
                is Function -> "($function)"
                else ->"$function"
            }
            return "$functionString $argumentString"

        }
    }
    data class Function(val body:DeBruijn): DeBruijn(){
        override fun toString(): String = "λ.$body"
    }
    data class Identifier(val identifier:String):DeBruijn(){
        override fun toString(): String = identifier
    }
    data class Replace(val expression:DeBruijn,val argument:DeBruijn,val replaceHolder:Identifier):DeBruijn(){
        override fun toString(): String = "$expression[$argument/$replaceHolder]"
    }

    companion object{
        private var scope = Scope()

        fun Scope.getDeBruijnLevel(name:String):Int?{
            val cur = getScopeInner().size - 1
            for (i in getScopeInner().indices.reversed()) {
                if (this.getScopeInner()[i].containsKey(name)) {
                    return cur-i
                }
            }
            return null
        }

        fun Value.toDeBruijnIndex():DeBruijn= when(this){
            is Value.Application->Application(this.function.toDeBruijnIndex(),this.argument.toDeBruijnIndex())
            is Value.ExpressionIdentifier -> Identifier(this.identifier)
            is Value.Function -> {
                scope.pushStack()
                scope[this.parameter.identifier] = Identifier(this.parameter.identifier)
                val bodyNew = this.body.toDeBruijnIndex()
                scope.popStack()
                Function(bodyNew)
            }
            is Value.Identifier -> {
                val level = scope.getDeBruijnLevel(this.identifier)
                Identifier("${level?:this.identifier}")
            }
            is Value.Replace -> Replace(expression.toDeBruijnIndex(), argument.toDeBruijnIndex(),replaceHolder.toDeBruijnIndex() as Identifier)
            is Fix -> toFullFunction().toDeBruijnIndex()
            is ActionFunction -> Identifier(this.name)
        }

    }

}