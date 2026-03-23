CXX = g++
CXXFLAGS = -std=c++17 -Wall -Wextra -Iinclude
SRC = src/main.cpp src/Ride.cpp src/StandardRide.cpp src/PremiumRide.cpp src/Driver.cpp src/Rider.cpp
TARGET = ride_sharing.exe

$(TARGET): $(SRC)
	$(CXX) $(CXXFLAGS) -o $(TARGET) $(SRC)

clean:
	rm -f $(TARGET)

.PHONY: clean
