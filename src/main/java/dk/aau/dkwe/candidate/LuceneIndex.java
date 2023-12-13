package dk.aau.dkwe.candidate;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

/**
 * Lucene index searching using 1 of 4 fields: URI, label, string literals of entity, and label and string literals of direct KG neighbors of the entity
 */
public class LuceneIndex implements Index<String, Map<String, Double>>
{
    public static final String URI_FIELD = "URI";
    public static final String LABEL_FIELD = "LABEL";
    public static final String DESCRIPTION_FIELD = "DESCRIPTION";
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

    /**
     * Perform keyword search using one of the specified fields
     * @param key Keyword query
     * @param field Field to search
     * @return Map of retrieved entities and their matching score
     */
    @Override
    public Map<String, Double> lookup(String key, String field)
    {
        try
        {
            if (!this.parsers.containsKey(field))
            {
                throw new IllegalArgumentException("Field '" + field + "' does not exist");
            }

            Query query = this.parsers.get(field).parse(key);
            ScoreDoc[] hits = this.searcher.search(query, this.resultSize).scoreDocs;
            Map<String, Double> results = new HashMap<>(hits.length);

            for (ScoreDoc hit : hits)
            {
                String uri = this.searcher.doc(hit.doc).get(URI_FIELD);
                double score = hit.score;
                results.put(uri, score);
            }

            return results;
        }

        catch (ParseException | IOException e)
        {
            return null;
        }
    }

    @Override
    public Map<String, Double> lookup(String key)
    {
        throw new IllegalArgumentException("LuceneIndex::lookup: Missing field argument");
    }

    @Override
    public boolean add(String key, Map<String, Double> value)
    {
        throw new UnsupportedOperationException("LuceneIndex::add is unsupported");
    }

    @Override
    public boolean remove(String key)
    {
        throw new UnsupportedOperationException("LuceneIndex::remove is unsupported");
    }

    @Override
    public int size()
    {
        throw new UnsupportedOperationException("LuceneIndex::size us unsupported");
    }
}
