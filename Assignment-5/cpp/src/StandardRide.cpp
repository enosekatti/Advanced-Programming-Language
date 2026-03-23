#include "StandardRide.hpp"

StandardRide::StandardRide(std::string id, std::string pickup, std::string dropoff, double miles)
    : Ride(std::move(id), std::move(pickup), std::move(dropoff), miles) {}

double StandardRide::fare() const {
    return distance * RATE_PER_MILE;
}
