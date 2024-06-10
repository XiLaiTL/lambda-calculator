package ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import org.scilab.forge.jlatexmath.ParseException
import org.scilab.forge.jlatexmath.TeXConstants
import org.scilab.forge.jlatexmath.TeXFormula
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.*
import kotlin.Exception

object LatexSource{
    val labelMap:MutableMap<String,JLabel> = HashMap()
    fun label(index:String):JLabel{
        return labelMap.computeIfAbsent(index) { JLabel() }
    }
}

fun alignmentToBorderLayout(
    alignment: Alignment
):String = when(alignment){
    Alignment.TopStart-> BorderLayout.NORTH
    Alignment.TopCenter-> BorderLayout.NORTH
    Alignment.TopEnd-> BorderLayout.NORTH
    Alignment.CenterStart-> BorderLayout.WEST
    Alignment.Center-> BorderLayout.CENTER
    Alignment.CenterEnd-> BorderLayout.EAST
    Alignment.BottomStart-> BorderLayout.SOUTH
    Alignment.BottomCenter-> BorderLayout.SOUTH
    Alignment.BottomEnd-> BorderLayout.SOUTH
    else -> BorderLayout.CENTER
}

fun alignmentToBorderLayout(
    alignment: Alignment.Horizontal
):String = when(alignment){
    Alignment.Start->BorderLayout.WEST
    Alignment.CenterHorizontally->BorderLayout.CENTER
    Alignment.End->BorderLayout.EAST
    else -> BorderLayout.CENTER
}

fun alignmentToFlowLayout(
    alignment: Alignment.Horizontal
):Int = when(alignment){
    Alignment.Start->FlowLayout.LEFT
    Alignment.CenterHorizontally->FlowLayout.CENTER
    Alignment.End->FlowLayout.RIGHT
    else -> FlowLayout.CENTER
}

fun String.couldParseLatex():Boolean{
    try {
        TeXFormula(this)
    } catch (_:ParseException){
        return false
    }
    return true
}

@Composable
fun Latex(
    latex:String,
    index:String,
    size:Float = 20F,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colors.secondary,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    modifier: Modifier =Modifier
){
    SwingPanel(
        modifier = modifier.fillMaxWidth().height(40.dp),
        factory = {
            JPanel().apply {
                add(LatexSource.label(index), alignmentToBorderLayout(alignment))
                layout = FlowLayout(alignmentToFlowLayout(alignment))
                background = Color(color.red,color.green,color.blue,color.alpha)
            }
        },
        update = {
            try{
                val formula = TeXFormula(latex);
                val icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, size);
                LatexSource.label(index).icon = icon
            }
            catch (_:Exception){

            }
        }
    )
}



