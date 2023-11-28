package dk.aau.dkwe.load;

import com.robrua.nlp.bert.Bert;
import dk.aau.dkwe.candidate.EmbeddingIndex;
import dk.aau.dkwe.candidate.Index;
import dk.aau.dkwe.candidate.IndexBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Override
    public Index<String, List<Double>> getIndex()
    {
        return this.index;
    }

    @Override
    public boolean constructIndex()
    {
        if (this.isClosed)
        {
            return false;
        }

        for (String entity : this.entities)
        {
            float[] embedding = BERT.embedSequence(entity);
            List<Double> embeddingsList = new ArrayList<>(embedding.length);

            for (float e : embedding)
            {
                embeddingsList.add((double) e);
            }

            this.index.add(entity, embeddingsList);
        }

        this.isClosed = true;
        BERT.close();
        IndexBuilder.embeddingBuilder(this.directory, this.index);

        return true;
    }
}
