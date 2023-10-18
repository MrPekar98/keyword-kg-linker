package dk.aau.dkwe.candidate;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuceneIndex implements Index<String, List<String>>
{
    static final String URI_FIELD = "URI";
    static final String TEXT_FIELD = "text";
    private final IndexSearcher searcher;
    private final QueryParser parser = new QueryParser(TEXT_FIELD, new StandardAnalyzer());
    private final int resultSize;

    LuceneIndex(IndexSearcher searcher, int resultSize)
    {
        this.searcher = searcher;
        this.resultSize = resultSize;
    }

    @Override
    public List<String> lookup(String key)
    {
        try
        {
            Query query = this.parser.parse(key);
            ScoreDoc[] hits = this.searcher.search(query, this.resultSize).scoreDocs;
            List<String> results = new ArrayList<>(hits.length);

            for (ScoreDoc hit : hits)
            {
                results.add(this.searcher.doc(hit.doc).get(URI_FIELD));
            }

            return results;
        }

        catch (ParseException | IOException e)
        {
            return null;
        }
    }
}
