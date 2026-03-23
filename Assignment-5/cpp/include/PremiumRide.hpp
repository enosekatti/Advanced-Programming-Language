#ifndef PREMIUM_RIDE_HPP
#define PREMIUM_RIDE_HPP

#include "Ride.hpp"

/** Premium ride: higher per-mile rate + flat booking fee. */
class PremiumRide : public Ride {
private:
    static constexpr double RATE_PER_MILE = 2.75;
    static constexpr double BOOKING_FEE = 5.00;

public:
    PremiumRide(std::string id, std::string pickup, std::string dropoff, double miles);
    double fare() const override;
    std::string rideDetails() const override;
};

#endif
