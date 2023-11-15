package dk.aau.dkwe;

import java.util.Map;
import java.util.Set;

public record Config(int candidates, Set<String> predicates, Map<String, Double> weights)
{
}
