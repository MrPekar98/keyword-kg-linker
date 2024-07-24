package dk.aau.dkwe.load;

import dk.aau.dkwe.candidate.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataReader
{
    private final File kgDir;
    private final String domain;

    public DataReader(File kgDirectory, String domain)
    {
        this.kgDir = kgDirectory;
        this.domain = domain;
    }

    public DataReader(File kgDirectory)
    {
        this(kgDirectory, null);
    }

    public Set<Document> read(boolean log)
    {
        int fileCount = 0;
        File[] kgFiles = this.kgDir.listFiles();
        Set<Document> documents = new HashSet<>();
        Map<String, Map<String, Set<String>>> entities = new HashMap<>();
        String labelPredicate = "http://www.w3.org/2000/01/rdf-schema#label",
                descriptionPredicate = "http://schema.org/description";
        Set<String> predicates = Set.of(labelPredicate, descriptionPredicate);

        for (File kgFile : kgFiles)
        {
            if (log)
            {
                System.out.print("                                                                                 \r");
                System.out.print("Reading " + ++fileCount + "/" + kgFile.length() + " (" + kgFile.getName() + ")\r");
            }

            try (BufferedReader reader = Files.newBufferedReader(kgFile.toPath(), StandardCharsets.ISO_8859_1))
            {
                String line;

                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("#"))
                    {
                        continue;
                    }

                    String[] split = line.split(" ");
                    String entityUri = split[0].replace("<", "").replace(">", "");
                    String predicate = split[1].replace("<", "").replace(">", "");

                    if (entityUri.contains("Category:") || entityUri.contains("/prop") ||
                            (this.domain != null && !entityUri.contains(this.domain)) ||
                            !predicates.contains(predicate))
                    {
                        continue;
                    }

                    else if (!entities.containsKey(entityUri))
                    {
                        entities.put(entityUri, new HashMap<>());
                        predicates.forEach(pred -> entities.get(entityUri).put(pred, new HashSet<>()));
                    }

                    int valueStart = line.lastIndexOf('>') + 2;
                    String value = line.substring(valueStart, line.length() - 2);

                    if (value.contains("@en"))
                    {
                        value = value.substring(0, value.indexOf("@")).replace("\"", "");
                    }

                    entities.get(entityUri).get(predicate).add(value);
                }
            }

            catch (IOException ignored) {}
        }

        entities.keySet().forEach(entity -> {
            StringBuilder labelBuilder = new StringBuilder(), descriptionBuilder = new StringBuilder();
            Set<String> labels = entities.get(entity).get(labelPredicate),
                    descriptions = entities.get(entity).get(descriptionPredicate);
            labels.forEach(label -> labelBuilder.append(label).append(" "));
            descriptions.forEach(description -> descriptionBuilder.append(description).append(" "));

            Document document = new Document(entity, labelBuilder.toString(), descriptionBuilder.toString());
            documents.add(document);
        });

        return documents;
    }
}
