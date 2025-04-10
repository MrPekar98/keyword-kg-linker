package anonymous.load;

import anonymous.candidate.Index;

public interface Indexer<K, V>
{
    Index<K, V> getIndex();
    boolean constructIndex();
}
