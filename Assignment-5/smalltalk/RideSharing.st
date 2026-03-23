"Ride Sharing System — GNU Smalltalk
 Run: gst -f RideSharing.st
 Demonstrates encapsulation, inheritance, polymorphism.
"

Object subclass: Ride [
    | rideID pickupLocation dropoffLocation distance |

    Ride class >> id: id pickup: p dropoff: d miles: m [
        ^ (self new)
            setId: id pickup: p dropoff: d miles: m;
            yourself
    ]

    setId: id pickup: p dropoff: d miles: m [
        rideID := id.
        pickupLocation := p.
        dropoffLocation := d.
        distance := m asFloat.
    ]

    fare [
        self subclassResponsibility
    ]

    rideDetails [
        ^ 'Ride ID: ', rideID, (String with: Character nl),
          '  From: ', pickupLocation, ' -> To: ', dropoffLocation, (String with: Character nl),
          '  Distance: ', (distance printString), ' mi', (String with: Character nl),
          '  Fare: $', ((self fare) printString), (String with: Character nl).
    ]

    getRideID [ ^ rideID ]
    getDistance [ ^ distance ]
].

Ride subclass: StandardRide [
    fare [ ^ distance * 1.50 ]
].

Ride subclass: PremiumRide [
    fare [ ^ (distance * 2.75) + 5.00 ]

    rideDetails [
        ^ (super rideDetails),
          '  (Premium: $5.00 booking fee included)', (String with: Character nl).
    ]
].

Object subclass: Driver [
    | driverID name rating assignedRides |

    Driver class >> id: id name: n rating: r [
        ^ (self new)
            setDriverId: id name: n rating: r;
            yourself
    ]

    setDriverId: id name: n rating: r [
        driverID := id.
        name := n.
        rating := r asFloat.
        assignedRides := OrderedCollection new.
    ]

    addRide: aRide [
        aRide ifNotNil: [ assignedRides add: aRide ].
    ]

    getDriverInfo [
        | total |
        total := 0.0.
        assignedRides do: [ :each | total := total + (each fare) ].
        ^ 'Driver ID: ', driverID, (String with: Character nl),
          'Name: ', name, (String with: Character nl),
          'Rating: ', (rating printString), ' / 5.0', (String with: Character nl),
          'Completed rides: ', (assignedRides size printString), (String with: Character nl),
          'Total fares (sum of fare): $', (total printString), (String with: Character nl).
    ]
].

Object subclass: Rider [
    | riderID name requestedRides |

    Rider class >> id: id name: n [
        ^ (self new)
            setRiderId: id name: n;
            yourself
    ]

    setRiderId: id name: n [
        riderID := id.
        name := n.
        requestedRides := OrderedCollection new.
    ]

    requestRide: aRide [
        aRide ifNotNil: [ requestedRides add: aRide ].
    ]

    viewRides [
        Transcript show: 'Rider ID: ', riderID, ' | Name: ', name; nl.
        Transcript show: 'Requested rides (', (requestedRides size printString), '):'; nl.
        1 to: (requestedRides size) do: [ :i |
            Transcript show: '  [', (i printString), ']'; nl.
            Transcript show: ((requestedRides at: i) rideDetails); nl.
        ].
    ]
].

Object subclass: RideSharingDemo [
    RideSharingDemo class >> run [
        | pool driver rider |
        Transcript clear.
        Transcript show: '========== Ride Sharing System (GNU Smalltalk) =========='; nl; nl.

        pool := OrderedCollection new.
        pool add: (StandardRide id: 'R-1001' pickup: 'Downtown' dropoff: 'Airport' miles: 12.5).
        pool add: (PremiumRide id: 'R-1002' pickup: 'Hotel' dropoff: 'Convention Center' miles: 3.2).
        pool add: (StandardRide id: 'R-1003' pickup: 'Mall' dropoff: 'Suburb' miles: 8.0).
        pool add: (PremiumRide id: 'R-1004' pickup: 'Station' dropoff: 'University' miles: 5.5).

        Transcript show: '--- Polymorphic fare() / rideDetails() ---'; nl.
        pool do: [ :r |
            Transcript show: (r rideDetails); nl.
        ].

        driver := Driver id: 'D-42' name: 'Alex Morgan' rating: 4.8.
        1 to: ((pool size) min: 3) do: [ :i |
            driver addRide: (pool at: i).
        ].

        Transcript show: '--- Driver (encapsulated assigned rides) ---'; nl.
        Transcript show: (driver getDriverInfo); nl.

        rider := Rider id: 'U-900' name: 'Jamie Lee'.
        rider requestRide: (pool at: 1).
        rider requestRide: (pool at: 2).

        Transcript show: '--- Rider requested rides ---'; nl.
        rider viewRides.
    ]
].

RideSharingDemo run!
