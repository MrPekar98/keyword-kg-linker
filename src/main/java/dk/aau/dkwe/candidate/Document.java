package dk.aau.dkwe.candidate;

import java.util.Set;

/**
 * Represents a single entity to be indexed in Lucene
 * @param uri Entity URI
 * @param label Label of entity
 * @param description Concatenated string literals of entity, such as alias, surname, fullname, etc.
 * @param subDescription Label and description concatenated for direct KG neighbors of the entity
 */
public record Document(String uri, String label, Set<String> description, Set<String> subDescription)
{
}
