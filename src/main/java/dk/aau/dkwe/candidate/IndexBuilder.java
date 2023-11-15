package dk.aau.dkwe.candidate;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public final class IndexBuilder
{
    public static LuceneIndex luceneBuilder(Set<Document> documents, File directory) throws IOException
    {
        return LuceneIndexBuilder.build(documents, directory);
    }

    public static LuceneIndex luceneBuilder(File directory, int resultSize) throws IOException
    {
        Directory dir = FSDirectory.open(directory.toPath());
        DirectoryReader dirReader = DirectoryReader.open(dir);
        return new LuceneIndex(new IndexSearcher(dirReader), resultSize);
    }
}
