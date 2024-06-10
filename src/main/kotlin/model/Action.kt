package model

import model.Action.AlphaReduce.Companion.alphaReduce
import model.Action.AlphaReduce.Companion.containIdentifier
import model.Action.AlphaReduce.Companion.restoreName
import model.Action.BetaReduce.Companion.betaReduce
import model.Action.Display.Companion.displayBetaReduce
import model.Action.EtaReduce.Companion.containsExpressionIdentifier
import model.Action.EtaReduce.Companion.etaReduce
import model.Action.Normalize.Companion.normalize
import model.Action.Print.Companion.activate
import model.Action.Replace.Companion.replace
import model.DeBruijn.Companion.toDeBruijnIndex
import kotlin.reflect.KClass

//推导步骤的展示太难做的话，可以试试，做每个子表达式的化简结果，然后倒推回来建立一个树

sealed interface Action {
    fun run(): Value
    
    companion object{
        val messageList:MutableList<String> = ArrayList()
        val equivList:MutableList<String> = ArrayList()

        fun collect(message: Any?){
            println(message)
            messageList.add(message.toString())
        }
        fun collectReduce(message:Any?, type:String){
            val longMessage = "\\xrightarrow{${type}} $message"
            println(longMessage)
            messageList.add(longMessage)
        }
        fun collectEqual(message:Any?, type:Boolean){
            val longMessage = "${if(type)"" else "\\not"}\\equiv $message"
            println(longMessage)
            messageList.add(longMessage)
            equivList.add(longMessage)
        }

        fun actionType(key:String): KClass<out Action> = when(key){
            "ACT"-> Activate::class
            "PRINT"-> Print::class
            "NORMALIZE"-> Normalize::class
            "ALPHA","\\alpha", "α"->AlphaReduce::class
            "BETA" , "\\beta" , "β"-> BetaReduce::class
            "ETA",  "\\eta", "η"-> EtaReduce::class
            "DISPLAY"->Display::class
            "FV" ->FreeVariable::class
            "REMOVE_NAME"->DeBruijinIndex::class
            "REPLACE"->Replace::class
            "EQUIV","\\equiv" ->Equal::class
            else -> Skip::class
        }
    }

    class Activate(private val value:Value):Action {
        override fun run(): Value {
            return value.activate()
        }
    }

    class Print(private val value:Value):Action{
        override fun run():Value {
            val res = value.activate()
            collect(res)
            return res
        }
        companion object{
            fun Value.activate():Value = when(this){
                is Value.Application ->{
                    val func = this.function.activate()
                    //if(func.name=="\\beta"||func.name=="\\alpha"||func.name=="\\eta"||func.name=="PRINT"||func.name=="REPLACE")
                    if(func is ActionFunction){
                        func.run(this.argument).activate()
                    }
                    else{
                        Value.Application.create(func,this.argument.activate())
                    }
                }
                is Value.ExpressionIdentifier -> this
                is Fix -> this
                is Value.Function -> Value.Function(this.parameter,this.body.activate())
                is Value.Identifier -> this
                is Value.Replace -> Value.Replace(this.expression.activate(),this.argument.activate(),this.replaceHolder)
                is ActionFunction -> this
            }
        }
    }

    class DeBruijinIndex(private val value:Value):Action{
        override fun run():Value {
            val res = value.activate()
            collect(res.toDeBruijnIndex())
            return res
        }

    }

    class FreeVariable(private val value:Value):Action{
        override fun run():Value {
            val res = value.activate()
            collect(FV(res) )
            return res
        }

        companion object {
            fun FV(value: Value):Set<Value.Identifier> =
                when(value){
                    is Value.Application -> FV(value.function).union(FV(value.argument))
                    is Value.ExpressionIdentifier -> mutableSetOf()
                    is Value.Function -> FV(value.body).filterNot { it==value.parameter }.toSet()
                    is Value.Identifier -> mutableSetOf(value)
                    is Value.Replace -> {
                        val replaceNew = value.expression.replace(value.replaceHolder,value.argument)
                        if(replaceNew is Value.Replace) mutableSetOf()
                        else FV(replaceNew)
                    }
                    is Fix-> FV(value.toFunction())
                    is ActionFunction -> mutableSetOf()
                }
        }
    }


