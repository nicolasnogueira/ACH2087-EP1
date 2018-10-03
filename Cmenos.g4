// Rascunho feito por Nícolas Nogueira

grammar	Cmenos;
prog
	: 	decllist 
	;
decllist
	:	decllist decl 
	| 	decl 
	;
decl
	: 	vardecl 
	| 	fundecl 
	;
vardecl
	: 	tipo ID PONTOVIRG
	|	tipo ID COLCHESQ NUM COLCHDIR PONTOVIRG 
	;
tipo
	:	INT
	|	VOID 
	;
fundecl
	:	tipo ID PARENESQ params PARENDIR compdecl 
	;
params
	: 	paramlist
	|	VOID
	;
paramlist
	: 	paramlist VIRG param
	|	param 
	;
param
	:	tipo ID
	|	tipo ID COLCHESQ COLCHDIR
	;
compdecl
	: 	CHAVEESQ locdecl stmtlist CHAVEDIR 
	;
locdecl
	:	locdecl vardecl
	|	
	;
stmtlist
	:	stmtlist stmt
	|	
	;
stmt
	:	exprdecl
	|	compdecl
	|	seldecl
	|	iterdecl
	|	retdecl 
	;
exprdecl
	:	expr PONTOVIRG
	|	PONTOVIRG
	;
seldecl
	:	IF PARENESQ expr PARENDIR stmt
	|	IF PARENESQ expr PARENDIR stmt ELSE stmt 
	;
iterdecl
	:	WHILE PARENESQ expr PARENDIR stmt 
	;
retdecl
	:	RETURN PONTOVIRG
	|	RETURN expr PONTOVIRG 
	;
expr
	:	var ATRIBUI expr
	|	simpexpr 
	;
var
	:	ID
	|	ID COLCHESQ expr COLCHDIR 
	;
simpexpr
	:	somaexpr rel somaexpr
	|	somaexpr 
	;
rel
	: 	MENORIGUAL
	|	MENOR
	|	MAIOR
	|	MAIORIGUAL
	|	IGUAL
	|	DIFERENTE
	;
somaexpr
	:	somaexpr soma termo
	|	termo 
	;
soma
	:	MAIS
	|	MENOS
	;
termo
	:	termo mult fator
	|	fator 
	;
mult
	:	MULT
	|	DIV
	;
fator
	:	PARENESQ expr PARENDIR
	|	var
	|	ativ
	|	NUM 
	;
ativ
	:	ID PARENESQ args PARENDIR 
	;
args
	:	arglist
	|	
	;
arglist
	:	arglist VIRG expr
	|	expr 
	;

BLOCOCOMENT : '/*' .*? '*/' -> skip;

WS : [ \n\t\r]+ -> channel(HIDDEN);

INT : 'int';
VOID : 'void';

IF : 'if';
ELSE : 'else';
WHILE : 'while'; 
RETURN : 'return';

PONTOVIRG :	';';
VIRG :	',';
PARENESQ : '(';
PARENDIR : ')';
COLCHESQ : '[';
COLCHDIR : ']';
CHAVEESQ : '{';
CHAVEDIR : '}';

ATRIBUI : '=';

MENORIGUAL: '<=';
MENOR: '<';
MAIOR: '>';
MAIORIGUAL: '>=';
IGUAL: '==';
DIFERENTE: '!='; 

MULT : '*';
DIV : '/';
MAIS : '+';
MENOS : '-';

ID	:	[a-zA-Z]+;
NUM	:	[0-9]+;