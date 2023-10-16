package dk.aau.dkwe;

import org.apache.commons.text.similarity.LevenshteinDistance;

// TODO: Check if the directory exists
public class Main
{
    public static void main(String[] args)
    {
        ArgParser parser = ArgParser.parse(args);

        if (!parser.isParsed())
        {
            System.err.println("Could not parse parameters: " + parser.parseError());
        }

        String txt1 = "Hello, World!", txt2 = "Hello, Friend!";
        int distance = LevenshteinDistance.getDefaultInstance().apply(txt1, txt2);
        System.out.printf("Levenshtein distance is %d", distance);
    }
}