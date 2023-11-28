package dk.aau.dkwe.utils;

import java.util.List;

public class MathUtils
{
    public static double cosine(List<Double> vector1, List<Double> vector2)
    {
        if (vector1.size() != vector2.size())
        {
            throw new IllegalArgumentException("Vectors are not of the same dimension");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        int dimension = vector1.size();

        for (int i = 0; i < dimension; i++)
        {
            double val1 = vector1.get(i), val2 = vector2.get(i);
            dotProduct += val1 * val2;
            normA += Math.pow(val1, 2);
            normB += Math.pow(val2, 2);
        }

        if (normA == 0 || normB == 0)
        {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
