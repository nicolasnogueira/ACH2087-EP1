CC = javac
VM = java

default: antlr4 compile exec

antlr4:
	export CLASSPATH=".:/usr/local/lib/antlr-4.7.1-complete.jar:$CLASSPATH"
	$(VM) -jar /usr/local/lib/antlr-4.7.1-complete.jar Cmenos.g4

compile:
	$(CC) *.java
exec:
	$(VM) org.antlr.v4.gui.TestRig Cmenos prog -gui < input

distclean:
	rm -f *.java *.tokens *.interp
clean:
	rm -f *.class *.tokens