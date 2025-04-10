package dk.aau.dkwe.load;

import dk.aau.dkwe.Config;
import dk.aau.dkwe.candidate.Document;
import dk.aau.dkwe.candidate.Index;
import dk.aau.dkwe.candidate.IndexBuilder;
import dk.aau.dkwe.candidate.LuceneIndexBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Indexes KG entities using 4 fields: entity URI, label, set of string literals, and label and string literals of direct KG neighbors
 */
public class LuceneIndexer implements Indexer<String, Map<String, Double>>, Progressable
{
    private final File directory;
    private final Set<Document> documents;
    private final Config config;

    public static LuceneIndexer create(Config config, File indexDirectory, Set<Document> documents)
    {
        return new LuceneIndexer(config, indexDirectory, documents);
    }

    private LuceneIndexer(Config config, File indexDirectory, Set<Document> documents)
    {
        this.directory = indexDirectory;
        this.documents = documents;
        this.config = config;
    }

    /**
     * Getter to constructed Lucene index
     * @return Lucene index
     */
    @Override
    public Index<String, Map<String, Double>> getIndex()
    {
        try
        {
            return IndexBuilder.luceneBuilder(this.directory, this.config.candidates());
        }

        catch (IOException e)
        {
            return null;
        }
    }

    /**
     * Constructs the Lucene index of 4 fields
     * The indexing can be parallelized using multiple threads
     * @return True if index is constructed successfully, otherwise false
     */
    @Override
    public boolean constructIndex()
    {
        try
        {
            IndexBuilder.luceneBuilder(this.documents, this.directory);
            return true;
        }

        catch (IOException e)
        {
            return false;
        }
    }

    @Override
    public double progress()
    {
        return LuceneIndexBuilder.progress();
    }
}
