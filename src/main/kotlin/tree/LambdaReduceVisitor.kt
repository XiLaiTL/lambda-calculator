package tree

import LambdaBaseVisitor
import LambdaParser
import model.*
import model.Action.Companion.actionType
import model.Action.Companion.collect
import model.Fix.Companion.create
import model.Peano.Companion.getPeano
import model.Value.Companion.getValue
import kotlin.reflect.KClass

class LambdaReduceVisitor: LambdaBaseVisitor<Value>() {
    private val scope: Scope = Scope()
    private val actions: MutableList<Action> = ArrayList()
    fun clearActions(){
        actions.clear()
        Value.Identifier.resetCount()
    }
    fun clearScope(){scope.clear()}
    fun runActionsOrThrow(){actions.forEach {it.run()}}
    fun runActions(){ actions.forEach {
            try{
                it.run()
            }
            catch(e:Exception){
                collect(e)
            }
            catch (e:Error){
                collect(e)
            }
        }
    }

//    override fun visitApplication(ctx: LambdaParser.ApplicationContext?): Value {
//        return super.visitApplication(ctx)
//    }

//    override fun visitStatement(ctx: LambdaParser.StatementContext?): Value {
//        return super.visitStatement(ctx)
//    }

    override fun visitAbstractExpression(ctx: LambdaParser.AbstractExpressionContext?): Value.Function {
        ctx?:throw LambdaReduceException.at(LambdaParser.AbstractExpressionContext::class)
        val identifiers = ctx.identifiers().identifier().map { it.VARIABLE().text }
        scope.pushStack()
        val identifierValues = identifiers.map {
            Value.Identifier(it).also { id-> scope[it]=id }
        }
        var body = visit(ctx.lambdaExpression())
        for(identifier in identifierValues.asReversed()){
            body=Value.Function(identifier,body)
        }
        scope.popStack()
        return body as Value.Function
    }

    override fun visitAppliedExpression(ctx: LambdaParser.AppliedExpressionContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.AppliedExpressionContext::class)
        var application:Value=visit(ctx.first)
        for(atomContext in ctx.rest){
            val atom = visit(atomContext)
            application = Value.Application.create(application,atom)
        }
        return application
    }

    override fun visitAssignExpression(ctx: LambdaParser.AssignExpressionContext?): Value {
        ctx?: throw LambdaReduceException.at(LambdaParser.AssignExpressionContext::class)
        val name = ctx.allIdentifier().text
        val value = visit(ctx.atom())
        scope.checkDuplication(name)
        scope[name]=value
        return super.visitAssignExpression(ctx)
    }

