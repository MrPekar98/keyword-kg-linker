package dk.aau.dkwe.candidate;

public interface Index<K, V>
{
    V lookup(K key, String field);
    V lookup(K key);
    boolean add(K key, V value);
    boolean remove(K key);
    int size();
}
