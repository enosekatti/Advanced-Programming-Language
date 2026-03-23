#ifndef STANDARD_RIDE_HPP
#define STANDARD_RIDE_HPP

#include "Ride.hpp"

/** Standard ride: lower per-mile rate (inheritance + polymorphic fare). */
class StandardRide : public Ride {
private:
    static constexpr double RATE_PER_MILE = 1.50;

public:
    StandardRide(std::string id, std::string pickup, std::string dropoff, double miles);
    double fare() const override;
};

#endif
