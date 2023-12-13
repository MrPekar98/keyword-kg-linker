package dk.aau.dkwe.candidate;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Factory class to instantiate indexes
 */
public final class IndexBuilder
{
    private static final String LUCENE_FOLDER = "lucene/";
    private static final String EMBEDDINGS_FOLDER = "embeddings/";

    public static LuceneIndex luceneBuilder(Set<Document> documents, File directory) throws IOException
    {
        File folder = new File(directory.getAbsolutePath() + "/" + LUCENE_FOLDER);

        if (!folder.isDirectory())
        {
            folder.mkdirs();
        }

        LuceneIndexBuilder.build(documents, folder);
        return luceneBuilder(directory, 50);
    }

    public static LuceneIndex luceneBuilder(File directory, int resultSize) throws IOException
    {
        File folder = new File(directory.getAbsolutePath() + "/" + LUCENE_FOLDER);
        Directory dir = FSDirectory.open(folder.toPath());
        DirectoryReader dirReader = DirectoryReader.open(dir);
        return new LuceneIndex(new IndexSearcher(dirReader), resultSize);
    }

    public static EmbeddingIndex embeddingBuilder(File directory, EmbeddingIndex index)
    {
        File folder = new File(directory.getAbsolutePath() + "/" + EMBEDDINGS_FOLDER);

        if (!folder.isDirectory())
        {
            folder.mkdirs();
        }

        EmbeddingIndexBuilder.write(folder, index);
        return index;
    }

    public static EmbeddingIndex embeddingBuilder(File directory)
    {
        File folder = new File(directory.getAbsolutePath() + "/" + EMBEDDINGS_FOLDER);
        return EmbeddingIndexBuilder.read(folder);
    }
}
