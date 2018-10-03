CC = javac
VM = java

default: antlr4 compile exec

antlr4:
	$(VM) -jar /usr/local/lib/antlr-4.7.1-complete.jar Cmenos.g4;

compile:
	$(CC) *.java
exec:
	#$(VM) org.antlr.v4.gui.TestRig Cmenos prog -gui < input
clean:
	rm *.class *.tokens

teste:
	export CLASSPATH=".:/usr/local/lib/antlr-4.7.1-complete.jar:$CLASSPATH"
	java -jar /usr/local/lib/antlr-4.7.1-complete.jar Cmenos.g4
	javac *.java
	java org.antlr.v4.gui.TestRig Cmenos prog -gui input
	java -jar /usr/local/lib/antlr-4.7.1-complete.jar -no-listener -visitor Cmenos.g4
	javac *.java
	java EvalVisitor < input