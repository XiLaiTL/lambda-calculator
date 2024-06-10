package model

import model.Action.Replace.Companion.replace
import model.ExpandedValue.FixInstance
import model.Value.Companion.Apply
import model.Value.Companion.Lambda
import kotlin.reflect.KClass

object ExpandedValue {
    //Y = (\lambda g. (\lambda x. g (x x)) (\lambda x. g (x x)));
    val FixInstance = Lambda("g"){ g->
        Apply(
            Lambda("x"){x->
                Apply(g,Apply(x,x))
            },
            Lambda("x"){x->
                Apply(g,Apply(x,x))
            }
        )
    }

    val True = Lambda("f"){f->
        Lambda("x"){f}
    }

    val False = Lambda("f"){
        Lambda("x"){x->x}
    }
}



data class ActionFunction(
    val name:String="ACTION",
    val action:(Value)->Value
):Value(){

    fun run(value: Value):Value{
        return action(value)
    }

    override fun toString(): String = name
    companion object{
        val FixFunction = ActionFunction("FIX"){ Fix.create(it)}
        fun create(actionType: KClass< out Action>):Value{
            return when(actionType){
                Action.Activate::class->ActionFunction("ACT"){  Action.Activate(it).run()  }
                Action.Print::class-> ActionFunction("PRINT") {  Action.Print(it).run()  }
                Action.Normalize::class-> ActionFunction("NORMALIZE"){  Action.Normalize(it).run()  }
                Action.BetaReduce::class-> ActionFunction("\\beta") {  Action.BetaReduce(it).run()  }
                Action.AlphaReduce::class-> ActionFunction("\\alpha") { value1 -> ActionFunction{ value2-> if(value2 is Identifier) Action.AlphaReduce(value1,value2.identifier).run() else Action.AlphaReduce(value1).run() }    }
                Action.EtaReduce::class-> ActionFunction("\\eta") {  Action.EtaReduce(it).run()  }
                Action.Display::class-> ActionFunction("DISPLAY") {  Action.Display(it).run()  }
                Action.FreeVariable::class-> ActionFunction("FV") {  Action.FreeVariable(it).run()  }
                Action.DeBruijinIndex::class-> ActionFunction("REMOVE_NAME") { value1 -> Action.DeBruijinIndex(value1).run()  }
                Action.Skip::class-> Identifier("SKIP")
                Action.Equal::class-> ActionFunction("EQUIV"){ value1 -> ActionFunction{ value2-> Action.Equal(value1,value2).run() }  }
                Action.Replace::class-> ActionFunction("REPLACE") { value1 -> ActionFunction { value2->  ActionFunction{ value3-> if(value2 is Identifier) Action.Replace(value1,value2.identifier,value3).run() else value1 }  }   }
                else-> Identifier("SKIP")
            }
        }
    }
}



data class Fix(
    val selfParameter: Identifier,
    var selfFunction: Value
):Value(){
    fun toFunction():Value.Function=Value.Function(selfParameter,selfFunction)
    fun toFullFunction():Value.Application{
        val first = selfFunction.replace(selfParameter,Application.create(selfParameter,selfParameter))
        return Value.Application.create(first,first)
    }
    override fun toString(): String = "(FIX ${toFunction()})"

    companion object{
        fun create(value: Value)=when(value) {
            is Function->Fix(value.parameter,value.body)
            else->Value.Application.create(FixInstance,value)
        }

    }
}


/*
fun Fix(function: Value):Value {
    if(function !is Value.Function) return function
    //function = \lambda this. \lambda n. this x
    val param = function.parameter //this
    val body = function.body // \lambda n. this x
    if(body is Value.Function){
        body.body = body.body.replace(param,body) // \lambda n. body x
        return body
    }
    else{
        return function
        //这里没法解决啊！！！假如 function = \lambda this. x this
        //没法子变成 x f
    }
}
 */