//    override fun visitIdentifier(ctx: LambdaParser.IdentifierContext?): Value {
//        return super.visitIdentifier(ctx)
//    }
//
//    override fun visitIdentifiers(ctx: LambdaParser.IdentifiersContext?): Value {
//        return super.visitIdentifiers(ctx)
//    }


    override fun visitScopeAtom(ctx: LambdaParser.ScopeAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.ScopeAtomContext::class)
        scope.pushStack()
        var last:Value = visit(ctx.first)
        for(statement in ctx.rest){
            last = visit(statement)
        }
        scope.popStack()
        return last
    }

    override fun visitFunctorArgsAtom(ctx: LambdaParser.FunctorArgsAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.FunctorArgsAtomContext::class)
        val arguments= ctx.allIdentifier().map { it.text }
        if(ctx.FUNCTOR().text=="PEANO"){
            val identifier = ctx.atom().text
            val zero = scope.getValue(arguments[0])?:Value.Identifier(arguments[0])
            val succ = scope.getValue(arguments[1])?:Value.Identifier(arguments[1])
            val peano = Peano(identifier,zero,succ)
            scope[identifier] = peano
            return zero
        }
        val atom = visit(ctx.atom())
        val action:Action= when(ctx.FUNCTOR().text){
            //"ALPHA",  "\\alpha",  "α"-> Action.AlphaReduce(atom,arguments[0])
            //"REPLACE"->Action.Replace(atom,arguments[0], scope.getValue(arguments[1])?:Value.Identifier(arguments[1]) )
            "EQUIV","\\equiv" ->Action.Equal( scope.getValue(arguments[0])?:Value.Identifier(arguments[0]),atom)
            else -> Action.Skip(atom)
        }
        actions.add(action)
        return when(ctx.FUNCTOR().text){
            "ALPHA",  "\\alpha",  "α"->  Value.Application.create(Value.Application.create(ActionFunction.create(Action.AlphaReduce::class),atom), Value.Identifier(arguments[0]) )
            "REPLACE"-> Value.Application.create(Value.Application.create(Value.Application.create(ActionFunction.create(Action.Replace::class),atom), Value.Identifier(arguments[0]) ),scope.getValue(arguments[1])?:Value.Identifier(arguments[1]))
            "EQUIV","\\equiv" ->Value.Application.create(Value.Application.create(ActionFunction.create(Action.Equal::class) ,scope.getValue(arguments[0])?:Value.Identifier(arguments[0])),atom)
            else -> atom
        }
    }

    override fun visitFunctorAtom(ctx: LambdaParser.FunctorAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.FunctorAtomContext::class)
        if(ctx.FUNCTOR().text=="FIX"){
            return  ActionFunction.FixFunction
        }
        return ActionFunction.create(actionType(ctx.FUNCTOR().text))
    }

    override fun visitFunctorActionAtom(ctx: LambdaParser.FunctorActionAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.FunctorActionAtomContext::class)
        val atom = visit(ctx.atom())
        if(ctx.FUNCTOR().text=="FIX"){
            return create(atom)
        }
        val actionType = actionType(ctx.FUNCTOR().text)
        if(actionType==Action.Skip::class) return atom
        val action:Action = when(ctx.FUNCTOR().text){
            "ACT"->Action.Activate(atom)
            "PRINT"-> Action.Print(atom)
            "NORMALIZE"-> Action.Normalize(atom)
            "DISPLAY"->Action.Display(atom)
            "FV" ->Action.FreeVariable(atom)
            "REMOVE_NAME"->Action.DeBruijinIndex(atom)
            else -> Action.Skip(atom)
        }
        if(action !is Action.Skip) actions.add(action)
        return Value.Application.create(ActionFunction.create(actionType),atom)
    }

    override fun visitAssignAtom(ctx: LambdaParser.AssignAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.AssignAtomContext::class)
        return visit(ctx.assignExpression())
    }

    override fun visitParenAtom(ctx: LambdaParser.ParenAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.ParenAtomContext::class)
        return visit(ctx.lambdaExpression())
    }

    override fun visitValueAtom(ctx: LambdaParser.ValueAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.ValueAtomContext::class)
        val name = ctx.identifier().VARIABLE().text
        return scope.getValue(name)?:Value.Identifier(name) //这里就是自由变元了
    }

    override fun visitExpressionValueAtom(ctx: LambdaParser.ExpressionValueAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.ExpressionValueAtomContext::class)
        val name = ctx.expressionIdentifier().EXPRESSION().text
        return scope.getValue(name)?:Value.ExpressionIdentifier(name)
    }
    override fun visitPeanoValueAtom(ctx: LambdaParser.PeanoValueAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.PeanoValueAtomContext::class)
        val name = ctx.peanoIdentifier().allIdentifier().text
        val number = Integer.parseInt(ctx.peanoIdentifier().NATURAL_NUMBER().text)
        return scope.getPeano(name,number)?:Value.Identifier(ctx.peanoIdentifier().text)
    }

    override fun visitReplaceAtom(ctx: LambdaParser.ReplaceAtomContext?): Value {
        ctx?:throw LambdaReduceException.at(LambdaParser.ReplaceAtomContext::class)
        val expression = visit(ctx.exp)
        val newArgument = visit(ctx.new_)
        val name = ctx.identifier().VARIABLE().text
        val identifier = Value.Identifier(name)
        return Value.Replace(expression,newArgument,identifier)
    }


}