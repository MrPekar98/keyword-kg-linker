package dk.aau.dkwe.linking;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dk.aau.dkwe.candidate.EmbeddingIndex;
import dk.aau.dkwe.candidate.IndexBuilder;
import dk.aau.dkwe.load.EmbeddingIndexer;
import dk.aau.dkwe.utils.MathUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Links a mention to a KG entity using BERT embeddings
 */
public class EmbeddingLinker extends MentionLinker
{
    private final Cache<String, String> cache;
    private final EmbeddingIndex index;

    public EmbeddingLinker(File indexDir)
    {
        this.index = IndexBuilder.embeddingBuilder(indexDir);
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
        Iterator<String> entities = this.index.keys();
        double highestScore = -1.0;
        String bestEntity = null;

        while (entities.hasNext())
        {
            String entity = entities.next();
            List<Double> embedding = this.index.lookup(entity);

            if (embedding != null)
            {
                double score = MathUtils.cosine(mentionEmbedding, embedding);

                if (score > highestScore)
                {
                    highestScore = score;
                    bestEntity = entity;
                }
            }
        }

        if (bestEntity != null)
        {
            this.cache.put(mention, bestEntity);
        }

        return bestEntity;
    }

    @Override
    public void close() throws Exception {}
}