    class Display(private val value:Value):Action{
        override fun run():Value {
            Value.Identifier.resetCount()
            var value = this.value.restoreName()
            var stringNow = value.toString()
            var after = value.displayBetaReduce()
            collect(stringNow)
            while( stringNow != after.toString()){
                once=false
                if(value == after)
                    collectReduce(after,"\\alpha")
                else collectReduce(after.restoreName(),"\\beta")
                value = after
                stringNow = value.toString()
                after = value.displayBetaReduce()
            }
            if(value is Value.Function){
                stringNow = value.toString()
                after = value.etaReduce()
                if(stringNow!=after.toString()){
                    value = after
                    collectReduce(value,"\\eta")
                }
            }
            stringNow = value.toString()
            after = value.restoreName()
            if(stringNow!=after.toString()){
                value = after
                collectReduce(value,"\\alpha")
            }
            Value.Identifier.resetCount()
            return value
        }

        companion object{
            var once = false
            fun Value.displayAlphaReduce(parameter: Value.Identifier,argument: Value):Value = when(this){
                is Value.Function -> {
                    if(this.parameter===parameter) //this.replace(this.parameter,Value.Identifier.tempIdentifier())
                    {
                        val newIdentifier = Value.Identifier(this.parameter.originIdentifier)
                        newIdentifier.rename(Value.Identifier.tempName())
                        Value.Function(newIdentifier, this.body.replace(this.parameter, newIdentifier))
                        //相当于alpha reduce了
                    }
                    else if(this.parameter==parameter) {
                        this.alphaReduce(Value.Identifier.tempName())
                    }
                    //没法解决这里的问题：假设argument含有的是相同的引用，这样alphaReduce根本解决不了问题！
//                    else if (argument.containIdentifier(this.parameter.identifier)) {
//                        this.alphaReduce(Value.Identifier.tempName())
//                    }

                    else {
                        Value.Function(this.parameter,this.body.displayAlphaReduce(parameter,argument))
                    }
                }
                is Value.Application.IdentifierApplication -> this
                is Value.Application-> Value.Application.create(this.function.displayAlphaReduce(parameter,argument),this.argument.displayAlphaReduce(parameter,argument))
                is Value.ExpressionIdentifier -> this
                is Value.Identifier -> this
                is Value.Replace -> this
                is Fix ->Fix(this.selfParameter,this.selfFunction.displayAlphaReduce(parameter,argument))
                is ActionFunction -> this
            }

            fun Value.displayBetaReduce(): Value = when (this) {
                is Value.Application.IdentifierApplication -> this
                is Value.Application -> when(function) {
                    is ActionFunction-> {
                        (function as ActionFunction).run(this.argument)
                    }
                    is Fix -> {
                        val functionHere = (function as Fix)
                        val functionNew =  functionHere.selfFunction.replace(functionHere.selfParameter, functionHere)
                        val argumentNew = argument.betaReduce()
                        val res = when(functionNew) {
                            is Value.Function -> functionNew.body.replace(functionNew.parameter, argumentNew )
                            else -> Value.Application.create(functionNew, argumentNew)
                        }
                        res
                    }

                    else -> {
                        val functionNew = when (function) {
                            is Value.Function -> function
                            else -> function.displayBetaReduce()
                        }
                        //最左规约不需要argument首先规约
                        val argumentNew = argument//.displayBetaReduce()
                        var resFunc = functionNew
                        if (once) {
                            Value.Application.create(functionNew, argumentNew)
                        }
                        else{
                            val res = when (functionNew) {
                                is Value.Function -> {
                                    val stringNow = functionNew.body.toString()
                                    val res1 = functionNew.body.displayAlphaReduce(functionNew.parameter,argumentNew)
                                    resFunc = Value.Function(functionNew.parameter, res1)
                                    if (res1.toString() != stringNow) Value.Application.create(resFunc, argumentNew)
                                    else res1.replace(functionNew.parameter, argumentNew)
                                }
                                else -> Value.Application.create(functionNew, argumentNew.displayBetaReduce())
                            }
                            once = true;res
                        }
                        //if (!once && res != this) { once = true;res } else { Value.Application.create(resFunc, argumentNew) }
                    }
                }
                /*
                is Value.Application -> {
                    val functionNew = when(function){
                        is Value.Function->function
                        is Fix->{
                            val functionHere = (function as Fix)
                            functionHere.selfFunction.replace(functionHere.selfParameter,functionHere)
                        }
                        else->function.displayBetaReduce()
                    }
                    //最左规约不需要argument首先规约
                    val argumentNew = argument//.betaReduce()
                    if (once) {
                        Value.Application.create(functionNew, argumentNew)
                    }
                    else{
                        val res = when(functionNew) {
                            is Value.Function ->{
                                if(function is Fix){
                                    val argumentNew2 =  argumentNew.betaReduce()
                                    functionNew.body.replace(functionNew.parameter, argumentNew2 )
                                }
                                else{
                                    functionNew.body.replace(functionNew.parameter, argumentNew )
                                }
                            }
                            else -> Value.Application.create(functionNew, argumentNew.displayBetaReduce())
                        }
                        once=true; res
                    }

                }
                 */

                is Value.Function -> Value.Function(parameter, body.displayBetaReduce())
                is Value.Identifier -> this
                is Value.ExpressionIdentifier -> this
                is Value.Replace -> {
                    val res= expression.replace(this.replaceHolder,argument)
                    if(!once&&res!=this) { once=true;res } else{ Value.Replace(expression,argument,this.replaceHolder) }
                }

                is Fix -> Fix(this.selfParameter,this.selfFunction.displayBetaReduce())
                is ActionFunction -> this
            }
        }
    }

