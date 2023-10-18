package dk.aau.dkwe.candidate;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public final class LuceneIndexBuilder
{
    public static LuceneIndex build(Set<Pair<String, String>> entries, File directory) throws IOException
    {
        if (directory.exists())
        {
            throw new IllegalArgumentException("Directory '" + directory.getAbsolutePath() + "' already exists");
        }

        try (Analyzer analyzer = new StandardAnalyzer(); Directory dir = FSDirectory.open(directory.toPath()))
        {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(dir, config);
            load(entries, writer);
            writer.close();

            return IndexBuilder.luceneBuilder(directory, 50);
        }
    }

    private static void load(Set<Pair<String, String>> entries, IndexWriter writer)
    {
        entries.forEach(pair -> {
            try
            {
                Document doc = new Document();
                doc.add(new Field(LuceneIndex.URI_FIELD, pair.getKey(), TextField.TYPE_STORED));
                doc.add(new Field(LuceneIndex.TEXT_FIELD, pair.getValue(), TextField.TYPE_STORED));
                writer.addDocument(doc);
            }

            catch (IOException ignored) {}
        });
    }
}
