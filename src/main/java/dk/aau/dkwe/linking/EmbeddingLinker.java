package dk.aau.dkwe.linking;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.robrua.nlp.bert.Bert;

import java.util.Set;

public class EmbeddingLinker extends MentionLinker
{
    private final Set<String> entities;
    private static final Bert BERT;
    private boolean isClosed = false;
    private final Cache<String, String> cache;
    private static final String MODEL_PATH = "com/robrua/nlp/easy-bert/bert-uncased-L-12-H-768-A-12";

    static
    {
        BERT = Bert.load(MODEL_PATH);
    }

    public EmbeddingLinker(Set<String> entities)
    {
        this.entities = entities;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build();
    }

    @Override
    protected String performLink(String mention)
    {
        if (this.isClosed)
        {
            throw new IllegalStateException("Class has been closed");
        }

        String cachedLink = this.cache.getIfPresent(mention);

        if (cachedLink != null)
        {
            return cachedLink;
        }

        float[] mentionEmbedding = BERT.embedSequence(mention);
        double highestScore = -1.0;
        String bestEntity = null;

        for (String entity : this.entities)
        {
            String[] split = entity.split("/");
            String entityText = split[split.length - 1].replace('_', ' ');
            float[] entityEmbedding = BERT.embedSequence(entityText);
            double score = cosine(mentionEmbedding, entityEmbedding);

            if (score > highestScore)
            {
                highestScore = score;
                bestEntity = entity;
            }
        }

        if (bestEntity != null)
        {
            this.cache.put(mention, bestEntity);
        }

        return bestEntity;
    }

    private static double cosine(float[] embedding1, float[] embedding2)
    {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < embedding1.length; i++)
        {
            dotProduct += embedding1[i] * embedding2[i];
            normA += Math.pow(embedding1[i], 2);
            normB += Math.pow(embedding2[i], 2);
        }

        if (normA == 0 || normB == 0)
        {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Override
    public void close() throws Exception
    {
        BERT.close();
        this.isClosed = true;
    }
}
