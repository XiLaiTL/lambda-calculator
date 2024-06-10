grammar Lambda;

application
    : ( SPACE* statement SPACE* ';' SPACE*)*
    ;

statement
    : lambdaExpression
    | assignExpression
    ;

lambdaExpression
    : abstractExpression
    | appliedExpression
    ;

abstractExpression
    : LAMBDA SPACE* identifiers SPACE* '.' SPACE* lambdaExpression
    ;

appliedExpression
    : first=atom (SPACE rest+=atom)*
    ;

assignExpression
    : allIdentifier SPACE* '=' SPACE* atom
    ;

allIdentifier
    : identifier | expressionIdentifier
    ;

expressionIdentifier
    : EXPRESSION
    ;

peanoIdentifier
    : allIdentifier  '_' '{' NATURAL_NUMBER '}'
    ;

identifier
    : VARIABLE
    ;

identifiers
    : identifier (SPACE identifier)*
    ;

atom
    : '(' SPACE* assignExpression SPACE* ')'                #assignAtom
    | '(' SPACE* lambdaExpression SPACE* ')'                #parenAtom
    | '{' SPACE*  first=statement  SPACE* ';' SPACE* ( rest+=statement SPACE* ';' )* SPACE* '}'              #scopeAtom
    | exp=atom SPACE* '[' SPACE* new=atom SPACE*  '/' SPACE* identifier  SPACE* ']'                      #replaceAtom
    | identifier                                              #valueAtom
    | expressionIdentifier                                    #expressionValueAtom
    | peanoIdentifier                                         #peanoValueAtom
    | FUNCTOR SPACE atom                                    #functorActionAtom
    | FUNCTOR SPACE*  '(' SPACE* atom ( SPACE* '|' SPACE* allIdentifier )+ SPACE* ')' #functorArgsAtom
    | FUNCTOR                                                 #functorAtom
    ;

LAMBDA
    : '\\lambda' | 'λ'
    ;

FUNCTOR
    : 'PRINT'
    | 'ACT'
    | 'NORMALIZE'
    | 'DISPLAY'
    | 'BETA' | '\\beta' | 'β'
    | 'ALPHA' | '\\alpha' | 'α'
    | 'ETA' | '\\eta' | 'η'
    | 'REPLACE'
    | 'EQUIV' | '\\equiv'
    | 'PEANO'
    | 'FV'
    | 'REMOVE_NAME'
    | 'FIX'
    ;

VARIABLE
    : [a-z] [a-zA-Z0-9_\-]* [a-zA-Z0-9]
    | [a-z]
    ;

EXPRESSION
    : [A-Z] [a-zA-Z0-9_\-]* [a-zA-Z0-9]
    | [A-Z]
    ;

NATURAL_NUMBER
    : [1-9][0-9]*
    | '0'
    ;

SPACE
    : [ \t]
    ;

WS
    : [\r\n] -> skip
    ;


LineComment
    : '#'+ SPACE  ~[\r\n]* -> skip
    ;