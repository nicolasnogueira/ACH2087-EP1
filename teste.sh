#!/bin/sh
#chmod 777 teste.sh


if [ "$1" = "antlr4" ]; then
	java -jar /usr/local/lib/antlr-4.7.1-complete.jar -o ./build/ Cmenos.g4

elif [ "$1" = "compile" ]; then
	javac -cp /usr/local/lib/antlr-4.7.1-complete.jar -d $(pwd)/build/cbuild/ $(pwd)/build/*.java $(pwd)/build/Cmenos/*.java
	#javac -sourcepath $(pwd)/build/ -cp /usr/local/lib/antlr-4.7.1-complete.jar -d $(pwd)/build/cbuild/ $(pwd)/build/*.java

elif [ "$1" = "tree" ]; then
	java -cp $(pwd)/build/cbuild/:/usr/local/lib/antlr-4.7.1-complete.jar:. org.antlr.v4.gui.TestRig Cmenos prog -gui < input

elif [ "$1" = "exec" ]; then
	java -cp $(pwd)/build/cbuild/:/usr/local/lib/antlr-4.7.1-complete.jar:. CmenosMips

elif [ "$1" = "clean-all" ]; then
	rm $(pwd)/build/*.java -f
	rm $(pwd)/build/*.tokens -f
	rm $(pwd)/build/*.interp -f
	rm $(pwd)/build/cbuild/*.class -f

elif [ "$1" = "path" ]; then

	if [ ! -d $(pwd)"/build" ]; then
	mkdir build
	mkdir build/cbuild

	else
		if [ ! -d $(pwd)"/build/cbuild" ]; then
			mkdir build/cbuild
		fi
	fi

elif [ "$1" = "help" ]; then
	echo '$ ./teste.sh path \t //check directory struct '
	echo '$ ./teste.sh antlr4 \t //generate project glc \(Cmenos.g4\)'
	echo '$ ./teste.sh compile \t //compile project glc \(Cmenos.g4\)'
	echo '$ ./teste.sh tree \t //run project glc \(Cmenos.g4\)'
	echo '$ ./teste.sh exec \t //generate assembly code of input'
	echo '$ ./teste.sh clean-all \t //clean all .java / .tokens / .interp from antlr4 and ./class from project'
fi




