package dk.aau.dkwe.connector;

import org.neo4j.driver.*;

import java.util.*;
import java.util.logging.Level;

/**
 * Neo4J connector with methods for a variety of different queries
 */
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

    public String entityLabel(String entity)
    {
        try (Session session = this.driver.session())
        {
            return session.readTransaction(tx -> {
                Map<String, Object> params = new HashMap<>();
                params.put("entity", entity);

                Result r = tx.run("MATCH (n:Resource) WHERE n.uri IN [$entity] RETURN n.rdfs__label AS label", params);

                for (var result : r.list())
                {
                    return result.get("label").asString();
                }

                return null;
            });
        }
    }

    public Set<String> entityString(String entity, Set<String> neo4jPredicates)
    {
        try (Session session = this.driver.session())
        {
            return session.readTransaction(tx -> {
                Set<String> labels = new HashSet<>();
                Map<String, Object> params = new HashMap<>();
                params.put("entity", entity);

                neo4jPredicates.forEach(p -> {
                    Result r = tx.run("MATCH (n:Resource) WHERE n.uri IN [$entity] AND EXISTS(n." + p +
                            ") RETURN n." + p + " AS label", params);

                    for (var result : r.list())
                    {
                        String label = result.get("label").asString();
                        labels.add(label);
                    }
                });

                return labels;
            });
        }
    }

    public Set<String> entities()
    {
        try (Session session = this.driver.session())
        {
            return session.readTransaction(tx -> {
                Set<String> ents = new HashSet<>();
                Result r = tx.run("MATCH (n:Resource) RETURN n.uri AS uri");

                for (var result : r.list())
                {
                    String uri = result.get("uri").asString();
                    ents.add(uri);
                }

                return ents;
            });
        }
    }

    public Set<String> neighbors(String entity)
    {
        try (Session session = this.driver.session())
        {
            return session.readTransaction(tx -> {
                Set<String> entities = new HashSet<>();
                Map<String, Object> params = new HashMap<>();
                params.put("entity", entity);

                Result r = tx.run("MATCH (n1:Resource)-[r]->(n2:Resource) WHERE n1.uri IN [$entity] RETURN n2.uri AS uri", params);

                for (var result : r.list())
                {
                    String uri = result.get("uri").asString();
                    entities.add(uri);
                }

                return entities;
            });
        }
    }

    @Override
    public void close() throws Exception
    {
        this.driver.close();
    }
}
