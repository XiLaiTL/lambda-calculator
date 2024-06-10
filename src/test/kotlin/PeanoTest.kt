import tree.LambdaReduce.calculate
import kotlin.test.Test

class PeanoTest {

    @Test
    fun peanoTest1(){
        calculate("""
            PRINT (\lambda x.y x);
            zero = (\lambda f x. x);
            succ = (\lambda n f x. f (n f x));
            PEANO ( I | zero | succ );
            NORMALIZE (I_{1});
            PRINT (I_{0});
            PRINT (I_{1});
        """.trimIndent())
    }

    @Test
    fun peanoTest4(){
        calculate("""
            PRINT (\lambda x.y x);
            ZERO = (\lambda f x. x);
            SUCC = (\lambda n f x. f (n f x));
            PEANO ( I | ZERO | SUCC );
            PLUS = (\lambda m n f x. n f (m f x));
            T1 = (PLUS I_{0} I_{1});
            T2 = (PLUS I_{1} I_{1});
            EQUIV (I_{1} | T1);
            EQUIV (I_{2} | T2 );
            EQUIV (I_{1} | T2 );
            
        """.trimIndent())
    }


    @Test
    fun logicalTest1() {
        calculate("""
            TRUE = (\lambda f x. f);
            FALSE = (\lambda f x. x);

            AND = (\lambda f x. f x f);
            OR = (\lambda f x. f f x);
            NOT = (\lambda n f x. n x f);
            XOR = (\lambda f x. f (NOT x) x);
            
            and1 = (AND TRUE FALSE); EQUIV (FALSE| and1);
            and2 = (AND FALSE FALSE); EQUIV (FALSE| and2);
            not1 = (NOT TRUE); EQUIV (FALSE | not1);
            xor1 = (XOR (OR TRUE FALSE) FALSE); EQUIV (TRUE | xor1);
        """.trimIndent())
    }

    @Test
    fun comparisonTest1() {
        calculate("""
            ZERO = (\lambda f x. x);
            SUCC = (\lambda n f x. f (n f x));
            PEANO ( I | ZERO | SUCC );

            PLUS = (\lambda m n f x. n f (m f x));

            PRED = (\lambda n f x. n (\lambda g h. h (g f)) (\lambda u. x) (\lambda u. u));
            SUB = (\lambda m n. n PRED m);

            # logical
            TRUE = (\lambda f x. f);
            FALSE = (\lambda f x. x);

            AND = (\lambda f x. f x f);
            OR = (\lambda f x. f f x);
            NOT = (\lambda n f x. n x f);
            XOR = (\lambda f x. f (NOT x) x);

            # comparison

            ISZERO = (\lambda n. n (\lambda x. FALSE) TRUE);
            LEQ = (\lambda m n. ISZERO (SUB m n));
            LT = (\lambda a b. NOT (LEQ b a));
            EQ = (\lambda m n. AND (LEQ m n) (LEQ n m));
            NEQ = (\lambda a b. OR (NOT (LEQ a b)) (NOT (LEQ b a)));
            GEQ = (\lambda a b. LEQ b a);
            GT = (\lambda a b. NOT (LEQ a b));
            
            NORMALIZE (LT I_{0} I_{1});
        """.trimIndent())
    }
}