package dk.aau.dkwe;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.aau.dkwe.candidate.Document;
import dk.aau.dkwe.candidate.IndexBuilder;
import dk.aau.dkwe.connector.Neo4J;
import dk.aau.dkwe.linking.CSVEntityLinker;
import dk.aau.dkwe.linking.EmbeddingLinker;
import dk.aau.dkwe.linking.KeywordLinker;
import dk.aau.dkwe.linking.MentionLinker;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
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

    private static void index(Set<ArgParser.Parameter> parameters) throws Exception {
        Instant start = Instant.now();
        File directory = null, configFile = null;
        System.out.println("Indexing...");

        for (ArgParser.Parameter param : parameters) {
            if (param == ArgParser.Parameter.CONFIG) {
                configFile = new File(param.getValue());
            } else {
                directory = new File(param.getValue());
            }
        }

        Config config = readConfigFile(configFile);
        Set<String> predicates = config.predicates();
        Neo4J neo4J = new Neo4J();
        Set<String> entities = neo4J.entities();
        Set<Document> documents = createDocuments(entities, neo4J, predicates);
        IndexBuilder.luceneBuilder(documents, directory);
        neo4J.close();

        Duration duration = Duration.between(start, Instant.now());
        System.out.println("Indexing done in " + duration.toString().substring(2));
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

    private static void link(Set<ArgParser.Parameter> parameters)
    {
        Neo4J neo4J = new Neo4J();
        Set<String> entities = neo4J.entities();
        System.out.println("Linking...");

        File resultDir = null, tableFile = null, indexDir = null, configFile = null;
        String linkerType = null;

        for (ArgParser.Parameter param : parameters)
        {
            switch (param)
            {
                case TABLE -> tableFile = new File(param.getValue());
                case DIRECTORY -> indexDir = new File(param.getValue());
                case OUTPUT -> resultDir = new File(param.getValue());
                case CONFIG -> configFile = new File(param.getValue());
                case TYPE -> linkerType = param.getValue();
            }
        }

        try
        {
            Config config = readConfigFile(configFile);
            MentionLinker linker = switch (linkerType) {
                case "keyword" -> new KeywordLinker(indexDir, config.weights(), config.candidates());
                case "embedding" -> new EmbeddingLinker(entities);
                default -> throw new IllegalArgumentException("Argument for '" + linkerType + "' was not recognized");
            };
            CSVEntityLinker csvLinker = new CSVEntityLinker(linker);
            Instant start = Instant.now();
            csvLinker.linkTable(tableFile, resultDir);
            neo4J.close();
            linker.close();

            Duration duration = Duration.between(start, Instant.now());
            System.out.println("Linking done in " + duration.toString().substring(2));
        }

        catch (Exception e)
        {
            System.err.println("Exception: " + e.getMessage());
        }
    }

    private static Config readConfigFile(File configFile) throws FileNotFoundException
    {
        Gson gson = new Gson();
        TypeToken<Config> type = new TypeToken<>(){};
        return gson.fromJson(new FileReader(configFile), type);
    }
}