SHELL := /bin/bash

compile:
	@if [ ! -d bin ]; then mkdir bin; fi
	@javac -d bin site/projectname/*/*.java
	@echo "Compiling Complete!"



