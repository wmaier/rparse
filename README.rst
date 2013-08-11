 rparse - a data-driven parser for Probabilistic LCFRS
=====================================================================

Introduction
------------

rparse is a data-driven parser for Probabilistic Linear Context-Free
Rewriting Systems (PLCFRS). It has been developed at the Emmy Noether
group of Prof. Dr. Laura Kallmeyer at the University of Tübingen,
Germany and is now maintained at her project "Grammar Formalisms
beyond Context-Free Grammars and their  use for Machine Learning
Tasks" at the Department for Computational Linguistics at the
Institute for Language and Information at the University of
Düsseldorf, Germany. The project is sponsored by Deutsche
Forschungsgemeinschaft (DFG).  

In order to reference this parser, please cite

  Laura Kallmeyer and Wolfgang Maier (2013):  *Data-driven Parsing 
  using Probabilistic Linear Context-Free Rewriting Systems." 
  Computational Linguistics, 39(1). 

for a general description of the parser and constituency parsing
evaluation, 

  Wolfgang Maier and Laura Kallmeyer (2010):  *Discontinuity and
  Non-Projectivity: Using Mildly Context-Sensitive Formalisms for
  Data-Driven Parsing.* In: Proceedings of the 10th International
  Conference on Tree Adjoining Grammars and Related Formalisms
  (TAG+10), Yale University.  

for grammar-based parsing of non-projective dependencies and its
evaluation, and

  Wolfgang Maier, Miriam Kaeshammer and Laura Kallmeyer (2012):
  *Data-Driven PLCFRS Parsing Revisited: Restricting the Fan-Out to
  Two.* In: Proceedings of the Eleventh International Conference on
  Tree Adjoining Grammars and Related Formalisms (TAG+11), Paris,
  France.    

for parsing with a (2,2)-PLCFRS.

The code is released under the GNU General Public Licence (GPL) 2.0 or
higher. The license text can be found in the file license.txt. The
release include a copy of the library jgrapht, which is licensed under
the GNU Lesser General Public License (LGPL) 2.1. The full license
texts of the GPL 2.0 and the LGPL 2.1 can be found at 
http://www.gnu.org/licenses/gpl-2.0 and
http://www.gnu.org/licenses/old-licenses/lgpl-2.1.  

This is version 2.0. For more information, update notifications and
contact information, please refer to the rparse homepage at
http://www.phil.hhu.de/rparse.


Quick Start
-----------


Compile
~~~~~~~

The parser is written in Java, Java 7 is required. In order to run it,
you need the jgrapht library. jgrapht is included with this release in
the /lib directory. You have to compile the parser against your copy
of jgrapht. This can be done using the ant build file included in the
rparse package, to which you have to pass the location of the compiled
jgrapht library as follows:  

``$ ant -Djgraph.path="/path/to/jgrapht/"``

This will build a jar file rparse.jar in the rparse package. The
classpath in the manifest of the jar file will contain the path to
jgrapht. If you want to use a different copy of jgrapht, pass both
jars in the class path and use the rparse entry point
``de.tuebingen.rparse.ui.Rparse``. 

Run
~~~

The default format for training is the export format. In order to
train the parser on NeGra, run something like the following::

  $ java -jar rparse.jar \
         -doTrain \
         -train path-to-training-corpus \
         -headFinder negra \
         -saveModel path-to-trained-model

In order to parser something with the trained model, you need to pass
the parser a POS tagged sentence (rparse cannot do POS tagging on its
own yet). The default input format is one word+POS tag combination per
line, separated by a slash. A call to the parser would look something
like the following::

  $ java -jar rparse.jar \
         -doParse \
         -test path-to-test-corpus \
         -readModel path-to-trained-model

In order to evaluate your output, use::

  $ java -jar rparse.jar \
         -doEval \
         -test path-to-test-corpus \
         -readModel path-to-trained-model

Make sure you also check the output of java -jar rparse.jar -help.

Changelog
------------------

**New in version 2.0**

Fast parser for (2,2)-PLCFRS. Can be accessed via -parserType
cyktwo. Default in mode -doParse remains old parser 
(-parserType cyk).


**New in Version 1.0.1**

Several bugfixes
