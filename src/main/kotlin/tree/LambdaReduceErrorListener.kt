package tree

import model.Action.Companion.collect
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer


object LambdaReduceErrorListener: BaseErrorListener(){
    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        val stack=(recognizer as Parser).ruleInvocationStack.apply { reverse() }
        collect("rule stack: $stack")
        collect("line $line:$charPositionInLine at $offendingSymbol: $msg")
    }
}