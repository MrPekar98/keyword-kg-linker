package dk.aau.dkwe.candidate;

/**
 * Represents a single entity to be indexed in Lucene
 * @param uri Entity URI
 * @param label Label of entity
 */
public record Document(String uri, String label, String description)
{
}