    class Normalize(private val value:Value):Action{
        override fun run():Value {
            Value.Identifier.resetCount()
            val res=value.normalize()
            collect(res)
            return res
        }

        companion object{
            fun Value.normalize() = when(val betaRes=this.betaReduce()){
                is Value.Function->betaRes.etaReduce().restoreName()
                else -> betaRes.restoreName()
            }
        }
    }

    class Equal(private val value:Value, private val newValue:Value):Action{
        override fun run():Value {
            val res = value.equal(newValue)
            collectEqual(newValue,res)
            return if (res) ExpandedValue.True else ExpandedValue.False
        }

        //可以改成比较无名项哦！
        companion object{
            fun Value.equal(value: Value):Boolean{
                val thisValue = this.normalize().tempAlphaReduce()
                val newValue = value.normalize().tempAlphaReduce()
                val equivalent = thisValue == newValue
                value.restoreName()
                newValue.restoreName()
                return equivalent
            }
            fun Value.tempAlphaReduce():Value{
                Value.Identifier.resetCount()
                return when(this) {
                    is Value.Application.IdentifierApplication -> this
                    is Value.Application -> {
                        this.function.tempAlphaReduce()
                        this.argument.tempAlphaReduce()
                        this
                    }
                    is Value.ExpressionIdentifier -> this
                    is Value.Function -> {
                        this.body.tempAlphaReduce()
                        this.alphaReduce(Value.Identifier.tempName())
                        this
                    }
                    is Value.Identifier -> this
                    is Value.Replace -> {
                        this.expression.tempAlphaReduce()
                        this.argument.tempAlphaReduce()
                        this
                    }
                    is Fix->{
                        toFunction().tempAlphaReduce()
                        this
                    }

                    is ActionFunction -> this
                }
            }
        }

    }

