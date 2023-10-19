package dk.aau.dkwe.disambiguation;

public record Result<E>(E element, double score)
{
    @Override
    public String toString()
    {
        return this.element + " - " + this.score;
    }
}
