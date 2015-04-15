/*
 * Simmcast - a network simulation framework
 * ScriptParser.java
 * Copyright (C) 2001-2003 Hisham H. Muhammad
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package simmcast.script;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import simmcast.distribution.CloneOnClient;
import simmcast.distribution.interfaces.NodeInterface;
import simmcast.distribution.interfaces.RouterNodeInterface;
import simmcast.distribution.proxies.NodeProxy;
import simmcast.distribution.proxies.ObjectProxy;
import simmcast.distribution.proxies.RouterNodeProxy;
import simmcast.group.Group;
import simmcast.group.GroupTable;
import simmcast.network.Network;
import simmcast.node.Node;
import simmcast.node.NodeVector;

/**
 * <p>
 * Before the beginning of the simulation, an object of this class
 * parses and executes the configuration file, creating nodes and
 * setting up the simulation scenario. For more details on the
 * configuration file format recognized by this class, see the
 * <a href="http://inf.unisinos.br/~simmcast/config_file.html">
 * documentation on the simulation file format</a>.
 * </p><p>
 * This is an ad-hoc implementation of the parser, and the language
 * recognized by this class is larger than the one you would expect
 * by reading the informal description referred to above.
 * </p>
 */
// TODO: I hope someone cleans up this class someday.
public class ScriptParser {

   // *****************************************************
   // CONSTANTS
   // *****************************************************

   /**
    * Macro names must start with MACRO_SYMBOL.
    */
   public static final String MACRO_SYMBOL  = "!";

   /**
    * The char representation of MACRO_SYMBOL.
    */
   public static final char MACRO_CHAR = '!';

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A handle to the network object, that is the main
    * holder of the node and group objects of the simulation.
    * Nodes and groups created by this class will be
    * later passed on to the network object.
    */
   Network network;

   /**
    * A simple "symbol table", where objects created during
    * the parsing of the configuration file are stored.
    */
   Hashtable symbols;

   /**
    * All nodes created by the configuration file are
    * stored in this object, which is finally passed to
    * the network object.
    */
   NodeVector nodes;

   /**
    * All groups created by the configuration file are
    * stored in this object, which is finally passed to
    * the network object.
    */
   GroupTable groups;

   /**
    * This object separates the part of the parsing logic
    * that is responsible for the pre-processing of the !MACRO
    * definitions from the rest of the code.
    */
   // TODO: The array preprocessor should be similarly separated.
   MacroPreprocessor macros;

   /**
    * This is a handle to the reflected Class object of the
    * Node class. This is provided for efficiency, since lookup
    * for this class is frequent, as part of the type checking
    * mechanism of this configuration file parser.
    */
   private Class nodeClass;
   private Class nodeInterface;
   private Class routerNodeClass;
   private Class routerNodeInterface;

   /**
    * This is a handle to the reflected Class object of the
    * Group class. This is provided for efficiency, since lookup
    * for this class is frequent, as part of the type checking
    * mechanism of this configuration file parser.
    */
   private Class groupClass;

   /**
    * This is a handle to the reflected Class object of the
    * String class. This is provided for efficiency, since lookup
    * for this class is frequent, as part of the type checking
    * mechanism of this configuration file parser.
    */
   private Class stringClass;

   /**
    * This is a handle to the reflected Class object of the
    * CloneOnClient interface. This is provided for efficiency, since lookup
    * for this class is frequent, as part of the type checking
    * mechanism of this configuration file parser.
    */
   private Class cloneOnClientInterface;

   /**
    * A temporary holder for the value of the global random seed.
    *
    * @deprecated The ScriptParser should not have any special
    * features for random seed control. Everything is now delegated
    * directly from the configuration file to the network object.
    *
    * @see simmcast.network.setSeed(long)
    */
   private long seed;

   /**
    * This flag enables the special debug mode, where all exceptions
    * display the virtual machine's stack trace.
    */
   private boolean debugMode;

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Hand the vector of nodes. This is called by the
    * network object, after this object finished processing
    * the configuration file.
    *
    * @return A vector containing all nodes declared in 
    * the configuration file.
    */
   public NodeVector getNodes() {
      return nodes;
   }

