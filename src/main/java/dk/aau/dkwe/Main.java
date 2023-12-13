package dk.aau.dkwe;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dk.aau.dkwe.connector.Neo4J;
import dk.aau.dkwe.linking.CSVEntityLinker;
import dk.aau.dkwe.linking.EmbeddingLinker;
import dk.aau.dkwe.linking.KeywordLinker;
import dk.aau.dkwe.linking.MentionLinker;
import dk.aau.dkwe.load.EmbeddingIndexer;
import dk.aau.dkwe.load.LuceneIndexer;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
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

        for (ArgParser.Parameter param : parameters)
        {
            if (param == ArgParser.Parameter.CONFIG)
            {
                configFile = new File(param.getValue());
            }

            else
            {
                directory = new File(param.getValue());
            }
        }

        Config config = readConfigFile(configFile);
        Neo4J neo4J = new Neo4J();
        Set<String> entities = neo4J.entities();
        LuceneIndexer luceneIndexer = LuceneIndexer.create(entities, config, directory, true);
        EmbeddingIndexer embeddingIndexer = EmbeddingIndexer.create(entities, directory, true);
        Thread embeddingIndexingThread = new Thread(embeddingIndexer::constructIndex);
        embeddingIndexingThread.start();

        if (!luceneIndexer.constructIndex())
        {
            System.err.println("Failed constructing indexes");
            embeddingIndexingThread.interrupt();
            neo4J.close();
            return;
        }

        embeddingIndexingThread.join();
        neo4J.close();

        Duration duration = Duration.between(start, Instant.now());
        System.out.println("Indexing done in " + duration.toString().substring(2));
    }

    private static void link(Set<ArgParser.Parameter> parameters)
    {
        File resultDir = null, tableFile = null, indexDir = null, configFile = null;
        String linkerType = null;
        System.out.println("Linking...");

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
                case "embedding" -> new EmbeddingLinker(indexDir);
                default -> throw new IllegalArgumentException("Argument for '" + linkerType + "' was not recognized");
            };
            CSVEntityLinker csvLinker = new CSVEntityLinker(linker);
            Instant start = Instant.now();
            csvLinker.linkTable(tableFile, resultDir);
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