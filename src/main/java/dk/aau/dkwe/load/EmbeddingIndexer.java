package dk.aau.dkwe.load;

import dk.aau.dkwe.candidate.Document;
import dk.aau.dkwe.candidate.EmbeddingIndex;
import dk.aau.dkwe.candidate.Index;
import dk.aau.dkwe.candidate.IndexBuilder;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private static Word2Vec model = null;
    private static final File MODEL_PATH = new File("/word2vec/model.bin");
    private static final int THREADS = 4;

    static
    {
        model = WordVectorSerializer.readWord2VecModel(MODEL_PATH);
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
        String[] words = text.split(" ");
        List<Double> aggregatedEmbedding = null;

        for (String word : words)
        {
            if (!model.hasWord(word))
            {
                continue;
            }

            double[] array = model.getWordVector(word);
            List<Double> embedding = new ArrayList<>(array.length);

            for (double val : array)
            {
                embedding.add(val);
            }

            if (aggregatedEmbedding == null)
            {
                aggregatedEmbedding = embedding;
            }

            else
            {
                List<Double> copy = new ArrayList<>(aggregatedEmbedding);
                aggregatedEmbedding = IntStream.range(0, embedding.size())
                        .mapToObj(i -> copy.get(i) + embedding.get(i))
                        .collect(Collectors.toList());
            }
        }

        return aggregatedEmbedding;
    }
}
