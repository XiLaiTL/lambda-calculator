package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.Action
import model.Action.AlphaReduce.Companion.alphaReduce
import model.Action.BetaReduce.Companion.betaReduce
import model.Action.Companion.collect
import model.Action.Display.Companion.displayBetaReduce
import model.Action.EtaReduce.Companion.etaReduce
import model.Action.Normalize.Companion.normalize
import model.DeBruijn.Companion.toDeBruijnIndex
import model.Value
import tree.LambdaReduce

@Composable
fun SingleCalculatorPage(){
    var inputText by remember { mutableStateOf("\\lambda x.y x") }
    var equivInputText by remember { mutableStateOf("") }
    var alphaInputText by remember { mutableStateOf("") }
    var scriptInputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var equivOutputText by remember { mutableStateOf("") } //= remember mutableStateListof
    var showEquiv by remember { mutableStateOf(false) }
    var showScript by remember { mutableStateOf(false) }
    Row (
        modifier = Modifier.fillMaxSize()
    ){
        Column (
            modifier = Modifier.fillMaxWidth(0.5f)
        ){
            if(showScript)
                TextField(
                    value = scriptInputText,
                    onValueChange = { scriptInputText = it },
                    modifier = Modifier.fillMaxHeight(0.3f).fillMaxWidth(),
                    label = { Text("代码片段") }
                )

            TextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    Action.equivList.clear()
                    equivOutputText = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("表达式") }
            )
            Latex(inputText.replace(" ","\\;"),"inputText")

            if(equivOutputText.isNotBlank()){
                equivOutputText.split("\n").forEachIndexed{ index,str->
                    Latex(str.replace(" ","\\;"), "equivOutputText$index", alignment = Alignment.Start)
                }
            }

            if(showEquiv)
                TextField(
                    value = equivInputText,
                    onValueChange = { equivInputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("比较等价的表达式") }
                )
        }
        Row(
            Modifier.fillMaxWidth(0.3f).fillMaxHeight().background(MaterialTheme.colors.background)
        ){
            Spacer(Modifier.width(5.dp))
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(Modifier.height(1.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if(!showScript) ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface, contentColor = MaterialTheme.colors.onPrimary)
                        else ButtonDefaults.buttonColors(),
                    onClick = {
                        if(!showScript) {
                            showScript = true
                            scriptInputText=""
                        }
                        else if(scriptInputText.isNotBlank()){
                            LambdaReduce.clearScope()
                            LambdaReduce.calculate(scriptInputText)
                            outputText = Action.messageList.joinToString("\n")
                        }
                    }
                ){
                    if(!showScript)
                        Text("预设环境")
                    else{
                        Text("点击预设")
                        IconButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                LambdaReduce.clearScope()
                                Action.messageList.clear()
                                scriptInputText=""
                                showScript = false
                            }
                        ){
                            Icon(Icons.Default.Clear,"清除代码片段")
                        }
                    }
                }
                Spacer(Modifier.height(1.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if(!showEquiv) ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface, contentColor = MaterialTheme.colors.onPrimary)
                    else ButtonDefaults.buttonColors(),
                    onClick = {
                        val value = LambdaReduce.build(inputText)
                        if(!showEquiv) {
                            showEquiv = true
                        }
                        else if(equivInputText.isBlank()) HomeTabs.showInfo("请输入比较表达式")
                        else {
                            val newValue = LambdaReduce.build(equivInputText)
                            Action.messageList.clear()
                            val equal = Action.Equal(value,newValue)
                            try{
                                equal.run()
                                val resString = Action.equivList.joinToString("\n")
                                equivOutputText=resString
                            }
                            catch(e:Exception){
                                HomeTabs.showInfo(info = e.toString())
                            }
                            catch (e:Error){
                                HomeTabs.showInfo(info = e.toString())
                            }
                            
                            Action.messageList.clear()
                        }
                    }
                ){
                    if(!showEquiv){
                        Text("比较等价")
                    }
                    else{
                        Text("点击比较")
                        IconButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                Action.equivList.clear()
                                equivOutputText=""
                                showEquiv = false
                            }
                        ){
                            Icon(Icons.Default.Clear,"清除等价比较结果")
                        }
                    }
                }

                Spacer(Modifier.height(5.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val value = LambdaReduce.build(inputText)
                        if(value !is Value.Function){
                            HomeTabs.showInfo(info = "不是函数")
                        }
                        else{
                            if(alphaInputText.isBlank()) alphaInputText="alpha"
                            val valueString = value.toString()
                            try{
                                val res = value.alphaReduce(alphaInputText)
                                val resString = res.toString()
                                if(resString==valueString){
                                    HomeTabs.showInfo(info = "变换结果相同")
                                }
                                else{
                                    outputText=resString
                                }
                            }
                            catch(e:Exception){
                                HomeTabs.showInfo(info = e.toString())
                            }
                            catch (e:Error){
                                HomeTabs.showInfo(info = e.toString())
                            }
                            
                        }
                    }
                ){
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text("α变换")
                        TextField(
                            value = alphaInputText,
                            onValueChange = { alphaInputText = it },
                            label = { Text("变量名")}
                        )
                    }
                }
                Spacer(Modifier.height(5.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val value = LambdaReduce.build(inputText)
                        val valueString = value.toString()
                        try {
                            Action.Display.once =false
                            val res = value.displayBetaReduce()
                            Action.Display.once =false
                            val resString = res.toString()
                            if(resString==valueString){
                                HomeTabs.showInfo(info = "规约结果相同")
                            }
                            else{
                                outputText=resString
                            }
                        }
                        catch(e:Exception){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        catch (e:Error){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        
                    }
                ){
                    Text("β规约")
                }
                Spacer(Modifier.height(5.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val value = LambdaReduce.build(inputText)
                        if(value !is Value.Function){
                            HomeTabs.showInfo(info = "不是函数")
                        }
                        else{
                            val valueString = value.toString()
                            try{
                                val res = value.etaReduce()
                                val resString = res.toString()
                                if(resString==valueString){
                                    HomeTabs.showInfo(info = "规约结果相同")
                                }
                                else{
                                    outputText=resString
                                }
                            }
                            catch(e:Exception){
                                HomeTabs.showInfo(info = e.toString())
                            }
                            catch (e:Error){
                                HomeTabs.showInfo(info = e.toString())
                            }
                            
                        }
                    }
                ){
                    Text("η规约")
                }
                Spacer(Modifier.height(5.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val value = LambdaReduce.build(inputText)
                        Action.messageList.clear()
                        val display = Action.Display(value)
                        try{
                            display.run()
                            val resString = Action.messageList.joinToString("\n")
                            outputText=resString
                        }
                        catch(e:Exception){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        catch (e:Error){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        
                        Action.messageList.clear()

                    }
                ){
                    Text("规范化过程")
                }
                Spacer(Modifier.height(5.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant),
                    onClick = {
                        val value = LambdaReduce.build(inputText)
                        val valueString = value.toString()
                        try{
                            val res =value.normalize()
                            val resString = res.toString()
                            if (resString == valueString) {
                                HomeTabs.showInfo(info = "规约结果相同")
                            } 
                            else {
                                outputText = resString
                            }
                        }
                        catch(e:Exception){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        catch (e:Error){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        
                    }
                ){
                    Text("规范式")
                }
                Spacer(Modifier.height(2.5.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant),
                    onClick = {
                        val value = LambdaReduce.build(inputText)
                        Action.messageList.clear()
                        try{
                            val set = Action.FreeVariable.FV(value)
                            val resString = set.toString()
                            outputText=resString
                        }
                        catch(e:Exception){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        catch (e:Error){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        
                        Action.messageList.clear()

                    }
                ){
                    Text("自由变元")
                }
                Spacer(Modifier.height(2.5.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant),
                    onClick = {
                        val value = LambdaReduce.build(inputText)
                        Action.messageList.clear()
                        try{
                            val res = value.toDeBruijnIndex()
                            val resString = res.toString()
                            outputText=resString
                        }
                        catch(e:Exception){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        catch (e:Error){
                            HomeTabs.showInfo(info = e.toString())
                        }
                        
                        Action.messageList.clear()

                    }
                ){
                    Text("无名项")
                }

            }
        }
        Spacer(Modifier.width(5.dp))


        LazyColumn (
            modifier = Modifier.fillMaxWidth()
        ){
            if(outputText.isNotBlank()){
                itemsIndexed(outputText.split("\n")){ index,str->
                    Latex(str.replace(" ","\\;"), "outputText$index",
                        alignment = if(index==0) Alignment.CenterHorizontally else Alignment.Start)
                }
            }
        }
    }
}