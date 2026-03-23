#include "Ride.hpp"
#include <sstream>
#include <iomanip>

Ride::Ride(std::string id, std::string pickup, std::string dropoff, double miles)
    : rideID(std::move(id))
    , pickupLocation(std::move(pickup))
    , dropoffLocation(std::move(dropoff))
    , distance(miles) {}

std::string Ride::rideDetails() const {
    std::ostringstream oss;
    oss << "Ride ID: " << rideID << "\n"
        << "  From: " << pickupLocation << " -> To: " << dropoffLocation << "\n"
        << "  Distance: " << std::fixed << std::setprecision(2) << distance << " mi\n"
        << "  Fare: $" << std::fixed << std::setprecision(2) << fare() << "\n";
    return oss.str();
}

std::string Ride::getRideID() const { return rideID; }
std::string Ride::getPickupLocation() const { return pickupLocation; }
std::string Ride::getDropoffLocation() const { return dropoffLocation; }
double Ride::getDistance() const { return distance; }
