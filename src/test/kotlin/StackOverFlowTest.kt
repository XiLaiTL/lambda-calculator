import tree.LambdaReduce.calculate
import tree.LambdaReduce.calculateOrThrow
import kotlin.test.Test

class StackOverFlowTest {
    @Test
    fun test1(){
        fun recursion():Unit{ recursion() }
        val err=try{
            recursion()
        }
        catch (e:StackOverflowError){
            println(e)

        }
    }

    @Test
    fun test2(){
        val err = try{
            calculate("""
                NORMALIZE ((\lambda x.x x) (\lambda x.x x));
            """.trimIndent())
        }
        catch (e:StackOverflowError){
            println(e)
        }
    }

    @Test
    fun test4(){
        calculateOrThrow("""
            ZERO = (\lambda f x. x);
            SUCC = (\lambda n f x. f (n f x));
            PEANO ( I | ZERO | SUCC );

            # I_{1} = (\lambda f x. f x);
            # I_{2} = (\lambda f x. f (f x));

            PLUS = (\lambda m n f x. n f (m f x));
            # EQUIV ((\lambda m n. n SUCC m)| PLUS );

            PRED = (\lambda n f x. n (\lambda g h. h (g f)) (\lambda u. x) (\lambda u. u));
            SUB = (\lambda m n. n PRED m);

            MULT = (\lambda m n f. m (n f));
            # EQUIV ((\lambda m n. m (PLUS n) I_{0} )|MULT);

            Y = (\lambda g. (\lambda x. g (x x)) (\lambda x. g (x x)));
            EXP = (\lambda a b. b a);

            # logical
            TRUE = (\lambda f x. f);
            FALSE = (\lambda f x. x);
            EQUIV ( I_{0} | FALSE );

            AND = (\lambda f x. f x f);
            OR = (\lambda f x. f f x);
            NOT = (\lambda n f x. n x f);
            XOR = (\lambda f x. f (NOT x) x);
            # EQUIV ( (\lambda f. f FALSE TRUE) | NOT );

            # comparison

            ISZERO = (\lambda n. n (\lambda x. FALSE) TRUE);
            LEQ = (\lambda m n. ISZERO (SUB m n));
            LT = (\lambda a b. NOT (LEQ b a));
            EQ = (\lambda m n. AND (LEQ m n) (LEQ n m));
            NEQ = (\lambda a b. OR (NOT (LEQ a b)) (NOT (LEQ b a)));
            GEQ = (\lambda a b. LEQ b a);
            GT = (\lambda a b. NOT (LEQ a b));
            NORMALIZE ( I_{1});
            TEST = (FIX (\lambda this.\lambda n.{
                (LT n I_{1}) I_{0} {
                    (this (SUB n I_{1}));
                };
            }));
            NORMALIZE (TEST I_{2});
        """.trimIndent())
    }

    @Test
    fun test3(){
        calculateOrThrow("""
            ZERO = (\lambda f x. x);
            SUCC = (\lambda n f x. f (n f x));
            PEANO ( I | ZERO | SUCC );

            # I_{1} = (\lambda f x. f x);
            # I_{2} = (\lambda f x. f (f x));

            PLUS = (\lambda m n f x. n f (m f x));
            # EQUIV ((\lambda m n. n SUCC m)| PLUS );

            PRED = (\lambda n f x. n (\lambda g h. h (g f)) (\lambda u. x) (\lambda u. u));
            SUB = (\lambda m n. n PRED m);

            MULT = (\lambda m n f. m (n f));
            # EQUIV ((\lambda m n. m (PLUS n) I_{0} )|MULT);

            Y = (\lambda g. (\lambda x. g (x x)) (\lambda x. g (x x)));
            EXP = (\lambda a b. b a);

            # logical
            TRUE = (\lambda f x. f);
            FALSE = (\lambda f x. x);
            EQUIV ( I_{0} | FALSE );

            AND = (\lambda f x. f x f);
            OR = (\lambda f x. f f x);
            NOT = (\lambda n f x. n x f);
            XOR = (\lambda f x. f (NOT x) x);
            # EQUIV ( (\lambda f. f FALSE TRUE) | NOT );

            # comparison

            ISZERO = (\lambda n. n (\lambda x. FALSE) TRUE);
            LEQ = (\lambda m n. ISZERO (SUB m n));
            LT = (\lambda a b. NOT (LEQ b a));
            EQ = (\lambda m n. AND (LEQ m n) (LEQ n m));
            NEQ = (\lambda a b. OR (NOT (LEQ a b)) (NOT (LEQ b a)));
            GEQ = (\lambda a b. LEQ b a);
            GT = (\lambda a b. NOT (LEQ a b));
            TEST = (FIX (\lambda this.\lambda n.(LT n I_{1}) I_{0} (this (SUB n I_{1}))));
            NORMALIZE (TEST I_{1});
        """.trimIndent())
    }

    @Test
    fun testFix1(){
        calculateOrThrow("""
            TEST = (FIX (\lambda this.\lambda n.this n));
            NORMALIZE (TEST i);
        """.trimIndent())
    }

    @Test
    fun testFix2(){
        calculateOrThrow("""
            ZERO = (\lambda f x. x);
            SUCC = (\lambda n f x. f (n f x));
            PEANO ( I | ZERO | SUCC );
            PRED = (\lambda n f x. n (\lambda g h. h (g f)) (\lambda u. x) (\lambda u. u));
            SUB = (\lambda m n. n PRED m);
            TRUE = (\lambda f x. f);
            FALSE = (\lambda f x. x);
            ISZERO = (\lambda n. n (\lambda x. FALSE) TRUE);
            PRINT (\lambda this.\lambda n. ISZERO n TRUE (this (SUB n I_{1})));
            TEST = (FIX (\lambda this.\lambda n. ISZERO n TRUE (this (SUB n I_{1}))));
            MULT = (\lambda m n f. m (n f));
            NORMALIZE (I_{0});
            NORMALIZE (I_{1});
            NORMALIZE (I_{2});
            NORMALIZE (I_{3});
            DISPLAY (TEST I_{3});
        """.trimIndent())
    }

    @Test
    fun fact(){
        calculateOrThrow("""
            ZERO = (\lambda f x. x);
            SUCC = (\lambda n f x. f (n f x));
            PEANO ( I | ZERO | SUCC );
            PRED = (\lambda n f x. n (\lambda g h. h (g f)) (\lambda u. x) (\lambda u. u));
            SUB = (\lambda m n. n PRED m);
            MULT = (\lambda m n f. m (n f));
            TRUE = (\lambda f x. f);
            FALSE = (\lambda f x. x);
            ISZERO = (\lambda n. n (\lambda x. FALSE) TRUE);
            FACT = (FIX (\lambda this.\lambda n. ISZERO n I_{1} (MULT n (this (SUB n I_{1})))));
            NORMALIZE (FACT I_{0});
            NORMALIZE (FACT I_{1});
            NORMALIZE (FACT I_{2});
            DISPLAY (FACT I_{3});
        """.trimIndent())
    }

    @Test
    fun fib(){
        calculateOrThrow("""
            
        """.trimIndent())
    }

}