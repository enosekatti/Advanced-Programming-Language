#ifndef RIDE_HPP
#define RIDE_HPP

#include <string>

/**
 * Base class Ride — inheritance: StandardRide and PremiumRide extend this type.
 * Polymorphism: fare() and rideDetails() are overridden in subclasses.
 */
class Ride {
protected:
    std::string rideID;
    std::string pickupLocation;
    std::string dropoffLocation;
    double distance;

public:
    Ride(std::string id, std::string pickup, std::string dropoff, double miles);
    virtual ~Ride() = default;

    /** Polymorphic: each ride type computes fare differently. */
    virtual double fare() const = 0;

    /** Polymorphic display of ride information. */
    virtual std::string rideDetails() const;

    std::string getRideID() const;
    std::string getPickupLocation() const;
    std::string getDropoffLocation() const;
    double getDistance() const;
};

#endif