   /**
    * Hand the table of groups. This is called by the
    * network object, after this object finished processing
    * the configuration file.
    *
    * @return A table containing all groups declared in 
    * the configuration file.
    */
   public GroupTable getGroups() {
      return groups;
   }

   /**
    * Returns the global random seed.
    *
    * @return The global random seed, as it is currently defined.
    *
    * @deprecated The ScriptParser should not have any special
    * features for random seed control. Everything is now delegated
    * directly from the configuration file to the network object.
    *
    * @see simmcast.network.setSeed
    */
   public long getSeed() {
      return seed;
   }

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Parses and executes the given file, creating objects
    * and calling methods in order to set a simulation scenario up.
    *
    * @param network_ The network object from which this 
    * processor is called.
    * @param filePath_ A string containing the path and filename
    * of the configuration file.
    * @param arguments_ A list of strings that will be available
    * to the configuration file as predefined symbols or macros.
    */
   public ScriptParser(Network network_, String filePath_, String arguments_[]) throws InvalidFileException {

      debugMode = false;
      int lineCount = 1;
      try {

         nodeClass = Class.forName("simmcast.node.Node");
         groupClass = Class.forName("simmcast.group.Group");
         //streamClass = Class.forName("arjuna.JavaSim.Distributions.RandomStream");
         stringClass = Class.forName("java.lang.String");
         cloneOnClientInterface = Class.forName("simmcast.distribution.CloneOnClient");
         nodeInterface = Class.forName("simmcast.distribution.interfaces.NodeInterface");
         routerNodeClass = Class.forName("simmcast.node.RouterNode");
         routerNodeInterface = Class.forName("simmcast.distribution.interfaces.RouterNodeInterface");

         String line;
         BufferedReader in = new BufferedReader(new FileReader(filePath_));
         network = network_;
//         network.randomGenerator = new Random(seed);
         macros = new MacroPreprocessor();
         macros.defineMacro("!UNLIMITED","0");
         symbols = new Hashtable();
         symbols.put("network", network);
         if (arguments_ != null) {
            for (int i=0; i < Array.getLength(arguments_); i++) {
               String strI = Integer.toString(i);
               symbols.put("arg"+strI, arguments_[i]);
               macros.defineMacro("!ARG"+strI, arguments_[i]);
            }
         }

         nodes  = new NodeVector();
         // TODO: fix the relationship between ScriptParser and Network.
         // They should probably be in the same package. The interaction
         // between these two classes causes too much things to be public.
         network.nodes = nodes;
         groups = new GroupTable();
         seed = System.currentTimeMillis();

         // Parse loop
         while ( (line = in.readLine()) != null ) {
            int p;
            if (line.equals("#$debug")) {
               debugMode = true;
               lineCount++;
               continue;
            }
            if ( (p = line.indexOf("#")) != -1) {
               if (p>0)
                  line = line.substring(0, p-1);
               else
                  line = "";
            }
            line = macros.preprocessLine(line);
            line.trim();
            processLineArray(line);
            lineCount++;
         }

      // Exception handlers
      } catch (ClassNotFoundException c) {
         InvalidFileException e = new InvalidFileException("Error loading classes.");
         e.lineNumber = lineCount;
         if (debugMode) { System.err.println(e); e.printStackTrace(); }
         throw e;
      } catch (InvalidFileException e) {
         e.lineNumber = lineCount;
         if (debugMode) { System.err.println(e); e.printOriginalStackTrace(); }
         throw e;
      } catch (FileNotFoundException f) {
         InvalidFileException e = new InvalidFileException("File not found.");
         e.lineNumber = lineCount;
         if (debugMode) { System.err.println(e); e.printStackTrace(); }
         throw e;
      } catch (IOException i) {
         InvalidFileException e = new InvalidFileException("I/O error.");
         e.lineNumber = lineCount;
         if (debugMode) { System.err.println(e); e.printStackTrace(); }
         throw e;
      } catch (Exception x) {
         InvalidFileException e = new InvalidFileException("An error occurred while reading file.");
         System.out.println("----- Stack Trace -----");
         x.printStackTrace();
         System.out.println("-----------------------");
         e.lineNumber = lineCount;
         throw e;
      }

   }

   // *****************************************************
   // PRE-PROCESSING
   // *****************************************************

