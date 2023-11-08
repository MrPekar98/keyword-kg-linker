# keyword-kg-linker
Keyword-based table to knowledge graph entity linking.

## Setup
Start a Neo4J instance with the following call.
Add the directory of the knowledge graph files if this is the first time starting the Neo4J instance.
Note that the knowledge graph files must be Turtle files.

```bash
./neo4j.sh [<KG_DIR>]
```

For example, if you have all of your Turtle file in `kg_files/`, then you need to run the following command:

```bash
./neo4j.sh kg_files/
```

If this is the first time, Neo4J will be setup, Neosemantics installed, and Neo4J populated with the knowledge graph triples.
To start the instance after the first time, simply start the script without any parameters.
The entity linker will not work without a populated Neo4J instance running.

Now, the entity linker needs to be indexed using the populated Neo4J instance.

```bash
./linker.sh -dir <DIRECTORY> -predicate-file <PREDICATES_FILE>
```

You must specify the directory in which to save the index files and the KG predicates in a file to retrieve entity string representations in Neo4J.
When the script is called the first time, it will build a Docker image before it will run the container to index the entity linker.

The predicate file must be a list separated by newline of Neo4J Neosemantics predicates.
For example, the RDFS label corresponds to `rdfs__label` in Neosemantics.
An example of such file is given below:

```
rdfs__label
ns1__shortname
ns1__surname
ns1__alias
```

## Usage
To link a table, call the `linker.sh` script with two arguments: the path to the table CSV file and the output directory.

```bash
./linker.sh -table <CSV_FILE> -output <OUTPUT_DIRECTORY> -dir <DIRECTORY> -candidates <CANDIDATE_SET_SIZE>
```

For example, if a CSV table is in `tables/table.csv`, the results should be put in `results/`, and the index files are in `data/`, call the `linker.sh` script the following way:

```bash
./linker.sh -table tables/table.csv -output results/ -dir data/ -candidates 50
```

The results with linked entities of entities identified in `tables/table.csv` can now be found in `results/` using the indexes stored in `data/`.
The accuracy and efficiency can be tuned with the `-candidates` parameter, which determines the size of the set of candidate entities.