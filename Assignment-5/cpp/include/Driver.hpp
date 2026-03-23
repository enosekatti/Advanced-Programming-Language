#ifndef DRIVER_HPP
#define DRIVER_HPP

#include "Ride.hpp"
#include <memory>
#include <string>
#include <vector>

/**
 * Encapsulation: assignedRides is private; only addRide() and summary accessors
 * expose controlled access to completed rides.
 */
class Driver {
private:
    std::string driverID;
    std::string name;
    double rating;
    std::vector<std::shared_ptr<Ride>> assignedRides;

public:
    Driver(std::string id, std::string driverName, double initialRating);

    void addRide(std::shared_ptr<Ride> ride);
    std::string getDriverInfo() const;

    size_t getCompletedRideCount() const;
    double getTotalEarningsFromRides() const;
};

#endif
