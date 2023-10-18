package dk.aau.dkwe;

import dk.aau.dkwe.candidate.IndexBuilder;
import dk.aau.dkwe.connector.Neo4J;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Set;

// TODO: Add argument to linking command to specify size of candidate set
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

        /*String txt1 = "Hello, World!", txt2 = "Hello, Friend!";
        int distance = LevenshteinDistance.getDefaultInstance().apply(txt1, txt2);
        System.out.printf("Levenshtein distance is %d\n\n", distance);*/
    }

    private static void index(Set<ArgParser.Parameter> parameters) throws Exception
    {
        Instant start = Instant.now();
        System.out.println("Indexing...");

        String predicate = null;
        File directory = null;

        for (ArgParser.Parameter param : parameters)
        {
            if (param == ArgParser.Parameter.PREDICATE)
            {
                predicate = param.getValue();
            }

            else
            {
                directory = new File(param.getValue());
            }
        }

        Neo4J neo4J = new Neo4J();
        var labels = neo4J.labels(predicate);
        IndexBuilder.luceneBuilder(labels, directory);
        neo4J.close();

        Duration duration = Duration.between(start, Instant.now());
        System.out.println("Indexing done in " + duration.toString());
    }

    private static void link(Set<ArgParser.Parameter> parameters)
    {

    }
}