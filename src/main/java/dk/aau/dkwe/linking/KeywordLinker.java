package dk.aau.dkwe.linking;

import dk.aau.dkwe.candidate.IndexBuilder;
import dk.aau.dkwe.candidate.LuceneIndex;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keyword-based entity linking of entity mentions
 */
public class KeywordLinker extends MentionLinker
{
    private final LuceneIndex lucene;
    private Map<String, Double> weights;

    public KeywordLinker(File indexDirectory, Map<String, Double> fieldWeights, int candidatesSize) throws IOException
    {
        this.lucene = IndexBuilder.luceneBuilder(indexDirectory, candidatesSize);
        this.weights = fieldWeights;

        if (!this.weights.containsKey(LuceneIndex.LABEL_FIELD) ||
                !this.weights.containsKey(LuceneIndex.DESCRIPTION_FIELD) ||
                !this.weights.containsKey(LuceneIndex.SUB_DESCRIPTION_FIELD))
        {
            throw new IllegalArgumentException("Weights are missing for one or more fields");
        }
    }

    /**
     * Keyword searches for KG entities using KG labels, string predicates, and direct neighbors using labels and string predicates
     * It then applies a weighted linear combination of the 3 searched fields and returns the entity with the highest linear combination score
     * @param mention Entity mention
     * @return KG entity with the highest weighted linear combination score
     */
    @Override
    protected String performLink(String mention)
    {
        String bestEntity = null;
        double bestEntityScore = -1;
        Map<String, Double> candidatesLabel = this.lucene.lookup(mention, LuceneIndex.LABEL_FIELD),
                candidatesDescription = this.lucene.lookup(mention, LuceneIndex.DESCRIPTION_FIELD),
                candidatesSubDescription = this.lucene.lookup(mention, LuceneIndex.SUB_DESCRIPTION_FIELD);
        Set<String> entities = new HashSet<>(candidatesLabel.keySet());
        entities.addAll(candidatesDescription.keySet());
        entities.addAll(candidatesSubDescription.keySet());

        for (String entity : entities)
        {
            double combination = this.weights.get(LuceneIndex.LABEL_FIELD) * candidatesLabel.getOrDefault(entity, 0.0)
                    + this.weights.get(LuceneIndex.DESCRIPTION_FIELD) * candidatesDescription.getOrDefault(entity, 0.0)
                    + this.weights.get(LuceneIndex.SUB_DESCRIPTION_FIELD) * candidatesSubDescription.getOrDefault(entity, 0.0);

            if (combination > bestEntityScore)
            {
                bestEntityScore = combination;
                bestEntity = entity;
            }
        }

        return bestEntity;
    }

    @Override
    public void close() throws Exception {}
}
