package dk.aau.dkwe.utils;

public class MathUtils
{
    public static double cosine(float[] vector1, float[] vector2)
    {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vector1.length; i++)
        {
            dotProduct += vector1[i] * vector2[i];
            normA += Math.pow(vector1[i], 2);
            normB += Math.pow(vector2[i], 2);
        }

        if (normA == 0 || normB == 0)
        {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
