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
	: 	tipo ID ';'
	|	tipo ID '[' NUM ']' ';' 
	;
tipo
	:	'int'
	|	'void' 
	;
fundecl
	:	tipo ID '(' params ')' compdecl 
	;
params
	: 	paramlist
	|	'void'
	;
paramlist
	: 	paramlist ',' param
	|	param 
	;
param
	:	tipo ID
	|	tipo ID '[' ']'
	;
compdecl
	: 	'{' locdecl stmtlist '}' 
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
	:	expr ';'
	|	';'
	;
seldecl
	:	iffirstpart
	|	iffirstpart ifsecondpart 
	;
iffirstpart
	:	 iffirstpartcond stmt
	;
iffirstpartcond
	:	'if' '(' expr ')'
	;
ifsecondpart
	:	'else' stmt
	;
iterdecl
	:	whilefirstpart stmt 
	;
whilefirstpart
	:	'while' '(' expr ')'
	;
retdecl
	:	'return' ';'
	|	'return' expr ';' 
	;
expr
	:	var '=' expr
	|	simpexpr 
	;
var
	:	ID
	|	ID '[' expr ']' 
	;
simpexpr
	:	somaexpr rel somaexpr
	|	somaexpr 
	;
rel
	: 	'<='
	|	'<'
	|	'>'
	|	'>='
	|	'=='
	|	'!='
	;
somaexpr
	:	somaexpr soma termo
	|	termo 
	;
soma
	:	'+'
	|	'-'
	;
termo
	:	termo mult fator
	|	fator 
	;
mult
	:	'*'
	|	'/'
	;
fator
	:	'(' expr ')'
	|	var
	|	ativ
	|	NUM 
	;
ativ
	:	ID '(' args ')' 
	;
args
	:	arglist
	|	
	;
arglist
	:	arglist ',' expr
	|	expr 
	;

BLOCOCOMENT : '/*' .*? '*/' -> skip;

LINHACOMENT : '//' .*? '\n' -> skip;

WS : [ \n\t\r]+ -> channel(HIDDEN);

ID	:	[a-zA-Z]+;
NUM	:	[0-9]+;