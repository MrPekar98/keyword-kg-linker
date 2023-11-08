package dk.aau.dkwe;

import dk.aau.dkwe.candidate.IndexBuilder;
import dk.aau.dkwe.connector.Neo4J;
import dk.aau.dkwe.linking.CSVEntityLinker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err.println("Missing parameters");
            System.exit(1);
        }

        ArgParser parser = ArgParser.parse(args);

        if (!parser.isParsed())
        {
            System.err.println("Could not parse parameters: " + parser.parseError());
            System.exit(1);
        }

        for (ArgParser.Parameter p : parser.getParameters())
        {
            if (p == ArgParser.Parameter.TABLE || p == ArgParser.Parameter.DIRECTORY)
            {
                File f = new File(p.getValue());

                if (!f.exists())
                {
                    System.err.println("Path '" + p.getValue() + "' does not exist for parameter '" + p + "'");
                    System.exit(1);
                }
            }
        }

        try
        {
            switch (parser.getCommand())
            {
                case INDEX -> index(parser.getParameters());
                case LINK -> link(parser.getParameters());
                default -> System.out.println("Did not recognize command '" + parser.getCommand() + "'");
            }
        }

        catch (IOException e)
        {
            System.err.println("IOException: " + e.getMessage());
        }

        catch (RuntimeException e)
        {
            System.err.println("RuntimeException: " + e.getMessage());
        }

        catch (Exception e)
        {
            System.err.println("Exception: " + e.getMessage());
        }
    }

    private static void index(Set<ArgParser.Parameter> parameters) throws Exception
    {
        Instant start = Instant.now();
        File directory = null, predicateFile = null;
        System.out.println("Indexing...");

        for (ArgParser.Parameter param : parameters)
        {
            if (param == ArgParser.Parameter.PREDICATE_FILE)
            {
                predicateFile = new File(param.getValue());
            }

            else
            {
                directory = new File(param.getValue());
            }
        }

        Set<String> predicates = predicates(predicateFile);
        Neo4J neo4J = new Neo4J();
        Set<String> entities = neo4J.entities();
        var documents = createDocuments(entities, neo4J, predicates);
        IndexBuilder.luceneBuilder(documents, directory);
        neo4J.close();

        Duration duration = Duration.between(start, Instant.now());
        System.out.println("Indexing done in " + duration.toString().substring(2));
    }

    private static Set<String> predicates(File predicateFile)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(predicateFile)))
        {
            Set<String> predicates = new HashSet<>();
            String line;

            while ((line = reader.readLine()) != null)
            {
                predicates.add(line);
            }

            return predicates;
        }

        catch (IOException e)
        {
            return new HashSet<>();
        }
    }

    private static Map<String, Set<String>> createDocuments(Set<String> entities, Neo4J neo4J, Set<String> predicates)
    {
        Map<String, Set<String>> documents = new HashMap<>(entities.size());

        for (String entity : entities)
        {
            Set<String> labels = neo4J.entityString(entity, predicates),
                    neighbors = neo4J.neighbors(entity);
            documents.put(entity, labels);
            neighbors.forEach(n -> documents.get(entity).addAll(neo4J.entityString(n, predicates)));
        }

        return documents;
    }

    private static void link(Set<ArgParser.Parameter> parameters)
    {
        Instant start = Instant.now();
        System.out.println("Linking...");

        File resultDir = null, tableFile = null, indexDir = null;
        int candidates = -1;

        for (ArgParser.Parameter param : parameters)
        {
            switch (param)
            {
                case TABLE -> tableFile = new File(param.getValue());
                case DIRECTORY -> indexDir = new File(param.getValue());
                case OUTPUT -> resultDir = new File(param.getValue());
                case CANDIDATES -> candidates = Integer.parseInt(param.getValue());
            }
        }

        try
        {
            CSVEntityLinker linker = new CSVEntityLinker(candidates, indexDir);
            linker.linkTable(tableFile, resultDir);
        }

        catch (IOException e)
        {
            System.err.println("IOException: " + e.getMessage());
        }

        finally
        {
            Duration duration = Duration.between(start, Instant.now());
            System.out.println("Linking done in " + duration.toString().substring(2));
        }
    }
}