    class BetaReduce(private val value:Value):Action{
        override fun run():Value {
            Display.once =false
            val res = value.displayBetaReduce()
            Display.once =false
            collect(res)
            return res
        }
        companion object{
            fun Value.Function.betaReduce(value: Value):Value {
                val bodyRes = body.betaReduce()
                return bodyRes.replace(parameter,value)
            }

            //Application出发的
            fun Value.betaReduce(): Value = when (this) {
                is Value.Application.IdentifierApplication -> this
                is Value.Application -> {
                    val functionNew = when(function){
                        is Value.Function->function
                        is Fix->{
                            val functionHere = (function as Fix)
                            functionHere.selfFunction.replace(functionHere.selfParameter,functionHere)
                        }
                        else->function.betaReduce()
                    }
                    //最左规约不需要argument首先规约
                    val argumentNew = argument//.betaReduce()
                    when(functionNew) {
                        is Value.Function ->{
                            val argumentNew2 = if(function is Fix) argumentNew.betaReduce() else argumentNew
                            functionNew.body.replace(functionNew.parameter, argumentNew2 ).betaReduce()
                        }
                        is ActionFunction ->{
                            functionNew.run(argumentNew).betaReduce()
                        }
                        else -> Value.Application.create(functionNew, argumentNew.betaReduce())
                    }
                }
                is Value.Function -> Value.Function(parameter, body.betaReduce())
                is Value.Identifier -> this
                is Value.ExpressionIdentifier -> this
                is Value.Replace ->{
                    val replaceNew = expression.replace(this.replaceHolder,argument)
                    if (replaceNew is Value.Replace) replaceNew
                    else replaceNew.betaReduce()
                }

                is Fix -> Fix(this.selfParameter,this.selfFunction.betaReduce())
                is ActionFunction -> this
            }
        }
    }
    class AlphaReduce(private val value:Value, private val newParam:String):Action{
        constructor(value:Value) : this(value,Value.Identifier.tempName())
        override fun run():Value {
            val res =  if(value is Value.Function) {
                value.alphaReduce(newParam)
            }
            else{
                value
            }
            collect(res)
            return res
        }
        companion object{
            fun Value.Function.alphaReduce(value: Value.Identifier): Value = Value.Function(value, body.replace(this.parameter,value))
            //这里用到了rename，因此要保证前后的Identifier是用的同一个！
            //TODO: 这里可能会有一些问题
            fun Value.Function.alphaReduce(newName:String): Value.Function {
                val originName = this.parameter.identifier.toString()
                val correctName= if(!this.body.containIdentifier(newName)) newName else Value.Identifier.tempName()
                this.parameter.rename(correctName)
                if (this.containsExpressionIdentifier()){
                    return Value.Function(this.parameter, body.replace(Value.Identifier(originName),this.parameter))
                }
                return this
            }

            fun Value.restoreName(): Value= when(this){
                is Value.Application.IdentifierApplication -> this
                is Value.Application -> {
                    this.function.restoreName()
                    this.argument.restoreName()
                    this
                }
                is Value.ExpressionIdentifier -> this
                is Value.Function -> {
                    this.body.restoreName()
                    if(!this.body.containIdentifier(this.parameter.originIdentifier))
                        this.parameter.restore()
                    this
                }
                is Value.Identifier -> this
                is Value.Replace -> {
                    this.expression.restoreName()
                    this.argument.restoreName()
                    this
                }

                is Fix -> {
                    this.selfFunction.restoreName()
                    this
                }

                is ActionFunction -> this
            }

            fun Value.containIdentifier(name:String):Boolean = when(this){
                is Value.Application.IdentifierApplication -> this.function.identifier == name || this.argument.identifier == name
                is Value.Application -> this.function.containIdentifier(name) || this.argument.containIdentifier(name)
                is Value.ExpressionIdentifier -> false
                is Value.Function -> this.parameter.identifier==name || this.body.containIdentifier(name)
                is Value.Identifier -> this.identifier==name
                is Value.Replace -> this.replaceHolder.identifier!=name&&( this.expression.containIdentifier(name) || this.argument.containIdentifier(name) )
                is Fix -> this.selfParameter.identifier != name && this.selfFunction.containIdentifier(name)
                is ActionFunction -> false
            }


        }
    }
    class EtaReduce(private val value:Value):Action{
        override fun run():Value {
            val res= if(value is Value.Function) {
                value.etaReduce()
            }
            else{
                value
            }
            collect(res)
            return res
        }
        companion object{
            fun Value.Function.etaReduce(): Value {
                var cur=this
                var curBody = this.body
                while(curBody is Value.Application&&!curBody.containsExpressionIdentifier()&&!curBody.function.containIdentifier(cur.parameter.identifier) &&curBody.argument == cur.parameter) {
                    val curBodyFunc = curBody.function
                    if(curBodyFunc !is Value.Function) return curBodyFunc
                    cur = curBodyFunc
                    curBody = cur.body
                }
                return cur
            }

            fun Value.containsExpressionIdentifier():Boolean=when(this){
                is Value.Application.FunctionApplication -> this.function.containsExpressionIdentifier() || this.argument.containsExpressionIdentifier()
                is Value.Application.IdentifierApplication -> false
                is Value.Application.NormalApplication -> this.function.containsExpressionIdentifier() || this.argument.containsExpressionIdentifier()
                is Value.ExpressionIdentifier -> true
                is Value.Function -> this.body.containsExpressionIdentifier()
                is Value.Identifier -> false
                is Value.Replace -> this.expression.containsExpressionIdentifier() || this.argument.containsExpressionIdentifier()
                is Fix -> this.selfFunction.containsExpressionIdentifier()
                is ActionFunction -> false
            }
        }
    }
    class Skip(private val value:Value):Action{
        override fun run():Value { return Value.Identifier("") }
    }
    class Replace(private val value:Value, private val replaceHolder:String, private val newValue:Value):Action{
        override fun run():Value {
            val replace = value.replace(Value.Identifier(replaceHolder),newValue);
            collect(replace)
            return replace
        }
        companion object{
            //Function出发的
            /**
             * @param parameter: 形式参数
             * @param argument: 实际参数
             */
            fun Value.replace(parameter: Value.Identifier, argument: Value): Value = when (this) {
                is Value.Application.IdentifierApplication -> Value.Application.create(
                    if (this.function == parameter) argument else this.function,
                    if (this.argument == parameter) argument else this.argument
                )
                is Value.Application -> Value.Application.create(
                    this.function.replace(parameter, argument),
                    this.argument.replace(parameter, argument)
                )
                is Value.Function -> if(this.parameter === parameter){
                        val newIdentifier = Value.Identifier(this.parameter.originIdentifier)
                        newIdentifier.rename(Value.Identifier.tempName())
                        Value.Function(newIdentifier, this.body.replace(this.parameter, newIdentifier))
                        //相当于alpha reduce了
                    }
                    else if (this.parameter == parameter){
                        val func = this.alphaReduce(Value.Identifier.tempName())
                        Value.Function(func.parameter, func.body.replace(parameter, argument))
                    }
                    else if (argument.containIdentifier(this.parameter.identifier)) {
                        val func = this.alphaReduce(Value.Identifier.tempName())
                        Value.Function(func.parameter, func.body.replace(parameter, argument))
                    }
                    else Value.Function(this.parameter, body.replace(parameter, argument))
                is Value.Identifier -> if (this == parameter) argument else this
                is Value.ExpressionIdentifier -> Value.Replace(this,argument,parameter)
                is Value.Replace -> if(this.replaceHolder==parameter) this
                    else Value.Replace(
                        this.expression.replace(parameter,argument),
                        this.argument.replace(parameter,argument),
                        this.replaceHolder
                    )

                is Fix ->Fix(this.selfParameter,this.selfFunction.replace(parameter,argument))
                is ActionFunction -> this
            }
        }
    }
}