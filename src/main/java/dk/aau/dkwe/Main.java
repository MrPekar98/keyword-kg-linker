package dk.aau.dkwe;

import dk.aau.dkwe.connector.Neo4J;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.File;

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

        String txt1 = "Hello, World!", txt2 = "Hello, Friend!";
        int distance = LevenshteinDistance.getDefaultInstance().apply(txt1, txt2);
        System.out.printf("Levenshtein distance is %d", distance);

        try (Neo4J db = new Neo4J())
        {
            System.out.println("Entity labels:\n");

            for (var label : db.labels("rdfs__label"))
            {
                System.out.println(label.getKey() + ": " + label.getValue());
            }
        }

        catch (Exception e)
        {
            System.err.println("Neo4J exception: " + e.getMessage());
        }
    }
}