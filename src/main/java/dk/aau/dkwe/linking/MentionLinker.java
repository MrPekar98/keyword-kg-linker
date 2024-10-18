package dk.aau.dkwe.linking;

import java.io.Closeable;

public abstract class MentionLinker implements EntityLinker<String, String>, Closeable
{
    @Override
    public String link(String mention)
    {
        return performLink(mention);
    }

    protected abstract String performLink(String mention);
}