   /**
    * Performs the array-like macro expansion on the line and
    * calls processLine() for each generated line.
    * See the class description for details on the syntax of
    * the array-like macro expansion.
    * Notice that the array-like expansion occurs after the
    * macro expansion performed by the MacroPreprocessor.
    *
    * @param The line to be pre-processed.
    */
   // TODO: shouldn't this be part of the MacroPreprocessor?
   void processLineArray(String line_) throws InvalidFileException {
      try {

         int openPosition = line_.indexOf("[");
         if (openPosition != -1) {
            int dotPosition = line_.indexOf("..", openPosition);
            int closePosition = line_.indexOf("]", dotPosition);
	    if (dotPosition == -1 || closePosition == -1)
               throw new InvalidFileException("Error processing array");
	    int colonPosition = line_.indexOf(":", openPosition);
	    int first, last;
	    String variable;
	    if (colonPosition != -1 && colonPosition < closePosition) {
	       variable = line_.substring(openPosition+1, colonPosition).trim();
               first = Integer.parseInt(line_.substring(colonPosition+1, dotPosition));
               last = Integer.parseInt(line_.substring(dotPosition+2, closePosition));
	       Pattern p = Pattern.compile("\\[ *"+variable+"[^\\]]* *\\]");
	       Matcher m = p.matcher(line_);
               for (int i=first; i<=last; i++)
	          processLineArray(m.replaceAll(""+i));
	    } else {
               first = Integer.parseInt(line_.substring(openPosition+1, dotPosition));
               last = Integer.parseInt(line_.substring(dotPosition+2, closePosition));
               String before = line_.substring(0,openPosition);
               String after = line_.substring(closePosition+1);
               for (int i=first; i<=last; i++)
                  processLineArray(before+i+after);
	    }
         } else
            processLine(line_);

      } catch (InvalidFileException e) {
         throw e;
      } catch (Exception e) {
         throw new InvalidFileException("Error processing array");
      }
   }

   // *****************************************************
   // PRIMITIVES
   // *****************************************************

   /**
    * Identifies what kind of command this line is, and
    * delegates it to its respective processor method.
    * At this point, the line was already preprocessed.
    *
    * @param line_ The current line to be processed.
    */
   private void processLine(String line_) throws InvalidFileException {
      StringTokenizer tokenizer = new StringTokenizer(line_);
      int tokenCount = tokenizer.countTokens();
      if (tokenCount < 1)
         return;
      String command = tokenizer.nextToken();
      if (command.equals("new") || command.equals("newON")) {
         if (tokenCount < 2)
            throw new InvalidFileException("Invalid object definition");
         processConstructor(tokenizer, command.equals("newON"));
      } else if (command.equals("seed")) {
         if (tokenCount < 2)
            throw new InvalidFileException("Invalid random seed definition");
         processSeed(tokenizer);
      } else if (command.equals("macro")) {
         if (tokenCount < 3)
            throw new InvalidFileException("Invalid macro definition");
         processMacro(tokenizer, line_);
      } else {
         if (tokenCount < 2)
            throw new InvalidFileException("Missing method name");
         processCommand(command, tokenizer);
      }
   }

   // *****************************************************
   // PRIMITIVE HANDLERS
   // *****************************************************

   /**
    * A macro definition. 
    *
    * @param tokenizer_ The currently processed line, broken up in tokens,
    * with the first token ("macro") already consumed.
    * @param line_ The entire currently processed line.
    */
   private void processMacro(StringTokenizer tokenizer_, String line_) throws InvalidFileException {
      try {

         String label = tokenizer_.nextToken();
         if (! label.startsWith(MACRO_SYMBOL) )
            throw new InvalidFileException("Syntax error - expected: macro label");
         int macroBegin = line_.indexOf("\"") + 1;
         int macroEnd = line_.lastIndexOf("\"");
         String macroText = line_.substring(macroBegin, macroEnd);
         macros.defineMacro(label, macroText);

      } catch (IndexOutOfBoundsException e) {
         throw new InvalidFileException("Invalid macro text definition");
      }
   }

