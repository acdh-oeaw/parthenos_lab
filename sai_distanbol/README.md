# sai_distanbol

## purpose
The purpose of the project is to take a json formated file as it comes from the sai_stanbol_wrapper as input and to make it human readable by transforming it 
into html. The html is, together with some includes like logos, css and jaca script, zipped into a file which is the output of sai_distanbol. This output serves 
itself as an input for the web app publisher from d4science.  

## how it works
As every SAI deployable Java application the sai_distanbol is a stand alone application, packed into a jar. The call is: 
`java -cp <name of the jar> at.ac.oeaw.acdh.nerlix.stanbol.SAIDistanbol <input file> <minimum confidence level>`

`<input file>`
Fully qualified path to the input file (or relative to the working directory). The input file is a json formated output file from the sai_stanbol_wrapper. 

`<minimum confidence level>`
A minimum confidence level between 0 (show everything) and 1

** The sai_distanbol project is a fork of the Distanbol project, written by coy123 and published on github (see https://github.com/acdh-oeaw/Distanbol). ** 

