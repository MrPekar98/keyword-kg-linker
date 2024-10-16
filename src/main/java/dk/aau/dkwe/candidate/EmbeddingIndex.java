package dk.aau.dkwe.candidate;

import com.pgvector.PGvector;

import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Simple hash index of KG embeddings
 */
public class EmbeddingIndex implements Index<String, List<Double>>, Closeable
{
    private Connection conn;
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";
    private static final String URL;
    private static final String TABLE = "embeddings";

    static
    {
        String ip = System.getenv("DB");
        URL = "jdbc:postgresql://" + ip + ":5432/postgres";
    }

    public EmbeddingIndex()
    {
        try
        {
            this.conn = DriverManager.getConnection(URL, USER, PASSWORD);

            if (this.conn == null)
            {
                throw new RuntimeException("Could not connect to PostgreSQL database");
            }

            PGvector.addVectorType(this.conn);
        }

        catch (SQLException e)
        {
            throw new RuntimeException(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public List<Double> lookup(String key, String ignore)
    {
        return lookup(key);
    }

    @Override
    public List<Double> lookup(String key)
    {
        String statement = "SELECT embedding FROM " + TABLE + " WHERE uri = ?";

        try (PreparedStatement stmt = this.conn.prepareStatement(statement))
        {
            stmt.setString(1, key);

            ResultSet rs = stmt.executeQuery();

            if (rs.next())
            {
                PGvector vector = (PGvector) rs.getObject(1);
                float[] primitiveEmbedding = vector.toArray();
                List<Double> embedding = new ArrayList<>(primitiveEmbedding.length);

                for (float f : primitiveEmbedding)
                {
                    embedding.add((double) f);
                }

                return embedding;
            }

            return Collections.emptyList();
        }

        catch (SQLException e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<EntityEmbedding> neighbors(List<Double> embeddingKey, int numNeighbors)
    {
        StringBuilder builder = new StringBuilder("SELECT uri, embedding FROM " + TABLE + " ORDER BY embedding <=> '[");
        embeddingKey.forEach(val -> builder.append(val).append(","));
        builder.deleteCharAt(builder.length() - 1).append("]' LIMIT ").append(numNeighbors);

        try (PreparedStatement stmt = this.conn.prepareStatement(builder.toString()))
        {
            ResultSet rs = stmt.executeQuery();
            List<EntityEmbedding> neighbors = new ArrayList<>(numNeighbors);

            while (rs.next())
            {
                String uri = rs.getString(1);
                PGvector vector = (PGvector) rs.getObject(2);
                neighbors.add(new EntityEmbedding(uri, fromPrimitive(vector.toArray())));
            }

            return neighbors;
        }

        catch (SQLException e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public boolean add(String key, List<Double> embedding)
    {
        PGvector vector = new PGvector(toPrimitive(embedding));
        String statement = "INSERT INTO " + TABLE + " (uri, embedding) VALUES (?, ?)";

        try (PreparedStatement stmt = this.conn.prepareStatement(statement))
        {
            stmt.setObject(1, key);
            stmt.setObject(2, vector);
            stmt.executeUpdate();

            return true;
        }

        catch (SQLException e)
        {
            e.printStackTrace();

            return false;
        }
    }

    @Override
    public boolean remove(String key)
    {
        String statement = "DELETE FROM " + TABLE + " WHERE uri = ?";

        try (PreparedStatement stmt = this.conn.prepareStatement(statement))
        {
            stmt.setString(1, key);
            stmt.executeUpdate();
            return true;
        }

        catch (SQLException e)
        {
            e.printStackTrace();

            return false;
        }
    }

    @Override
    public int size()
    {
        String statement = "SELECT COUNT(*) FROM " + TABLE;

        try (PreparedStatement stmt = this.conn.prepareStatement(statement))
        {
            ResultSet rs = stmt.executeQuery();
            return rs.getInt(1);
        }

        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public Iterator<String> keys()
    {
        List<String> keys = new ArrayList<>();
        String statement = "SELECT uri FROM " + TABLE;

        try (PreparedStatement stmt = this.conn.prepareStatement(statement))
        {
            ResultSet rs = stmt.executeQuery();

            while (rs.next())
            {
                keys.add(rs.getString(1));
            }

            return keys.iterator();
        }

        catch (SQLException e)
        {
            e.printStackTrace();
            return Collections.emptyIterator();
        }
    }

    private static float[] toPrimitive(List<Double> vector)
    {
        int size = vector.size();
        float[] result = new float[size];


        for (int i = 0; i < size; i++)
        {
            result[i] = vector.get(i).floatValue();
        }

        return result;
    }

    private static List<Double> fromPrimitive(float[] primitiveEmbedding)
    {
        List<Double> embedding = new ArrayList<>(primitiveEmbedding.length);

        for (float val : primitiveEmbedding)
        {
            embedding.add((double) val);
        }

        return embedding;
    }

    @Override
    public void close()
    {
        try
        {
            this.conn.close();
        }

        catch (SQLException ignored) {}
    }
}
