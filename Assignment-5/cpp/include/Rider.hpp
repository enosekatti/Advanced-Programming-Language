#ifndef RIDER_HPP
#define RIDER_HPP

#include "Ride.hpp"
#include <memory>
#include <string>
#include <vector>

/**
 * Encapsulation: requestedRides is private; requestRide() and viewRides()
 * are the public interface.
 */
class Rider {
private:
    std::string riderID;
    std::string name;
    std::vector<std::shared_ptr<Ride>> requestedRides;

public:
    Rider(std::string id, std::string riderName);

    void requestRide(std::shared_ptr<Ride> ride);
    void viewRides() const;

    size_t getRequestedRideCount() const;
};

#endif
