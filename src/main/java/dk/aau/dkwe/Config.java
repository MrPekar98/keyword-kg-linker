package dk.aau.dkwe;

import java.util.Map;

public record Config(int candidates, String domain, Map<String, Double> weights, boolean logging)
{
}
