package dk.aau.dkwe.candidate;

import java.util.Set;

public record Document(String uri, String label, Set<String> description, Set<String> subDescription)
{
}
