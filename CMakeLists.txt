cmake_minimum_required(VERSION 3.26)
project(keyword-kg-linker)

set(CMAKE_CXX_COMPILER "g++")
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_COMPILER_ID "GNU")
add_compile_options(-Wall -Wextra -pedantic)

include_directories(include/)
file(GLOB sources
            src/connector/*.cpp)
add_library(linkerlib STATIC ${sources})
#target_link_libraries(linkerlib)

add_executable(keyword-kg-linker src/main.cpp)
target_link_libraries(keyword-kg-linker linkerlib)