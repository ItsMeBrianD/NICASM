SHELL := /bin/bash

JAVAC=javac
BIN=bin
SRC=site/projectname/*/*.java

nicasm_DEP=site/projectname/err/SyntaxErrorException.class site/projectname/util/Numbers.class site/projectname/lang/Syntax.class

DEP=site/projectname/util/Logger.class


compile: clean
	@if [ ! -d $(BIN) ]; then mkdir $(BIN); fi
	@${JAVAC} -d ${BIN} ${SRC}
	@echo "Compiling Complete!"

package: compile checkP checkC
	@cd ${BIN};\
	jar -cfe ${CLASS}.jar site.projectname.$(PACKAGE).$(CLASS) site/projectname/$(PACKAGE)/$(wildcard *.class) ${${PACKAGE}_DEP} ${DEP};\
	mv ${CLASS}.jar ../
	@chmod +x ${CLASS}.jar
	@echo "site.projectname.${PACKAGE}.${CLASS} placed into ${CLASS}.jar with dependencies, and made executable"

clean:
	@rm -rf ${BIN}

checkP:
ifndef PACKAGE
	$(error PACKAGE is undefined)
endif

checkC:
ifndef CLASS
	$(error CLASS is undefined)
endif
