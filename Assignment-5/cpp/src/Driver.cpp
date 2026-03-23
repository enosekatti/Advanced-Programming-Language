#include "Driver.hpp"
#include <sstream>
#include <iomanip>

Driver::Driver(std::string id, std::string driverName, double initialRating)
    : driverID(std::move(id))
    , name(std::move(driverName))
    , rating(initialRating) {}

void Driver::addRide(std::shared_ptr<Ride> ride) {
    if (ride) {
        assignedRides.push_back(std::move(ride));
    }
}

size_t Driver::getCompletedRideCount() const {
    return assignedRides.size();
}

double Driver::getTotalEarningsFromRides() const {
    double total = 0.0;
    for (const auto& r : assignedRides) {
        if (r) total += r->fare();
    }
    return total;
}

std::string Driver::getDriverInfo() const {
    std::ostringstream oss;
    oss << "Driver ID: " << driverID << "\n"
        << "Name: " << name << "\n"
        << "Rating: " << std::fixed << std::setprecision(1) << rating << " / 5.0\n"
        << "Completed rides: " << assignedRides.size() << "\n"
        << "Total fares (sum of ride.fare()): $" << std::fixed << std::setprecision(2)
        << getTotalEarningsFromRides() << "\n";
    return oss.str();
}
