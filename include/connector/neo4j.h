#ifndef KEYWORD_KG_LINKER_NEO4J_H
#define KEYWORD_KG_LINKER_NEO4J_H

#include <neo4j-client.h>
#include <string>
#include <vector>
#include <utility>

class Neo4J
{
public:
    Neo4J() = delete;
    Neo4J(const Neo4J& other) = default;
    Neo4J(Neo4J&& other) = default;
    Neo4J(const std::string& host);
    Neo4J(std::string&& host);
    virtual ~Neo4J();

    Neo4J& operator=(const Neo4J& other) = default;
    Neo4J& operator=(Neo4J&& other) = default;

    std::vector<std::pair<std::string, std::string>> labels(const std::string& labelPredicate) const;

private:
    neo4j_connection_t* connection;
};

#endif
