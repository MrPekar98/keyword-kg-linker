package dk.aau.dkwe.linking;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dk.aau.dkwe.candidate.EmbeddingIndex;
import dk.aau.dkwe.candidate.EntityEmbedding;
import dk.aau.dkwe.load.EmbeddingIndexer;

import java.util.Iterator;
import java.util.List;

/**
 * Links a mention to a KG entity using BERT embeddings
 */
public class EmbeddingLinker extends MentionLinker
{
    private final Cache<String, String> cache;
    private final EmbeddingIndex index;

    public EmbeddingLinker()
    {
        this.index = new EmbeddingIndex();
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build();
    }

    /**
     * Finds the KG entity with the highest cosine similarity to the embeddings of the entity mention
     * @param mention Entity mention
     * @return KG entity with the highest cosine similarity
     */
    @Override
    protected String performLink(String mention)
    {
        String cachedLink = this.cache.getIfPresent(mention);

        if (cachedLink != null)
        {
            return cachedLink;
        }

        List<Double> mentionEmbedding = EmbeddingIndexer.embedding(mention);
        EntityEmbedding mentionEntity = new EntityEmbedding(mention, mentionEmbedding);
        Iterator<EntityEmbedding> neighbors = this.index.neighbors(mentionEmbedding, 10).iterator();
        double highestScore = -1.0;
        String bestEntity = null;

        while (neighbors.hasNext())
        {
            EntityEmbedding embedding = neighbors.next();
            double score = mentionEntity.cosine(embedding);

            if (score > highestScore)
            {
                highestScore = score;
                bestEntity = embedding.uri();
            }
        }

        if (bestEntity != null)
        {
            this.cache.put(mention, bestEntity);
        }

        return bestEntity;
    }

    @Override
    public void close()
    {
        this.index.close();
    }
}
