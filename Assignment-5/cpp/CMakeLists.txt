cmake_minimum_required(VERSION 3.14)
project(RideSharingSystem CXX)
set(CMAKE_CXX_STANDARD 17)

include_directories(include)

add_executable(ride_sharing
    src/main.cpp
    src/Ride.cpp
    src/StandardRide.cpp
    src/PremiumRide.cpp
    src/Driver.cpp
    src/Rider.cpp
)
