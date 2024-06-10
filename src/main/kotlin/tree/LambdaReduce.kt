package tree

import LambdaLexer
import LambdaParser
import model.Action.Companion.collect
import model.Value
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

object LambdaReduce {
    private val visitor = LambdaReduceVisitor()
    fun clearScope() = visitor.clearScope()
    fun visit(code:String){
        val input = CharStreams.fromString(code)

        val lexer = LambdaLexer(input)
        val tokens= CommonTokenStream(lexer)
        val parser = LambdaParser(tokens).apply {
            buildParseTree=true
            addErrorListener(LambdaReduceErrorListener)
        }
        val tree = parser.application()
        //println(tree.toStringTree(parser));
        visitor.clearActions()
        visitor.visit(tree)
    }
    fun calculateOrThrow(code:String){
        visit(code)
        visitor.runActionsOrThrow()
    }
    fun calculate(code:String){
        try{
            visit(code)
            visitor.runActions()
        }
        catch (e:Exception){
            collect(e)
        }
        catch (e:Error){
            collect(e)
        }

    }

    fun build(code:String): Value {
        try{
            val input = CharStreams.fromString(code)

            val lexer = LambdaLexer(input)
            val tokens= CommonTokenStream(lexer)
            val parser = LambdaParser(tokens).apply {
                buildParseTree=true
                //addErrorListener(LambdaErrorListener)
            }
            val tree = parser.lambdaExpression()
            visitor.clearActions()
            val value = visitor.visit(tree)
            return value
        }
        catch (e:Exception){
            collect(e)
            return Value.Identifier("Exception")
        }
        catch (e:Error){
            collect(e)
            return Value.Identifier("Error")
        }
    }

}