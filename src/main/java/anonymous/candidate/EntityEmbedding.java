package anonymous.candidate;

import anonymous.utils.MathUtils;

import java.util.List;

public record EntityEmbedding(String uri, List<Double> embedding)
{
    public Double cosine(EntityEmbedding other)
    {
        return MathUtils.cosine(this.embedding, other.embedding());
    }
}
