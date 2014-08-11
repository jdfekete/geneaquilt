# GeneaQuilts

GeneaQuilts is a new visualization technique for representing large
genealogies of up to several thousand individuals. The visualization
takes the form of a diagonally-filled matrix, where rows are
individuals and columns are nuclear families. The GeneaQuilts system
includes an overview, a timeline, search and filtering components, and
a new interaction technique called Bring & Slide that allows fluid
navigation in very large genealogies.

## Maven

GeneaQuilts uses Maven. To compile, simply type:

`mvn install`

Everything will be in the `target/` directory. In particular, two
scripts to start the program (run.sh and run.bat).
Alternatively, you can launch GeneaQuilts by using the generated jar file.

## Developing with ECLIPSE

To import the GeneaQuilts project in Eclipse, you first need to install the Subclipse plugin:
- Go the the Help menu -> Install new software...
- Add the site  http://subclipse.tigris.org/update_1.6.x, select "Subclipse" and proceed.
Then checkout the GeneaQuilts project:
- Menu File -> New Project... -> SVN -> Checkout projects from SVN
- Add the repository location http://scm.gforge.inria.fr/svn/geneaquilt
- Select the directory trunk/geneaquilt and proceed. 

To compile GeneaQuilts under Eclipse, you need to install the Maven plugin:
- Go to the help menu -> Install new software...
- Add the site http://m2eclipse.sonatype.org/sites/m2e, select "Maven integration for Eclipse" and proceed.
- Restart Eclipse. Geneaquilt should compile itself automatically.


## Command Line

