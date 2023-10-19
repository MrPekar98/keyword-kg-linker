package dk.aau.dkwe.linking;

import dk.aau.dkwe.candidate.IndexBuilder;
import dk.aau.dkwe.candidate.LuceneIndex;
import dk.aau.dkwe.disambiguation.LevenshteinRanker;
import dk.aau.dkwe.disambiguation.Result;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public abstract class TableEntityLinker implements EntityLinker<String, String>
{
    private final LuceneIndex lucene;

    protected TableEntityLinker(File indexDirectory, int candidatesSize) throws IOException
    {
        this.lucene = IndexBuilder.luceneBuilder(indexDirectory, candidatesSize);

    }

    @Override
    public String link(String mention)
    {
        Set<String> candidates = this.lucene.lookup(mention);
        List<Result<String>> disambiguate = LevenshteinRanker.levenshteinRank().rank(mention, candidates);
        return !disambiguate.isEmpty() ? disambiguate.get(0).element() : null;
    }
}
