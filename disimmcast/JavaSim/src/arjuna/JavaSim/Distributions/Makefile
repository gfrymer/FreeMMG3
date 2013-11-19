MAKE = make

JAVA  = javac

JAVA_FLAGS = 

SOURCES	= RandomStream.java UniformStream.java Draw.java \
	  ExponentialStream.java ErlangStream.java \
	  HyperExponentialStream.java NormalStream.java

all: RandomStream.class UniformStream.class Draw.class \
     ExponentialStream.class ErlangStream.class \
     HyperExponentialStream.class NormalStream.class

RandomStream.class: RandomStream.java
	$(JAVA) $(JAVA_FLAGS) RandomStream.java

UniformStream.class: UniformStream.java
	$(JAVA) $(JAVA_FLAGS) UniformStream.java

Draw.class: Draw.java
	$(JAVA) $(JAVA_FLAGS) Draw.java

ExponentialStream.class: ExponentialStream.java
	$(JAVA) $(JAVA_FLAGS) ExponentialStream.java

ErlangStream.class: ErlangStream.java
	$(JAVA) $(JAVA_FLAGS) ErlangStream.java

HyperExponentialStream.class: HyperExponentialStream.java
	$(JAVA) $(JAVA_FLAGS) HyperExponentialStream.java

NormalStream.class: NormalStream.java
	$(JAVA) $(JAVA_FLAGS) NormalStream.java

clean:
	rm -rf *.class *~*

purge: clean
