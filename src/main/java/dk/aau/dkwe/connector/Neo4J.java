package dk.aau.dkwe.connector;

import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.driver.*;

import java.util.*;
import java.util.logging.Level;

public final class Neo4J implements AutoCloseable
{
    private Driver driver;

    public Neo4J()
    {
        String dbUri = "bolt://neo4j:7687",
                dbUser = "neo4j",
                dbPassword = "admin";
        this.driver = GraphDatabase.driver(dbUri, AuthTokens.basic(dbUser, dbPassword),
                Config.builder().withLogging(Logging.javaUtilLogging(Level.WARNING)).build());

        if (!isConnected())
        {
            throw new RuntimeException("Could not establish connection to Neo4J");
        }
    }

    private boolean isConnected()
    {
        try (Session session = this.driver.session())
        {
            session.readTransaction(tx -> {
                Result r = tx.run("MATCH (n:Resource) RETURN COUNT(n) AS count");
                return r.single().get("count").asLong();
            });
            return true;
        }

        catch (Exception e)
        {
            return false;
        }
    }

    public Set<Pair<String, String>> labels(String neo4jPredicate)
    {
        try (Session session = this.driver.session())
        {
            return session.readTransaction(tx -> {
                Set<Pair<String, String>> labels = new HashSet<>();
                Result r = tx.run("MATCH (n:Resource) WHERE EXISTS(n." + neo4jPredicate +
                        ") RETURN n.uri AS uri, n." + neo4jPredicate + " AS label");

                for (var result : r.list())
                {
                    String uri = result.get("uri").asString(), label = result.get("label").asString();
                    labels.add(Pair.of(uri, label));
                }

                return labels;
            });
        }
    }

    @Override
    public void close() throws Exception
    {
        this.driver.close();
    }
}
