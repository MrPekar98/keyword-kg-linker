package dk.aau.dkwe.load;

import com.robrua.nlp.bert.Bert;
import dk.aau.dkwe.candidate.Document;
import dk.aau.dkwe.candidate.EmbeddingIndex;
import dk.aau.dkwe.candidate.Index;
import dk.aau.dkwe.candidate.IndexBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Indexing og KG entity embeddings
 */
public class EmbeddingIndexer implements Indexer<String, List<Double>>
{
    private final EmbeddingIndex index = new EmbeddingIndex();
    private Set<Document> documents;
    private File directory;
    private boolean isClosed;
    private boolean isParallelized;
    private final Object mtx = new Object();
    private static final Bert BERT;
    private static final String MODEL_PATH = "com/robrua/nlp/easy-bert/bert-uncased-L-12-H-768-A-12";
    private static final int THREADS = 4;

    static
    {
        BERT = Bert.load(MODEL_PATH);
    }

    public static EmbeddingIndexer create(Set<Document> documents, File indexDirectory, boolean parallelized)
    {
        return new EmbeddingIndexer(documents, indexDirectory, parallelized);
    }

    private EmbeddingIndexer(Set<Document> documents, File indexDirectory, boolean parallelized)
    {
        this.documents = documents;
        this.directory = indexDirectory;
        this.isClosed = false;
        this.isParallelized = parallelized;
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

        if (this.isParallelized)
        {
            insertParallel();
        }

        else
        {
            insertEntities(this.documents);
        }

        this.isClosed = true;
        BERT.close();
        IndexBuilder.embeddingBuilder(this.directory, this.index);

        return true;
    }

    private void insertParallel()
    {
        List<Document> documentList = new ArrayList<>(this.documents);
        ExecutorService threadPool = Executors.newFixedThreadPool(THREADS);
        List<Future<?>> tasks = new ArrayList<>();
        final int entityCount = this.documents.size(), splitSize = 10000, iterations = (int) Math.ceil((double) entityCount / splitSize);

        for (int i = 0; i < iterations; i++)
        {
            List<Document> subset = documentList.subList(i * splitSize, Math.min((i + 1) * splitSize, entityCount - 1));
            Future<?> task = threadPool.submit(() -> insertEntities(new HashSet<>(subset)));
            tasks.add(task);
        }

        tasks.forEach(f -> {
            try
            {
                f.get();
            }

            catch (InterruptedException | ExecutionException ignored) {}
        });
    }

    private void insertEntities(Set<Document> documents)
    {
        for (Document document : documents)
        {
            List<Double> embedding = embedding(document.uri());

            synchronized (this.mtx)
            {
                this.index.add(document.uri(), embedding);
            }
        }
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
