#include "Rider.hpp"
#include <iostream>

Rider::Rider(std::string id, std::string riderName)
    : riderID(std::move(id))
    , name(std::move(riderName)) {}

void Rider::requestRide(std::shared_ptr<Ride> ride) {
    if (ride) {
        requestedRides.push_back(std::move(ride));
    }
}

size_t Rider::getRequestedRideCount() const {
    return requestedRides.size();
}

void Rider::viewRides() const {
    std::cout << "Rider ID: " << riderID << " | Name: " << name << "\n";
    std::cout << "Requested rides (" << requestedRides.size() << "):\n";
    for (size_t i = 0; i < requestedRides.size(); ++i) {
        const auto& r = requestedRides[i];
        if (!r) continue;
        std::cout << "  [" << (i + 1) << "]\n" << r->rideDetails();
    }
}