   private void processConstructor(StringTokenizer tokenizer_, boolean onworker) throws InvalidFileException {
      String className = null;
      String label = null;
      String onworkerlabel = null;
      try {
    	 if (onworker)
    	 {
    		 onworkerlabel = tokenizer_.nextToken();
    	 }
         label = tokenizer_.nextToken();
         className = tokenizer_.nextToken();
         String[] arguments = parseArguments(tokenizer_);

         Class classType = Class.forName(className);
         if ( Modifier.isAbstract(classType.getModifiers()) ) {
            System.err.println("Error: Class is abstract.");
            throw new Exception();
         }

         Object newObject = null;
         Constructor constructor = findConstructor(classType, arguments.length);
         if (!nodeClass.isAssignableFrom(classType)) {
             /* now only groups will be created by server */
             if (onworker)
             {
            	 Object nodeobject = symbols.get(onworkerlabel);
            	 if (nodeobject == null)
            		 throw new Exception("Parameter "+onworkerlabel+" not found");
            	 if (!(nodeobject instanceof NodeProxy))
            		 throw new Exception("Parameter "+onworkerlabel+" must be instance of NodeProxy");
            	 NodeProxy nodeProxy = (NodeProxy) nodeobject;

            	 ObjectProxy objectProxy = new ObjectProxy(nodeProxy.getClientId(),network,label,className,arguments);
           		 newObject = objectProxy;
             }
             else
             {
            	 Object[] generated = generateArguments(constructor.getParameterTypes(), arguments);
            	 newObject = constructor.newInstance(generated);            	 
             }
         }

         // Special case initializations
         if (nodeClass.isAssignableFrom(classType)) {
         	NodeProxy nodeProxy;
        	if (routerNodeClass.isAssignableFrom(classType)) {
        		nodeProxy = new RouterNodeProxy(network,label,className,parseObjectArgs(generateArguments(constructor.getParameterTypes(), arguments),arguments));
        	}
        	else
        	{
        		nodeProxy = new NodeProxy(network,label,className,parseObjectArgs(generateArguments(constructor.getParameterTypes(), arguments),arguments));        		
        	}
         	nodes.add(nodeProxy);
        	network.tracer.node(nodeProxy);
        	newObject = nodeProxy;
            /*Node node = (Node)newObject;
            network.initializeNode(node, label);
            node.setNetworkId(network.obtainUnicastAddress());
            nodes.add(node);
            network.tracer.node(node);*/
         } else if (groupClass.isAssignableFrom(classType)) {
            Group group = (Group)newObject;
            int id = network.obtainMulticastAddress();
            group.setNetworkId(id);
            group.setName(label);
            groups.put(new Integer(id), group);
         }

         if (symbols.get(label) != null)
            throw new InvalidFileException("Attempted to redefine node identifier: "+label);
         symbols.put(label, newObject);

      // Exception handlers
      } catch (ClassNotFoundException e) {
         throw new InvalidFileException("Class "+className+" not found");
      } catch (InvalidFileException e) {
         throw e;
      } catch (Exception e) {
         if (debugMode) { System.err.println(e); e.printStackTrace(); }
         throw new InvalidFileException("Could not create node "+label);
      }
   }

   private void processSeed(StringTokenizer tokenizer_) throws InvalidFileException {
      seed = Long.parseLong(tokenizer_.nextToken());
      network.randomGenerator = new Random(seed);
   }

   private void processCommand(String label, StringTokenizer tokenizer_) throws InvalidFileException {
      String function = null;
      try {

         function = tokenizer_.nextToken();
         String[] passedArguments = parseArguments(tokenizer_);

         Object object = symbols.get(label);
         if (object == null)
            throw new InvalidFileException("Undeclared node "+label);
         if (object instanceof NodeProxy)
         {
	         Method method = findMethod(((NodeProxy)object).getClassType(), function, passedArguments.length);
	         Object[] arguments = generateArguments(method.getParameterTypes(), passedArguments);
	         ((NodeProxy)object).invoke(function, parseObjectArgs(arguments,passedArguments));
         }
         else if (object instanceof ObjectProxy)
         {
	         Method method = findMethod(((ObjectProxy)object).getClassType(), function, passedArguments.length);
	         Object[] arguments = generateArguments(method.getParameterTypes(), passedArguments);
	         ((ObjectProxy)object).invoke(function, parseObjectArgs(arguments,passedArguments));
         }
         else
         {
	         Method method = findMethod(object, function, passedArguments.length);
	         Object[] arguments = generateArguments(method.getParameterTypes(), passedArguments);
	         method.invoke(object, arguments);
         }
      // Exception handlers
      } catch (InvalidFileException e) {
         throw e;
      } catch (Exception e) {
         throw new InvalidFileException("Error executing method "+function, e);
      }
   }

