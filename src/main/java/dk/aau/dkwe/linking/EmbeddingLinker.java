package dk.aau.dkwe.linking;

import com.robrua.nlp.bert.Bert;

import java.util.Set;

public class EmbeddingLinker extends MentionLinker
{
    private final Set<String> entities;
    private final Bert bert;
    private boolean isClosed = false;
    private static final String MODEL_PATH = "com/robrua/nlp/easy-bert/bert-uncased-L-12-H-768-A-12";

    public EmbeddingLinker(Set<String> entities)
    {
        this.entities = entities;
        this.bert = Bert.load(MODEL_PATH);
    }

    @Override
    protected String performLink(String mention)
    {
        if (this.isClosed)
        {
            throw new IllegalStateException("Class has been closed");
        }

        float[] mentionEmbedding = this.bert.embedSequence(mention);
        double highestScore = -1.0;
        String bestEntity = null;

        for (String entity : this.entities)
        {
            String[] split = entity.split("/");
            String entityText = split[split.length - 1].replace('_', ' ');
            float[] entityEmbedding = this.bert.embedSequence(entityText);
            double score = cosine(mentionEmbedding, entityEmbedding);

            if (score > highestScore)
            {
                highestScore = score;
                bestEntity = entity;
            }
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
        this.bert.close();
        this.isClosed = true;
    }
}
