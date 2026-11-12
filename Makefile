JAVAC ?= javac
JAVA ?= java

.PHONY: all test demo clean
all:
	mkdir -p build
	$(JAVAC) --release 17 -Xlint:all -Werror -d build src/IsbnCheck.java

test: all
	$(JAVAC) --release 17 -Xlint:all -Werror -cp build -d build tests/IsbnCheckTest.java
	$(JAVA) -cp build IsbnCheckTest

demo: all
	$(JAVA) -cp build IsbnCheck "1-234-56789-x"

clean:
	rm -rf build
