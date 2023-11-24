package dk.aau.dkwe.load;

import dk.aau.dkwe.Config;
import dk.aau.dkwe.candidate.Document;
import dk.aau.dkwe.candidate.Index;
import dk.aau.dkwe.candidate.IndexBuilder;
import dk.aau.dkwe.connector.Neo4J;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LuceneIndexer implements Indexer<String, Map<String, Double>>
{
    private final Config config;
    private final File directory;
    private final boolean parallelized;
    private static final int THREADS = 4;

    public static LuceneIndexer create(Config config, File indexDirectory, boolean parallelize)
    {
        return new LuceneIndexer(config, indexDirectory, parallelize);
    }

    private LuceneIndexer(Config config, File indexDirectory, boolean parallelize)
    {
        this.config = config;
        this.directory = indexDirectory;
        this.parallelized = parallelize;
    }

    @Override
    public Index<String, Map<String, Double>> getIndex()
    {
        try
        {
            return IndexBuilder.luceneBuilder(this.directory, this.config.candidates());
        }

        catch (IOException e)
        {
            return null;
        }
    }

    @Override
    public boolean constructIndex()
    {
        try (Neo4J neo4J = new Neo4J())
        {
            Set<String> predicates = config.predicates();
            Set<String> entities = neo4J.entities();
            Set<Document> documents;

            if (this.parallelized)
            {
                documents = parallelCreateDocuments(entities, neo4J, predicates);
            }

            else
            {
                documents = createDocuments(entities, neo4J, predicates);
            }

            IndexBuilder.luceneBuilder(documents, this.directory);

            return true;
        }

        catch (Exception e)
        {
            return false;
        }
    }

    private Set<Document> parallelCreateDocuments(Set<String> entities, Neo4J neo4J, Set<String> predicates)
    {
        List<String> entityList = new ArrayList<>(entities);
        ExecutorService threadPool = Executors.newFixedThreadPool(THREADS);
        List<Future<Set<Document>>> tasks = new ArrayList<>();
        final int splitSize = 10000;
        int entityCount = entities.size(), iterations = entityCount / splitSize;

        for (int i = 0; i < iterations; i++)
        {
            List<String> subset = entityList.subList(i, Math.min(i + splitSize, entityCount));
            Future<Set<Document>> future = threadPool.submit(() -> createDocuments(new HashSet<>(subset), neo4J, predicates));
            tasks.add(future);
        }

        Set<Document> documents = new HashSet<>();
        tasks.forEach(f -> {
            try
            {
                documents.addAll(f.get());
            }

            catch (InterruptedException | ExecutionException ignored) {}
        });

        return documents;
    }

    private static Set<Document> createDocuments(Set<String> entities, Neo4J neo4J, Set<String> predicates)
    {
        Set<Document> documents = new HashSet<>(entities.size());
        int progress = 0, entityCount = entities.size();

        for (String entity : entities)
        {
            String label = neo4J.entityLabel(entity);
            Set<String> description = neo4J.entityString(entity, predicates),
                    neighbors = neo4J.neighbors(entity), neighborDescription = new HashSet<>();
            predicates.add("rdfs__label");  // Including label for neighbor entities only
            neighbors.forEach(n -> neighborDescription.addAll(neo4J.entityString(n, predicates)));

            if (label == null)
            {
                String[] entitySplit = entity.split("/");
                label = entitySplit[entitySplit.length - 1].replace('_', ' ');
            }

            documents.add(new Document(entity, label, description, neighborDescription));

            if (progress++ % 100 == 0)
            {
                System.out.print("\t\t\t\t\t\t\t\t\r");
                System.out.print("Progress: " + (((double) progress / entityCount) * 100) + "%\r");
            }
        }

        return documents;
    }
}
