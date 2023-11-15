package dk.aau.dkwe.linking;

import dk.aau.dkwe.candidate.IndexBuilder;
import dk.aau.dkwe.candidate.LuceneIndex;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class MentionLinker implements EntityLinker<String, String>
{
    private final LuceneIndex lucene;
    private Map<String, Double> weights;

    protected MentionLinker(File indexDirectory, Map<String, Double> weights, int candidatesSize) throws IOException
    {
        this.lucene = IndexBuilder.luceneBuilder(indexDirectory, candidatesSize);
        this.weights = weights;

        if (!this.weights.containsKey(LuceneIndex.LABEL_FIELD) ||
                !this.weights.containsKey(LuceneIndex.DESCRIPTION_FIELD) ||
                !this.weights.containsKey(LuceneIndex.SUB_DESCRIPTION_FIELD))
        {
            throw new IllegalArgumentException("Weights are missing for one or more fields");
        }
    }

    @Override
    public String link(String mention)
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
}
