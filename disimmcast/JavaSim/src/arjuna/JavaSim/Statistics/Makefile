MAKE = make

JAVA  = javac

JAVA_FLAGS = 

SOURCES	= Histogram.java Mean.java PrecisionHistogram.java \
	  Quantile.java TimeVariance.java Variance.java \
	  SimpleHistogram.java Bucket.java StatisticsException.java

all: Histogram.class Mean.class PrecisionHistogram.class \
     Quantile.class TimeVariance.class Variance.class \
     SimpleHistogram.class Bucket.class StatisticsException.class

Histogram.class: Histogram.java
	$(JAVA) $(JAVA_FLAGS) Histogram.java

Mean.class: Mean.java
	$(JAVA) $(JAVA_FLAGS) Mean.java

PrecisionHistogram.class: PrecisionHistogram.java
	$(JAVA) $(JAVA_FLAGS) PrecisionHistogram.java

Quantile.class: Quantile.java
	$(JAVA) $(JAVA_FLAGS) Quantile.java

TimeVariance.class: TimeVariance.java
	$(JAVA) $(JAVA_FLAGS) TimeVariance.java

Variance.class: Variance.java
	$(JAVA) $(JAVA_FLAGS) Variance.java

SimpleHistogram.class: SimpleHistogram.java
	$(JAVA) $(JAVA_FLAGS) SimpleHistogram.java

Bucket.class: Bucket.java
	$(JAVA) $(JAVA_FLAGS) Bucket.java

StatisticsException.class: StatisticsException.java
	$(JAVA) $(JAVA_FLAGS) StatisticsException.java

clean:
	rm -rf *.class *~*

purge: clean
