package dk.aau.dkwe.load;

import com.robrua.nlp.bert.Bert;
import dk.aau.dkwe.candidate.EmbeddingIndex;
import dk.aau.dkwe.candidate.Index;
import dk.aau.dkwe.candidate.IndexBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Indexing og KG entity embeddings
 */
public class EmbeddingIndexer implements Indexer<String, List<Double>>
{
    private EmbeddingIndex index = new EmbeddingIndex();
    Set<String> entities;
    private File directory;
    private boolean isClosed;
    private static final Bert BERT;
    private static final String MODEL_PATH = "com/robrua/nlp/easy-bert/bert-uncased-L-12-H-768-A-12";

    static
    {
        BERT = Bert.load(MODEL_PATH);
    }

    public static EmbeddingIndexer create(Set<String> entities, File indexDirectory)
    {
        return new EmbeddingIndexer(entities, indexDirectory);
    }

    private EmbeddingIndexer(Set<String> entities, File indexDirectory)
    {
        this.entities = entities;
        this.directory = indexDirectory;
        this.isClosed = false;
    }

    /**
     * Getter of the constructed embeddings index
     * @return
     */
    @Override
    public Index<String, List<Double>> getIndex()
    {
        return this.index;
    }

    /**
     * Constructs the index of BERT embeddings of KG entities
     * @return True if the index was constructed successfully, otherwise false
     */
    @Override
    public boolean constructIndex()
    {
        if (this.isClosed)
        {
            return false;
        }

        for (String entity : this.entities)
        {
            List<Double> embedding = embedding(entity);
            this.index.add(entity, embedding);
        }

        this.isClosed = true;
        BERT.close();
        IndexBuilder.embeddingBuilder(this.directory, this.index);

        return true;
    }

    /**
     * Retrieve BERT embeddings of given text
     * Warning: This method cannot be called after constructing the index, as the BERT class is closed.
     */
    public static List<Double> embedding(String text)
    {
        float[] embedding = BERT.embedSequence(text);
        List<Double> embeddingLst = new ArrayList<>(embedding.length);

        for (float e : embedding)
        {
            embeddingLst.add((double) e);
        }

        return embeddingLst;
    }
}