   // *****************************************************
   // ARGUMENT PROCESSING
   // *****************************************************
   /**
    * FIXME This method is modified by Ruthiano on 11/30/2005.
    * The only issue here is about strings like "a b   c" (with two 
    * or more spaces between two words) - in these cases, the resulting 
    * string belongs to "a b c" (with only one space). In other cases, 
    * this method works OK.
    */
   private String[] parseArguments(StringTokenizer tokenizer_) {
      List arguments = new Vector();
      
      //-------------------------------------------------------
      // TODO: tokenize a string properly
      //while (tokenizer_.hasMoreTokens())
      //   arguments.add(tokenizer_.nextToken());
      //-------------------------------------------------------
      boolean start = true; // start of a string   
      String token = "";

      while ( tokenizer_.hasMoreTokens() ) {
        String tmp = tokenizer_.nextToken();
        
        if ( start ) { 
        	// Adds a string with spaces
        	if (tmp.indexOf("\"") == 0 && tmp.charAt(tmp.length()-1) != '\"') {
            	token += tmp.substring(1) + " ";
            	start = false;        
            
            // Adds a "normal" string
        	} else {
        		if (tmp.indexOf("\"") == 0) { tmp = tmp.substring(1); }
        		if (tmp.charAt(tmp.length()-1) == '\"') { tmp = tmp.substring(0, tmp.length()-1); }
        		
        		arguments.add(tmp);
        	}
        	
        // A given string is already read in previous loop:
        } else {
    		if ( tmp.charAt(tmp.length()-1) == '\"' ) {
    			token += tmp.substring(0, tmp.length()-1);
    			arguments.add(token);
    			token = "";
    			start = true;
    		} else {
    			token += tmp.substring(0, tmp.length()) + " ";
    		}
        } // else
      } // while
           
      String[] result = new String[arguments.size()];

      for (int i = 0; i < arguments.size(); i++) {
         result[i] = (String)arguments.get(i);
      }
      
      return result;
   }
   
   public static Double unitConverter(String value) {
		String pattern = new String("^[0-9]+(\\.[0-9]+)?");
		Double out;
		
		// Default units:
		//   time  -> ms
		//   size  -> bytes
		//   rate  -> bytes/ms
		
		// Rate
		if (value.matches(pattern+"Mbps")) {
			out = new Double(value.substring(0, value.indexOf("Mbps")));
			out *= 125;
		}
		else if (value.matches(pattern+"Kbps")) {
			out = new Double(value.substring(0, value.indexOf("Kbps")));
			out /= 8;
		}
		else if (value.matches(pattern+"bps")) {
			out = new Double(value.substring(0, value.indexOf("bps")));
			out /= 8000;
		}
		else if (value.matches(pattern+"bytes?/ms")) {
			out = new Double(value.substring(0, value.indexOf("byte")));
		}
		
		// Size
		else if (value.matches(pattern+"Mbytes?")) {
			out = new Double(value.substring(0, value.indexOf("Mbyte")));
			out *= 1000000;
		}
		else if (value.matches(pattern+"Kbytes?")) {
			out = new Double(value.substring(0, value.indexOf("Kbyte")));
			out *= 1000;
		}
		else if (value.matches(pattern+"bytes?")) {
			out = new Double(value.substring(0, value.indexOf("byte")));
		}
		else if (value.matches(pattern+"bits?")) {
			out = new Double(value.substring(0, value.indexOf("bit")));
			out /= 8;
		}
		
		// Time
		else if (value.matches(pattern+"(h|H)")) {
			if (value.endsWith("h"))
				out = new Double(value.substring(0, value.indexOf("h")));
			else out = new Double(value.substring(0, value.indexOf("H")));
			out *= 3600000;
		}
		else if (value.matches(pattern+"(m|M)")) {
			if (value.endsWith("m"))
				out = new Double(value.substring(0, value.indexOf("m")));
			else out = new Double(value.substring(0, value.indexOf("M")));
			out *= 60000;
		}
		else if (value.matches(pattern+"(s|S)")) {
			if (value.endsWith("s"))
				out = new Double(value.substring(0, value.indexOf("s")));
			else out = new Double(value.substring(0, value.indexOf("S")));
			out *= 1000;
		}
		else if (value.matches(pattern+"(ms|MS|Ms)")) {
			if (value.endsWith("ms"))
				out = new Double(value.substring(0, value.indexOf("ms")));
			else out = new Double(value.substring(0, value.indexOf("M")));
		}
		else out = new Double(value);
		
		return out;
	}

