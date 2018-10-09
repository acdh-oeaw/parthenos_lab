# sai_nlp_chain

## purpose
The purpose of the project is to create a chain of the three existing data miner applications StanbolWrapper (here published as sai_stanbol_wrapper), Distanbol (sai_distanbol) and the Web App Publisher. The input of this application is a textfile. This textfile is send to the Stanbol Wrapper, its enriched output is transformed to html which is 
futher on published by the Web App Publisher. The output of the whole application is a link to a file which contains a redirect to the Web App Publisher.  

## how it works
As every SAI deployable Java application the sai_stanbol_wrapper is a stand alone application, packed into a jar. The call is: 
`java -cp <name of the jar> at.ac.oeaw.acdh.parthenos_lab.nlpchain.Main <input file> <minimum confidence level>`

`<input file>`
Fully qualified path to the input file (or relative to the working directory)

`<minimum confidence level>`
A minimum confidence level between 0 (show everything) and 1.

**After mavenized build the target directory contains two jar-files. Please use the jar-file with dependencies. If you run the program locally you must make sure, that a file globalvariables.csv is available in your working directory, which contains a valid username (gcube_username) and the corresponding token (gcube_token).** 

