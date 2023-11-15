package dk.aau.dkwe.candidate;

public interface Index<K, V>
{
    V lookup(K key, String field);
}
