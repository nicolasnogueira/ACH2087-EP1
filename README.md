# ACH2087-EP1

EP1 de ACH2087 - Construção de Compiladores

Nícolas Nogueira  
Victor Emerenciano


Os seguintes comandos são utilizados para executar o programa:

```
// checar estrutura de diretorios
$ ./teste.sh path 				

// gerar o projeto da glc Cmenos.g4	
$ ./teste.sh antlr4

// compilar os arquivos da glc Cmenos.g4			
$ ./teste.sh compile 				

// visualizar arvore para arquivo input
$ ./teste.sh tree 				

// gera codigo MIPS para o arquivo input, salvando em output.asm	
$ ./teste.sh exec > output.asm		
```

## Organização do projeto (arquivos relevantes)

- **build**: possui arquivos gerados pelo ANTLR a serem compilados.
- **build/Cmenos**: possui os arquivos criados para percorrer a árvore e gerar o código.
- **build/cbuild**: possui os arquivos .class gerados pelo ANTLR.
- **Cmenos.g4**: gramática para a linguagem Cmenos.


Referências
- http://www.antlr.org/