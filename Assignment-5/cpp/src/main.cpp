#include "Ride.hpp"
#include "StandardRide.hpp"
#include "PremiumRide.hpp"
#include "Driver.hpp"
#include "Rider.hpp"
#include <iostream>
#include <memory>
#include <vector>

int main() {
    std::cout << "========== Ride Sharing System (C++) ==========\n\n";

    // Polymorphism: heterogeneous collection of Ride pointers
    std::vector<std::shared_ptr<Ride>> ridePool;
    ridePool.push_back(std::make_shared<StandardRide>("R-1001", "Downtown", "Airport", 12.5));
    ridePool.push_back(std::make_shared<PremiumRide>("R-1002", "Hotel", "Convention Center", 3.2));
    ridePool.push_back(std::make_shared<StandardRide>("R-1003", "Mall", "Suburb", 8.0));
    ridePool.push_back(std::make_shared<PremiumRide>("R-1004", "Station", "University", 5.5));

    std::cout << "--- Polymorphic fare() and rideDetails() ---\n";
    for (const auto& ride : ridePool) {
        std::cout << ride->rideDetails() << "\n";
    }

    Driver driver("D-42", "Alex Morgan", 4.8);
    for (size_t i = 0; i < ridePool.size() && i < 3; ++i) {
        driver.addRide(ridePool[i]);
    }

    std::cout << "--- Driver (encapsulated assignedRides) ---\n";
    std::cout << driver.getDriverInfo() << "\n";

    Rider rider("U-900", "Jamie Lee");
    rider.requestRide(ridePool[0]);
    rider.requestRide(ridePool[1]);

    std::cout << "--- Rider requested rides ---\n";
    rider.viewRides();

    return 0;
}
