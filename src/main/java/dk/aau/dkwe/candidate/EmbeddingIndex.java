package dk.aau.dkwe.candidate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple hash index of KG embeddings
 */
public class EmbeddingIndex implements Index<String, List<Double>>, Serializable
{
    private final Map<String, List<Double>> embeddings = new HashMap<>();

    @Override
    public List<Double> lookup(String key, String ignore)
    {
        return lookup(key);
    }

    @Override
    public List<Double> lookup(String key)
    {
        return this.embeddings.getOrDefault(key, null);
    }

    @Override
    public boolean add(String key, List<Double> embedding)
    {
        if (!this.embeddings.containsKey(key))
        {
            this.embeddings.put(key, embedding);
            return true;
        }

        return false;
    }

    @Override
    public boolean remove(String key)
    {
        if (this.embeddings.containsKey(key))
        {
            this.embeddings.remove(key);
            return true;
        }

        return false;
    }

    @Override
    public int size()
    {
        return this.embeddings.size();
    }

    public Iterator<String> keys()
    {
        return this.embeddings.keySet().iterator();
    }
}
