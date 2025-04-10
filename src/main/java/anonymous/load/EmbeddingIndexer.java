package anonymous.load;

import anonymous.candidate.Document;
import anonymous.candidate.EmbeddingIndex;
import anonymous.candidate.Index;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Indexing og KG entity embeddings
 */
public class EmbeddingIndexer implements Indexer<String, List<Double>>, Progressable
{
    private final EmbeddingIndex index = new EmbeddingIndex();
    private Set<Document> documents;
    private final Object mtx = new Object();
    private boolean isClosed = false;
    private double progress = 0.0;
    private final int BATCH_SIZE = 1000;
    private static final String SENTENCE_FILE = "/home/sentences.txt";
    private static final String EMBEDDINGS_FILE = "/home/embeddings.txt";

    public static EmbeddingIndexer create(Set<Document> documents)
    {
        return new EmbeddingIndexer(documents);
    }

    private EmbeddingIndexer(Set<Document> documents)
    {
        this.documents = documents;
    }

    /**
     * Getter of the constructed embeddings index
     * @return Index instance
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
        try
        {
            return insertEntities();
        }

        catch (Exception e)
        {
            return false;
        }

        finally
        {
            this.index.close();
            this.isClosed = true;
        }
    }

    private boolean insertEntities()
    {
        List<Document> documentList = new ArrayList<>(this.documents);
        int entities = this.documents.size();
        int iterations = (int) Math.ceil((double) entities / BATCH_SIZE);

        for (int i = 0; i < iterations; i++)
        {
            List<Document> batch = documentList.subList(i * BATCH_SIZE, Math.min((i + 1) * BATCH_SIZE, entities - 1));
            writeSentences(batch);

            try
            {
                Process python = Runtime.getRuntime().exec("python3 /home/embeddings.py " + SENTENCE_FILE + " " + EMBEDDINGS_FILE);
                int ret = python.waitFor();

                if (ret != 0)
                {
                    cleanup();
                    return false;
                }

                List<List<Double>> embeddings = readEmbeddings();
                int embeddingsCount = embeddings.size();
                cleanup();

                for (int entity = 0; entity < embeddingsCount; entity++)
                {
                    this.index.add(batch.get(entity).uri(), embeddings.get(entity));
                }
            }

            catch (IOException | InterruptedException e)
            {
                return false;
            }

            synchronized (this.mtx)
            {
                double fraction = (double) batch.size() / documentList.size();
                this.progress += fraction;
            }
        }

        return true;
    }

    private static void writeSentences(List<Document> documents)
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SENTENCE_FILE)))
        {
            for (Document document : documents)
            {
                String text = "The graph model contains an entity describing ";
                text += !document.label().isEmpty() ? document.label() :
                        document.uri().substring(document.uri().lastIndexOf('/') + 1).replace("_", " ");
                writer.write(text);
                writer.newLine();
            }

            writer.flush();
        }

        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static List<List<Double>> readEmbeddings()
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(EMBEDDINGS_FILE)))
        {
            String line;
            List<List<Double>> embeddings = new ArrayList<>();

            while ((line = reader.readLine()) != null)
            {
                List<Double> embedding = new ArrayList<>();

                for (String strVal : line.split(" "))
                {
                    try
                    {
                        double val = Double.parseDouble(strVal);
                        embedding.add(val);
                    }

                    catch (NumberFormatException ignored) {}
                }

                embeddings.add(embedding);
            }

            return embeddings;
        }

        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static void cleanup()
    {
        File sentencesFile = new File(SENTENCE_FILE);
        sentencesFile.delete();

        File embeddingsFile = new File(EMBEDDINGS_FILE);
        embeddingsFile.delete();
    }

    /**
     * Retrieve Word2Vec embeddings of given text
     */
    public static List<Double> embedding(String text)
    {
        Document doc = new Document("", text, "");
        writeSentences(List.of(doc));

        try
        {
            Process python = Runtime.getRuntime().exec("python3 /home/embeddings.py " + SENTENCE_FILE + " " + EMBEDDINGS_FILE);
            int ret = python.waitFor();

            if (ret != 0)
            {
                cleanup();
                return null;
            }

            return readEmbeddings().get(0);
        }

        catch (IOException | InterruptedException e)
        {
            return null;
        }

        finally
        {
            cleanup();
        }
    }

    @Override
    public double progress()
    {
        synchronized (this.mtx)
        {
            return this.progress;
        }
    }
}
