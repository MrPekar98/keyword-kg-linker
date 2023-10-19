package dk.aau.dkwe.disambiguation;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        results.sort((r1, r2) -> Double.compare(r2.score(), r1.score()));

        return results;
    }

    @Override
    public List<Result<String>> rank(String base, Collection<String> corpus, int k)
    {
        return rank(base, corpus).subList(0, k);
    }
}
