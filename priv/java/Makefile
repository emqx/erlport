JAR_PKG = erlport.jar
ENTRY_POINT = erlport.CLI
RES_DIR = yes

# Source files
SOURCE_FILES = \
erlport/terms/Tag.java \
erlport/terms/Atom.java \
erlport/terms/Binary.java \
erlport/terms/Pid.java \
erlport/terms/Port.java \
erlport/terms/Reference.java \
erlport/terms/Tuple.java \
erlport/Utils.java \
erlport/Request.java \
erlport/Response.java \
erlport/Port.java \
erlport/CLI.java \

JAVAC = javac

JFLAGS = -encoding UTF-8# -Xlint:unchecked

#### Compile settings ####

.PHONY: default build new clean run jar

vpath %.class classes
vpath %.java src

build: $(SOURCE_FILES:.java=.class)

# show help message by default
help:
	@echo "make new: new project, create src, classes, res dirs."
	@echo "make build: build project."
	@echo "make clean: clear classes generated."
	@echo "make rebuild: rebuild project."
	@echo "make run: run your app."
	@echo "make jar: package your project into a executable jar."

%.class: %.java
	$(JAVAC) -cp classes -d classes $(JFLAGS) $<

rebuild: clean build

new:
ifeq ($(RES_DIR),yes)
	mkdir -pv src classes res
else
	mkdir -pv src classes 
endif

clean:
	rm -frv classes/*

run:
	java -cp classes $(ENTRY_POINT)

jar:
ifeq ($(RES_DIR),yes)
	jar cvfe $(JAR_PKG) $(ENTRY_POINT)  -C classes . res
else
	jar cvfe $(JAR_PKG) $(ENTRY_POINT) -C classes .
endif
