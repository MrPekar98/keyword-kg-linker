package dk.aau.dkwe.candidate;

import java.io.*;

/**
 * Serializes embeddings index to disk or deserializes it from disk
 */
public class EmbeddingIndexBuilder
{
    private static final String FILE_NAME = "embedding.idx";

    public static void write(File directory, EmbeddingIndex index)
    {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(directory.getAbsolutePath() + "/" + FILE_NAME)))
        {
            outputStream.writeObject(index);
            outputStream.flush();
        }

        catch (IOException e)
        {
            throw new RuntimeException("IOException: " + e.getMessage());
        }
    }

    public static EmbeddingIndex read(File directory)
    {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(directory.getAbsolutePath() + "/" + FILE_NAME)))
        {
            return (EmbeddingIndex) inputStream.readObject();
        }

        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("ClassNotFoundException: " + e.getMessage());
        }

        catch (IOException e)
        {
            throw new RuntimeException("IOException: " + e.getMessage());
        }
    }
}
