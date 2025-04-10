package anonymous.linking;

public interface EntityLinker<S, T>
{
    T link(S source);
}
