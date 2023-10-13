#include <connector/neo4j.h>
#include <utility>
#include <stdexcept>

Neo4J::Neo4J(const std::string& host)
    : Neo4J(std::move(const_cast<std::string&>(host)))
{}

Neo4J::Neo4J(std::string&& host)
    : host(std::move(host))
{
    neo4j_client_init();
    this->connection = neo4j_connect(this->host.c_str(), NULL, NEO4J_INSECURE);
}

Neo4J::~Neo4J()
{
    neo4j_close(this->connection);
    neo4j_client_cleanup();
}

std::vector<std::pair<std::string, std::string>> Neo4J::labels(const std::string& labelPredicate) const
{
    char query[256];
    const char* predicate = labelPredicate.c_str();
    sprintf(query, "MATCH (n:Resource) WHERE EXISTS(n.%s) RETURN n.uri, n.%s", predicate, predicate);
    neo4j_result_stream_t* results = neo4j_run(this->connection, query, neo4j_null);
    std::vector<std::pair<std::string, std::string>> labels;
    neo4j_result_t* result;

    if (results == NULL)
    {
        throw std::runtime_error(std::string("Failed executing Cypher query: ") + std::string(neo4j_error_message(results)));
    }

    while ((result = neo4j_fetch_next(results)) != NULL)
    {
        char uri[256], label[256];
        neo4j_value_t uri_value = neo4j_result_field(result, 0),
            label_value = neo4j_result_field(result, 1);
        neo4j_tostring(uri_value, uri, sizeof(uri));
        neo4j_tostring(label_value, label, sizeof(label));
        labels.emplace_back(std::string(uri), std::string(label));
    }

    neo4j_close_results(results);
    return labels;
}