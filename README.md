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
./linker.sh -dir <DIRECTORY> -config <CONFIG_FILE>
```

You must specify the directory in which to save the index files and the configuration file.
See `config.json` for the content of this configuration file.
It must specify a set of Neo4J predicates to retrieve entity string representations.
For example, the RDFS label corresponds to `rdfs__label` in Neosemantics in Neo4J.
Second, the configuration file contains weights of different entity components.
`LABEL` is the weight of the entity label, `DESCRIPTION` is the weight of the entity string representations, and `SUB-DESCRIPTION` is the weight of entity descriptions of neighboring entities.
Last, it must specify number of candidate entities to disambiguate.

When the script is called the first time, it will build a Docker image before it will run the container to index the entity linker.

## Usage
To link a table, call the `linker.sh` script with 5 arguments: the path to the table CSV file, the output directory, the index directory, the configuration file, and the type of entity linking.

```bash
./linker.sh -table <CSV_FILE> -output <OUTPUT_DIRECTORY> -dir <DIRECTORY> -config <CONFIG_FILE> -type <ENTITY_LINKING_TYPE>
```

There are two types of entity linking: keyword-based (`keyword`) and embedding-based (`embedding`).
For the embedding-based, it used uncased BERT embeddings.

For example, if a CSV table is in `tables/table.csv`, the results should be put in `results/`, and the index files are in `data/`, call the `linker.sh` script the following way to use keyword-based entity linking:

```bash
./linker.sh -table tables/table.csv -output results/ -dir data/ -config config.json -type keyword
```

The results with linked entities of entities identified in `tables/table.csv` can now be found in `results/` using the indexes stored in `data/`.
The accuracy and efficiency can be tuned with the weights and the number of candidates specified in the configuration file, which determines the size of the set of candidate entities.