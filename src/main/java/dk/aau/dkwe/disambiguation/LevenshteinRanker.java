package dk.aau.dkwe.disambiguation;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Computes a ranked list of strings with the shortest Levenshtein distance to a given input string
 */
public class LevenshteinRanker implements Ranker<String>
{
    public static LevenshteinRanker levenshteinRank()
    {
        return new LevenshteinRanker();
    }

    @Override
    public List<Result<String>> rank(String base, Collection<String> corpus)
    {
        List<Result<String>> results = new ArrayList<>(corpus.size());
        corpus.forEach(e -> {
            int distance = LevenshteinDistance.getDefaultInstance().apply(base, e);
            results.add(new Result<>(e, distance));
        });
        results.sort(Comparator.comparingDouble(Result::score));

        return results;
    }

    @Override
    public List<Result<String>> rank(String base, Collection<String> corpus, int k)
    {
        return rank(base, corpus).subList(0, k);
    }
}