   private Object[] generateArguments(Class[] classTypes_, String[] passedArguments_) throws InvalidFileException {
      String argument = null;
      try {
         if (passedArguments_.length != classTypes_.length)
            throw new InvalidFileException("Invalid number of arguments");
         Object[] arguments = new Object[classTypes_.length];
         int index = 0;
         for (int i = 0; i < passedArguments_.length; i++) {
            argument = passedArguments_[i];
            Class classType = classTypes_[index];

            int posDoubleQuote = argument.indexOf(":");
            if (posDoubleQuote>0) //Is there a method call on that object?
            {
            	String objname = argument.substring(0,posDoubleQuote);
            	if (symbols.get(objname) != null)
            	{
            		Object obj = symbols.get(objname);
            		Method meth = findMethod(obj,argument.substring(posDoubleQuote+1),0);
            		Object retdata;
					try {
						retdata = meth.invoke(obj, new Object[0]);
	                    arguments[index] = retdata;
	                    index++;
	                    continue;
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
            	}
            }
            // String support
            if (classType.equals(stringClass))
               arguments[index] = argument;
            // Handle Java's eight primitive types
            else if (classType.equals(Boolean.TYPE))
               arguments[index] = new Boolean(argument);
            else if (classType.equals(Byte.TYPE))
               arguments[index] = new Byte(argument);
            else if (classType.equals(Character.TYPE))
               arguments[index] = new Character(argument.charAt(0));
            else if (classType.equals(Double.TYPE))
            	// teste
               arguments[index] = new Double(unitConverter(argument));
            	//arguments[index] = new Double(argument);
            else if (classType.equals(Float.TYPE))
               // teste
               arguments[index] = new Float(unitConverter(argument).floatValue());
               //arguments[index] = new Float(argument);
            else if (classType.equals(Integer.TYPE)) {
               try {
            	  // teste
            	  arguments[index] = new Integer(unitConverter(argument).intValue());
                  //arguments[index] = new Integer(argument);
               } catch (NumberFormatException ne) {
                  Object object = symbols.get(argument);
                  do {
                     if (object != null) {
                        // An example of poor OO design.
                        if (nodeInterface.isAssignableFrom(object.getClass())) {
                           arguments[index] = new Integer( ((NodeInterface)object).getNetworkId() );
                           break;
                        } else if (groupClass.isAssignableFrom(object.getClass())) {
                           arguments[index] = new Integer( ((Group)object).getNetworkId() );
                           break;
                        }
                     }
                     throw new InvalidFileException("Parameter "+argument+" is not a number or a node/group id.");
                  } while (false);
               }
            } else if (classType.equals(Long.TYPE))
               // teste
               arguments[index] = new Long(unitConverter(argument).longValue());
               //arguments[index] = new Long(argument);
               
            else if (classType.equals(Short.TYPE))
               // teste
               arguments[index] = new Short(unitConverter(argument).shortValue());
               //arguments[index] = new Short(argument);
            // Handle an object reference
            else {
               Object object = symbols.get(argument);
               if (object != null && (classType.isAssignableFrom(object.getClass()) || (object instanceof NodeProxy) || (object instanceof ObjectProxy)))
                  arguments[index] = object;
               else
                  throw new InvalidFileException("Parameter "+argument+" is not of "+classType);
            }
            index++;
         }
         return arguments;

      // Exception handlers
      } catch (IndexOutOfBoundsException e) {
         if (debugMode) { System.err.println(e); e.printStackTrace(); }
         throw new InvalidFileException("Exceeding argument: "+argument);
      }
   }

   private String[] parseObjectArgs(Object[] objects_, String[] arguments_) throws InvalidFileException {
      String argument = null;
      try {
         String[] arguments = new String[objects_.length];
         for (int i = 0; i < objects_.length; i++) {
        	if (cloneOnClientInterface.isAssignableFrom(objects_[i].getClass()))
        	{
        		argument = "(" + arguments_[i] + "," + objects_[i].getClass().getName() + "," + ((CloneOnClient) objects_[i]).getConstructorParameters() + ")";
        	}
        	else if (objects_[i] instanceof NodeProxy)
        	{
        		argument = "{" + arguments_[i] + "," + ((NodeProxy) objects_[i]).getNetworkId() + "," + ((NodeProxy) objects_[i]).getClientId() + "," + ((NodeProxy) objects_[i]).getClientDescription() + "}"; 
        	}
        	else if (objects_[i] instanceof ObjectProxy)
        	{
        		argument = "{" + arguments_[i] + "," + ((ObjectProxy) objects_[i]).getClientId() + "," + ((ObjectProxy) objects_[i]).getClientDescription() + "}"; 
        	}
        	else if (objects_[i].getClass().isArray())
        	{
        		argument = "[";
        		for (int h=0;h<Array.getLength(objects_[i]);h++)
        		{
        			argument += Array.get(objects_[i],h).toString();
        			if (h<Array.getLength(objects_[i])-1)
        			{
        				argument += ",";
        			}
        		}
        		argument += "]";
        	}
        	else
        	{
        		argument = objects_[i].toString();//arguments_[i].toString();
        	}
           	arguments[i] = argument;
         }
         return arguments;

      // Exception handlers
      } catch (IndexOutOfBoundsException e) {
         if (debugMode) { System.err.println(e); e.printStackTrace(); }
         throw new InvalidFileException("Exceeding argument: "+argument);
      }
   }

   // *****************************************************
   // UTILITIES
   // *****************************************************

   /**
    * Locate a constructor in this object with
    * the specified number of parameters.
    */

   // TODO: This characterizes a partial support for
   // polymorphism -- ideally each parameter type
   // would be checked, not only the number of parameters.

   public static Constructor findConstructor(Class classType, int arguments) throws InvalidFileException {
      Constructor[] constructors = classType.getConstructors();
      int index = 0;
      while (
         (index<constructors.length) &&
         ! (constructors[index].getParameterTypes().length == arguments)
      )
         index++;
      if (! (index >= constructors.length))
         return constructors[index];
      throw new InvalidFileException("Constructor not found");
   }

   public static Constructor findConstructor(Class classType, Class[] arguments) throws InvalidFileException {
	      Constructor[] constructors = classType.getConstructors();
	      for (int i=0;i<constructors.length;i++)
	      {
	    	  if (constructors[i].getParameterTypes().length==arguments.length)
	    	  {
	    		  Class[] params = constructors[i].getParameterTypes();
	    		  boolean allMatch = true;
	    		  for (int j=0;j<params.length;j++)
	    		  {
	    			  if (params[j]!=arguments[j])
	    			  {
	    				  allMatch = false;
	    				  break;
	    			  }
	    		  }
	    		  if (allMatch)
	    		  {
	    			  return constructors[i];
	    		  }
	    	  }
	      }
	      throw new InvalidFileException("Constructor not found");
	}

   /**
    * Locate a method in this object with
    * the specified name and number of parameters.
    */

   // TODO: This characterizes a partial support for
   // polymorphism -- ideally each parameter type
   // would be checked, not only the number of parameters.

   public static Method findMethod(Object object, String function, int arguments) throws InvalidFileException {
       return findMethod(object.getClass(), function, arguments);	   
   }

	   private static Method findMethod(Class classType, String function, int arguments) throws InvalidFileException {
      try {
         while (classType != Class.forName("java.lang.Object")) {
            Method[] methods = classType.getDeclaredMethods();
            int index = 0;
            while (
               (index<methods.length) &&
               ! ((methods[index].getName().equals(function)) &&
                 (methods[index].getParameterTypes().length == arguments))
            )
               index++;
            if (index >= methods.length)
               classType = classType.getSuperclass();
            else
               return methods[index];
         }
         throw new InvalidFileException("Method "+function+" not found");

      // Exception handlers
      } catch (ClassNotFoundException e) {
         throw new InvalidFileException("Method "+function+" not found");
      }
   }

}
