import tree.LambdaReduce.calculate
import kotlin.test.Test

class VisitorTest{
    @Test
    fun test1() {
        calculate( """
            ACT (BETA ((\lambda x.y x) u));
        """.trimIndent())
    }

    @Test
    fun etaTest(){
        calculate( """
            NORMALIZE ((\lambda x y.M y) u );
            NORMALIZE ((\lambda x y.z y) u );
        """.trimIndent())
    }

    @Test
    fun alphaTest(){
        calculate( """
            NORMALIZE ((\lambda x y.x (\lambda x.x)) u );
        """.trimIndent())
    }

    @Test
    fun assignTest(){
        calculate( """
            test=(\lambda x.x);
            NORMALIZE ((\lambda x y.x test) u );
            Test=(\lambda x.x);
            NORMALIZE ((\lambda x y.x Test) u );
        """.trimIndent())
    }

    @Test
    fun scopeTest(){
        calculate("""
            NORMALIZE (\lambda x y.{
                PRINT (\lambda z.z);
            } u);
        """.trimIndent())
    }
    @Test
    fun alphaTest2(){
        calculate("""
            ACT (ALPHA ((\lambda x y.x) | i));
        """.trimIndent())
    }

    @Test
    fun replaceTest(){
        calculate("""
            ACT (REPLACE ((\lambda x .x y) | y | k));
            ACT (REPLACE (\lambda x .x y) y k);
            ACT (REPLACE (\lambda x.y x) y PRINT);
        """.trimIndent())
    }

    @Test
    fun displayTest(){
        calculate("""
            DISPLAY ((\lambda x y.x (\lambda x.x)) u );
        """.trimIndent())
    }

    @Test
    fun alphaTest3(){
        calculate("""
            DISPLAY (((\lambda x.x y) (\lambda b.x y))[(\lambda a.a b)/y]) ;

            
        """.trimIndent())
    }


    @Test
    fun replaceTest1(){
        calculate("""
            S1 = x[(x y)/x]; 
            S2 = y[M/x];
            S3_5 = (\lambda x.x y)[E/x];
            S3_7 = (\lambda x.x z)[w/y];
            
            NORMALIZE S1;
            NORMALIZE S2;
            NORMALIZE S3_5;
            NORMALIZE S3_7;
        """.trimIndent())
    }

    @Test
    fun replaceTest2(){
        calculate("""
            S4 = ((\lambda x.x y) (\lambda b.x y))[(\lambda a.a b)/y];
            NORMALIZE S4;

        """.trimIndent())
    }

    @Test
    fun alphaTest1(){
        calculate("""
            
            ACT (ALPHA ((\lambda x.(z x)) | y));
            ACT (ALPHA ((\lambda x.((\lambda y.y x) x)) | z));
            ACT (ALPHA ((\lambda x.(z (\lambda y.x))) | y));
            ACT (ALPHA ((\lambda x.(z y)) | y));

        """.trimIndent())
    }

    @Test
    fun betaTest1(){
        calculate("""
            
            BETA ((\lambda x.x y) x);
            BETA ((\lambda x.x x) y);
            NORMALIZE ((\lambda x. (\lambda y. (\lambda z. x y z))) a b c);
            DISPLAY ((\lambda x. (\lambda y. (\lambda z. x y z))) a b c);

        """.trimIndent())
    }

    @Test
    fun etaTest1(){
        calculate("""
            ETA (\lambda x. (\lambda y. y y) x);
            ETA (\lambda x. (\lambda y. y x) x);

            DISPLAY ((\lambda y.y ((\lambda a.x a) (\lambda a.a))) (\lambda b.b));
        """.trimIndent())
    }

    @Test
    fun equivTest1(){
        calculate("""
            msg = (\lambda y.y);
            EQUIV ( (\lambda x.x) | msg );
            EQUIV ((\lambda x.y) | msg);
        """.trimIndent())
    }


    @Test
    fun idTest1(){
        calculate("""
            id = (\lambda x.x);
            DISPLAY (id (id (\lambda z. id z)));
        """.trimIndent())
    }



}
