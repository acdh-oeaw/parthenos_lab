# sai_stanbol_wrapper

## purpose
The purpose of the project is to stream an text input file to an external Stanbol installation, make a text enhancement and to return an enhanced output file. 

## how it works
As every SAI deployable Java application the sai_stanbol_wrapper is a stand alone application, packed into a jar. The call is: 
`java -cp <name of the jar> at.ac.oeaw.acdh.nerlix.stanbol.SAIStanbolWrapper <input file> <enhancement chain> <output format>`

<input file>
Fully qualified path to the input file (or realtive to the working directory)

<enhancement chain>
The enhancement chain to use. Currently the are the options COUNTRIES (leads to enhancement chain *geoNames_PCLI*), CITIES (*geoNames_PPLC*) and LOCATIONS 
(geoNames_SPAsubset). Any other value will lead to the enhancement chain *dbpedia-fst-linking*. 

<output format>
Allowed values are RDF_XML (for *application/rdf+xml*), RDF_JSON (*application/rdf+json*), TURTLE (*text/turtle*) and N_TRIPLES (*text/rdf+nt*). Any other 
value will produce an output in format *application/json*. 

The server with the Stanbol installation is hard coded (see line 34 of class the SAIStanbolWrapper) and the names of the enhancement chains only make 
sense in the context of this Stanbol instance.  

