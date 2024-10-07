# keyword-kg-linker
Keyword-based table to knowledge graph entity linking.

## Setup
Store all the KG files in a single directory, and make sure all the files are in N-Triples format.
Now, the entity linker needs to be indexed using the KG files and Word2Vec model.

```bash
./linker.sh -dir <DIRECTORY> -kg <KG_DIRECTORY> -config <CONFIG_FILE>
```

You must specify the directory in which to save the index files, the directory containing the KG files, and the configuration file.
See `config.json` for the content of this configuration file.
It must specify number of candidate entities to disambiguate.

When the script is called the first time, it will build a Docker image before it will run the container to index the entity linker.

## Usage
To link a table, call the `linker.sh` script with 5 arguments: the path to the table CSV file, the output directory, the index directory, the configuration file, and the type of entity linking.

```bash
./linker.sh -tables <CSV_TABLE_DIRECTORY> -output <OUTPUT_DIRECTORY> -dir <DIRECTORY> -config <CONFIG_FILE> -type <ENTITY_LINKING_TYPE>
```

There are two types of entity linking: keyword-based (`keyword`) and embedding-based (`embedding`).
For the embedding-based, it used uncased BERT embeddings.

For example, if a CSV tables are in `tables/`, the results should be put in `results/`, and the index files are in `data/`, call the `linker.sh` script the following way to use keyword-based entity linking:

```bash
./linker.sh -tables tables/ -output results/ -dir data/ -config config.json -type keyword
```

The results with linked entities of entities identified in `tables/table.csv` can now be found in `results/` using the indexes stored in `data/`.
The accuracy and efficiency can be tuned with the weights and the number of candidates specified in the configuration file, which determines the size of the set of candidate entities.
Note that it is also possible to link to a single table by for example passing `-tables some_table.csv` as argument.
