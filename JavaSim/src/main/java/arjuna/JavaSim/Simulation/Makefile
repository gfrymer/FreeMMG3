MAKE = make

JAVA  = javac

JAVA_FLAGS =

SOURCES	= SimulationProcessList.java Scheduler.java \
	  SimulationProcess.java SimulationException.java \
	  SimulationEntity.java Semaphore.java TriggerQueue.java \
	  SemaphoreOutcome.java RestartException.java

all: SimulationProcessList.class Scheduler.class \
     SimulationProcess.class SimulationException.class \
     SimulationEntity.class Semaphore.class TriggerQueue.class \
     SemaphoreOutcome.class RestartException.class

Scheduler.class: Scheduler.java
	$(JAVA) $(JAVA_FLAGS) Scheduler.java

SimulationProcess.class: SimulationProcess.java
	$(JAVA) $(JAVA_FLAGS) SimulationProcess.java

SimulationException.class: SimulationException.java
	$(JAVA) $(JAVA_FLAGS) SimulationException.java

RestartException.class: RestartException.java
	$(JAVA) $(JAVA_FLAGS) RestartException.java

SimulationProcessList.class: SimulationProcessList.java
	$(JAVA) $(JAVA_FLAGS) SimulationProcessList.java

SimulationEntity.class: SimulationEntity.java
	$(JAVA) $(JAVA_FLAGS) SimulationEntity.java

Semaphore.class: Semaphore.java
	$(JAVA) $(JAVA_FLAGS) Semaphore.java

SemaphoreOutcome.class: SemaphoreOutcome.java
	$(JAVA) $(JAVA_FLAGS) SemaphoreOutcome.java

TriggerQueue.class: TriggerQueue.java
	$(JAVA) $(JAVA_FLAGS) TriggerQueue.java

clean:
	rm -rf *.class *~*

purge: clean
