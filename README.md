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

Now, the entity linker needs to be indexes using the populated Neo4J instance.

```bash
./linker.sh -index
```

When the script is called the first time, it will build a Docker image before it will run the container to index the entity linker.

## Usage
To link a table, call the `linker.sh` script with two arguments: the path to the table CSV file and the output directory.

```bash
./linker.sh -table <CSV_FILE> -output <OUTPUT_DIRECTORY>
```

For example, if a CSV table is in `tables/table.csv` and the results should be put in `results/`, call the `linker.sh` script the following way:

```bash
./linker.sh -table tables/table.csv -output results/
```

The results with linked entities of entities identified in `tables/table.csv` can now be found in `results/`.

## Development
To work with this repository, the following dependencies must be installed:

- Xapian
- Neo4J

The Docker file `develop.dockerfile` defines an image with a work environment for development of this repository.
The image contains all dependencies, so build the image with the following command, and develop from within a container on this image.

```bash
docker build -t keyword-kg-linker-dev .
```

You can run this container in most IDEs, but to manually run a container, use the following command:

```bash
docker run --rm -it -v ${PWD}:/home keyword-kg-linker-dev bash
```

On Windows, run the following command to start a container:

```bash
docker run --rm -it -v %cd%:/home keyword-kg-linker-dev bash
```