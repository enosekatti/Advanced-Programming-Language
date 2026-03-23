#include "PremiumRide.hpp"
#include <sstream>
#include <iomanip>

PremiumRide::PremiumRide(std::string id, std::string pickup, std::string dropoff, double miles)
    : Ride(std::move(id), std::move(pickup), std::move(dropoff), miles) {}

double PremiumRide::fare() const {
    return distance * RATE_PER_MILE + BOOKING_FEE;
}

std::string PremiumRide::rideDetails() const {
    std::ostringstream oss;
    oss << Ride::rideDetails()
        << "  (Premium: $" << std::fixed << std::setprecision(2) << BOOKING_FEE << " booking fee included)\n";
    return oss.str();
}
