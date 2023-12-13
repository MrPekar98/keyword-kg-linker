package dk.aau.dkwe.candidate;

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

/**
 * Writes Lucene index to disk
 */
public final class LuceneIndexBuilder
{
    public static void build(Set<dk.aau.dkwe.candidate.Document> documents, File directory) throws IOException
    {
        if (directory.listFiles() != null && directory.listFiles().length > 0)
        {
            throw new IllegalArgumentException("Directory '" + directory.getAbsolutePath() + "' already exists");
        }

        try (Analyzer analyzer = new StandardAnalyzer(); Directory dir = FSDirectory.open(directory.toPath()))
        {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(dir, config);
            load(documents, writer);
            writer.close();
        }
    }

    private static void load(Set<dk.aau.dkwe.candidate.Document> documents, IndexWriter writer)
    {
        documents.forEach(doc -> {
            try
            {
                StringBuilder description = new StringBuilder(), subDescription = new StringBuilder();
                Document luceneDoc = new Document();
                luceneDoc.add(new Field(LuceneIndex.URI_FIELD, doc.uri(), TextField.TYPE_STORED));
                luceneDoc.add(new Field(LuceneIndex.LABEL_FIELD, doc.label(), TextField.TYPE_STORED));

                doc.description().forEach(desc -> description.append(' ').append(desc));
                doc.subDescription().forEach(subDesc -> subDescription.append(' ').append(subDesc));
                luceneDoc.add(new Field(LuceneIndex.DESCRIPTION_FIELD, description.toString(), TextField.TYPE_STORED));
                luceneDoc.add(new Field(LuceneIndex.SUB_DESCRIPTION_FIELD, subDescription.toString(), TextField.TYPE_STORED));

                writer.addDocument(luceneDoc);
            }

            catch (IOException ignored) {}
        });
    }
}
