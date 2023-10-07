# keyword-kg-linker
Keyword-based table to knowledge graph entity linking.

## Setup
Start a Neo4J instance with the following call.
Add the directory of the knowledge graph files if this is the first time starting the Neo4J instance.

```bash
./neo4j.sh [<KG_DIR>]
```

If this is the first time, Neo4J will be setup, Neosemantics installed, and Neo4J populated with the knowledge graph triples.
To start the instance after the first time, simply start the script without any parameters.
The entity linker will not work without a populated Neo4J instance running.

## Usage
TODO