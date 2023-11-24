package dk.aau.dkwe.load;

import dk.aau.dkwe.candidate.Index;

public interface Indexer<K, V>
{
    Index<K, V> getIndex();
    boolean constructIndex();
}
