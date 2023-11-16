package dk.aau.dkwe.linking;

public abstract class MentionLinker implements EntityLinker<String, String>, AutoCloseable
{
    @Override
    public String link(String mention)
    {
        return performLink(mention);
    }

    protected abstract String performLink(String mention);
}
