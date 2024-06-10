import tree.LambdaReduce.calculate
import kotlin.test.Test

class DeBruijnTest {
    @Test
    fun indexTest(){
        calculate("""
            test1 = (\lambda x.\lambda y.x (y x));
            REMOVE_NAME test1;
            
            test2 = (\lambda z.(\lambda y.y (\lambda x.x)) (\lambda x.z x));
            REMOVE_NAME test2;
        """.trimIndent())
    }
}