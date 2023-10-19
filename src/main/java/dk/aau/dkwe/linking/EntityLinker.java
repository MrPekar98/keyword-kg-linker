package dk.aau.dkwe.linking;

public interface EntityLinker<S, T>
{
    T link(S source);
}
