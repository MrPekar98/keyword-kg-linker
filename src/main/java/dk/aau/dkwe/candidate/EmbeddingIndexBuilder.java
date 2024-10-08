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
        write(directory, index, FILE_NAME);
    }

    public static void write(File directory, EmbeddingIndex index, int partition)
    {
        write(directory, index, partition + "-" + FILE_NAME);
    }

    public static void write(File directory, EmbeddingIndex index, String fileName)
    {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(directory.getAbsolutePath() + "/" + fileName)))
        {
            outputStream.writeObject(index);
            outputStream.flush();
        }

        catch (NegativeArraySizeException e)
        {
            throw new RuntimeException("Negative array size for when serializing embeddings index: " + e.getMessage());
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

    private static EmbeddingIndex read(File directory, int partition)
    {
        return read(new File(directory.getAbsolutePath() + "/" + partition + "-" + FILE_NAME));
    }
}
