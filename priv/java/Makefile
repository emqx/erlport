# 设置你要生成的jar包的文件名
# Set the file name of your jar package:
JAR_PKG = erlport.jar
# 设置你的项目的入口点
# Set your entry point of your java app:
ENTRY_POINT = erlport.CLI
# 是否需要res目录，如果你的程序有图片、文档等，
# 最好放入res目录中。
# yes: 需要；no：不需要
RES_DIR = yes
# 设置你项目包含的源文件
# 如果你使用了package，请自己在src下建立相应的目录层次，
# 并将源文件放在对应的目录中。
# 如你要生成的一个类是 com.game.A，
# 那么你的源文件应该是 com/game/A.java。
# 多个类之间用空格间隔，如果一行太长，用'\'换行，
# 建议一行一个。
# 另外注意顺序，如果class A 引用 class B，那么B.java应该放在A.java前。
SOURCE_FILES = \
erlport/terms/Tag.java \
erlport/terms/Atom.java \
erlport/terms/Binary.java \
erlport/terms/EList.java \
erlport/terms/Pid.java \
erlport/terms/Port.java \
erlport/terms/Reference.java \
erlport/terms/Tuple.java \
erlport/Utils.java \
erlport/Request.java \
erlport/Response.java \
erlport/Port.java \
erlport/CLI.java \
erlport/Main.java \

# 设置你的java编译器
# Set your java compiler here:
JAVAC = javac
# 设置你的编译选项
JFLAGS = -encoding UTF-8# -Xlint:unchecked

#############下面的内容建议不要修改####################

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

# pattern rule
# 不能处理两个类互相引用的情况，尽量避免
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
