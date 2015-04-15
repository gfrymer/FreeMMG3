#!/bin/bash
export CLASSPATH=bin:../disimmcast/Examples/DiSimmcast:../disimmcast/JavaSim/classes:../disimmcast/lib/gson-2.2.4.jar
java FreeMMGSimulator MANAGER 192.168.1.104 mmg.sim
