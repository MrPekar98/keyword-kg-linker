package dk.aau.dkwe.load;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;
import dk.aau.dkwe.candidate.Document;
import dk.aau.dkwe.candidate.EmbeddingIndex;
import dk.aau.dkwe.candidate.Index;
import dk.aau.dkwe.candidate.IndexBuilder;

import java.io.File;
import java.io.IOException;
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
    private boolean isParallelized;
    private final Object mtx = new Object();
    private static Word2VecModel model = null;
    private static Searcher searcher = null;
    private static final File MODEL_PATH = new File("/word2vec/model.bin");
    private static final int THREADS = 4;

    static
    {
        try
        {
            model = Word2VecModel.fromBinFile(MODEL_PATH);
            searcher = model.forSearch();
        }

        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static EmbeddingIndexer create(Set<Document> documents, File indexDirectory, boolean parallelized)
    {
        return new EmbeddingIndexer(documents, indexDirectory, parallelized);
    }

    private EmbeddingIndexer(Set<Document> documents, File indexDirectory, boolean parallelized)
    {
        this.documents = documents;
        this.directory = indexDirectory;
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
     * Constructs the index of Word2Vec embeddings of KG entities
     * @return True if the index was constructed successfully, otherwise false
     */
    @Override
    public boolean constructIndex()
    {
        if (this.isParallelized)
        {
            insertParallel();
        }

        else
        {
            insertEntities(this.documents);
        }

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
            String text = !document.label().isEmpty() ? document.label() : document.uri();
            long start = System.currentTimeMillis();
            List<Double> embedding = embedding(text);
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Elapsed: " + elapsed);

            if (embedding != null)
            {
                synchronized (this.mtx)
                {
                    this.index.add(document.uri(), embedding);
                }
            }
        }
    }

    /**
     * Retrieve Word2Vec embeddings of given text
     */
    public static List<Double> embedding(String text)
    {
        try
        {
            return searcher.getRawVector(text.replace(" ", "_"));
        }

        catch (Searcher.UnknownWordException e)
        {
            return null;
        }
    }
}
