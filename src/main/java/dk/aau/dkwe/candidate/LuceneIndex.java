package dk.aau.dkwe.candidate;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

public class LuceneIndex implements Index<String, Set<String>>
{
    public static final String URI_FIELD = "URI";
    public static final String LABEL_FIELD = "LABEL";
    public static final String DESCRIPTION_FIELD = "DESCIPTION";
    public static final String SUB_DESCRIPTION_FIELD = "SUB-DESCRIPTION";
    private final IndexSearcher searcher;
    private final Map<String, QueryParser> parsers = new HashMap<>();
    private final int resultSize;

    LuceneIndex(IndexSearcher searcher, int resultSize)
    {
        this.searcher = searcher;
        this.resultSize = resultSize;
        this.parsers.put(URI_FIELD, new QueryParser(URI_FIELD, new StandardAnalyzer()));
        this.parsers.put(LABEL_FIELD, new QueryParser(LABEL_FIELD, new StandardAnalyzer()));
        this.parsers.put(DESCRIPTION_FIELD, new QueryParser(DESCRIPTION_FIELD, new StandardAnalyzer()));
        this.parsers.put(SUB_DESCRIPTION_FIELD, new QueryParser(SUB_DESCRIPTION_FIELD, new StandardAnalyzer()));
    }

    @Override
    public Set<String> lookup(String key, String field)
    {
        try
        {
            if (!this.parsers.containsKey(field))
            {
                throw new IllegalArgumentException("Field '" + field + "' does not exist");
            }

            Query query = this.parsers.get(field).parse(key);
            ScoreDoc[] hits = this.searcher.search(query, this.resultSize).scoreDocs;
            Set<String> results = new HashSet<>(hits.length);

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
