#include <iostream>
//#include <xapian.h>
#include <connector/neo4j.h>

/**
 * Usage:
 *   Link:
 *      ... -table <CSV TABLE> -output <OUTPUT DIRECTORY>
 *   Index:
 *      ... -index <DIR>
 */
int main(int argc, char** argv)
{
    Neo4J neo4j("neo4j://neo4j:admin@172.17.0.2:7687");
    auto labels = neo4j.labels("rdfs__label");
    std::cout << "Number of labels: " << labels.size() << "\n\n";

    for (auto pair : labels)
    {
        std::cout << "URI: " << pair.first << "\n" << "Label: " << pair.second << std::endl;
    }

    return 0;
}
