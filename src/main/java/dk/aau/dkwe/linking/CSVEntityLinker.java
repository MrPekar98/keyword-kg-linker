package dk.aau.dkwe.linking;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Links all non-numeric cells in the given CSV table and writes the output on disk
 */
public class CSVEntityLinker
{
    private MentionLinker linker;

    public CSVEntityLinker(MentionLinker linker)
    {
        this.linker = linker;
    }

    public void linkTable(File tableFile, File outputDirectory) throws IOException
    {
        try (CSVReader reader = new CSVReader(new FileReader(tableFile)))
        {
            String[] line;
            List<List<String>> output = new ArrayList<>();

            while ((line = reader.readNext()) != null)
            {
                List<String> outputLine = new ArrayList<>(line.length);

                for (String cell : line)
                {
                    if (StringUtils.isNumeric(cell))
                    {
                        outputLine.add(null);
                    }

                    else
                    {
                        String entity = this.linker.link(cell);
                        outputLine.add(entity);
                    }
                }

                output.add(outputLine);
            }

            File outputFile = new File(outputDirectory.getAbsolutePath() + "/results.csv");
            flushLinks(output, outputFile);
        }
    }

    private static void flushLinks(List<List<String>> links, File outputFile) throws IOException
    {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile)))
        {
            for (List<String> outputLine : links)
            {
                String[] line = new String[outputLine.size()];
                int column = 0;

                for (String output : outputLine)
                {
                    line[column++] = output != null ? output : "";
                }

                writer.writeNext(line);
            }
        }
    }
}
