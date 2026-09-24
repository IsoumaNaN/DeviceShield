cmake_minimum_required(VERSION 3.22.1)

project("deviceshield_native")

add_library(
    deviceshield_native
    SHARED
    src/main/cpp/native-lib.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    deviceshield_native
    ${log-lib}
    ${CMAKE_DL_LIBS}
)
