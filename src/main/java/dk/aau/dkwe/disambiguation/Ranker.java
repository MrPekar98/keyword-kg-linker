package dk.aau.dkwe.disambiguation;

import java.util.Collection;
import java.util.List;

public interface Ranker<E>
{
    List<Result<E>> rank(String base, Collection<E> corpus);
    List<Result<E>> rank(String base, Collection<E> corpus, int k);
}
