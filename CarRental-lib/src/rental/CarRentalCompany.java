package rental;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;


@Entity
@NamedQueries({
    @NamedQuery(name="getAllRentalCompaniesName", 
            query="SELECT company.name "
                    + "FROM CarRentalCompany company"),
    
    @NamedQuery(name="getAllRentalCompaniesObject", 
            query="SELECT company "
            + "FROM CarRentalCompany company"),
    
    @NamedQuery(name = "allCarIdsOfType",
        query= "SELECT car.id FROM Car car, CarRentalCompany company "
        + "WHERE company.name = :companyName "
        + "AND car MEMBER OF company.cars "
        + "AND car.type.name = :carTypeName"),

    @NamedQuery(name="getAllCarTypesByCarRentalCompanyName", 
            query="SELECT DISTINCT carType "
            + "FROM CarRentalCompany company, CarType carType "
            + "WHERE company.name = :givenName "
            + "AND carType MEMBER OF company.carTypes"),
    
    @NamedQuery(name = "allReservationsForClient",
            query= "SELECT reservation FROM Reservation reservation, Car car "
            + "WHERE reservation.carRenter = :clientName "
            + "AND reservation MEMBER OF car.reservations"),
    
    @NamedQuery(name = "allReservationsForCarTypeanId",
            query= "SELECT reservation FROM Reservation reservation, Car car "
            + "WHERE reservation.rentalCompany = :companyName "
            + "AND car.type.name = :carTypeName "
            + "AND reservation MEMBER OF car.reservations"),
    
    @NamedQuery(name= "getBestClient",
            query="SELECT reservation.carRenter, COUNT(reservation.id) AS tot"
                    + " FROM Reservation reservation"
                    + " GROUP BY reservation.carRenter"
                    + " ORDER BY tot DESC "),
        
     @NamedQuery(name="mostPopularCarType",
            query="SELECT reservation.carType, COUNT(reservation.carType) AS tot "
            + " FROM Reservation reservation "
            + " WHERE reservation.rentalCompany = :companyName "
            + " AND FUNC('YEAR', reservation.startDate) = :year "
            + " GROUP BY reservation.carType "
            + " ORDER BY tot DESC"),    
        
    @NamedQuery(name="getCheapestCarType",
            query="SELECT cartype "
            + "FROM CarType cartype, Car car, Reservation res "
            + "WHERE car.type = cartype "
            + "AND res.carId = car.id "
            + "AND res.startDate BETWEEN :startdate AND :enddate " 
            + "GROUP BY cartype")
    })

//
public class CarRentalCompany implements Serializable {

    private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());
    
    @Id
    private String name;
    
    @OneToMany(cascade= CascadeType.ALL)
    private List<Car> cars;
    
    @ManyToMany(cascade= CascadeType.ALL)
    private Set<CarType> carTypes = new HashSet<CarType>();
    
    private List<String> regions;

	
    /***************
     * CONSTRUCTOR *
     ***************/
    public CarRentalCompany(){}
    public CarRentalCompany(String name, List<String> regions, List<Car> cars) {
        logger.log(Level.INFO, "<{0}> Starting up CRC {0} ...", name);
        setName(name);
        this.cars = cars;
        setRegions(regions);
        for (Car car : cars) {
            carTypes.add(car.getType());
        }
    }

    /********
     * NAME *
     ********/
    
    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    /***********
     * Regions *
     **********/
    private void setRegions(List<String> regions) {
        this.regions = regions;
    }
    
    public List<String> getRegions() {
        return this.regions;
    }

    /*************
     * CAR TYPES *
     *************/
   
    public Collection<CarType> getAllTypes() {
        return carTypes;
    }
    public void setCarType(Set<CarType> cartype){
        this.carTypes = cartype;
    }
    public CarType getType(String carTypeName) {
        for(CarType type:carTypes){
            if(type.getName().equals(carTypeName))
                return type;
        }
        throw new IllegalArgumentException("<" + carTypeName + "> No cartype of name " + carTypeName);
    }

    public boolean isAvailable(String carTypeName, Date start, Date end) {
        logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
        return getAvailableCarTypes(start, end).contains(getType(carTypeName));
    }

    public Set<CarType> getAvailableCarTypes(Date start, Date end) {
        Set<CarType> availableCarTypes = new HashSet<CarType>();
        for (Car car : cars) {
            if (car.isAvailable(start, end)) {
                availableCarTypes.add(car.getType());
            }
        }
        return availableCarTypes;
    }

    /*********
     * CARS *
     *********/
    
    public List<Car> getCars(){
        return cars;
    }
    public void setCars(List<Car> cars) {
        this.cars = cars;
        initializeCarTypes();
    }
     private void initializeCarTypes() {
        for (Car car : cars) {
            carTypes.add(car.getType());
        }
    }

    public void addCar(Car car) {
        cars.add(car);
        carTypes.add(car.getType());
    }
    public Car getCar(int uid) {
        for (Car car : cars) {
            if (car.getId() == uid) {
                return car;
            }
        }
        throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
    }

    public Set<Car> getCars(CarType type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (car.getType().equals(type)) {
                out.add(car);
            }
        }
        return out;
    }
    
     public Set<Car> getCars(String type) {
        Set<Car> out = new HashSet<Car>();
        for (Car car : cars) {
            if (type.equals(car.getType().getName())) {
                out.add(car);
            }
        }
        return out;
    }

    private List<Car> getAvailableCars(String carType, Date start, Date end) {
        List<Car> availableCars = new LinkedList<Car>();
        for (Car car : cars) {
            if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
                availableCars.add(car);
            }
        }
        return availableCars;
    }

    /****************
     * RESERVATIONS *
     ****************/
    
    public Quote createQuote(ReservationConstraints constraints, String guest)
            throws ReservationException {
        logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
                new Object[]{name, guest, constraints.toString()});

        System.out.println("komt in create ");
        if (!this.regions.contains(constraints.getRegion()) || !isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
            throw new ReservationException("<" + name
                    + "> No cars available to satisfy the given constraints.");
        }
		
        CarType type = getType(constraints.getCarType());

        double price = calculateRentalPrice(type.getRentalPricePerDay(), constraints.getStartDate(), constraints.getEndDate());

        return new Quote(guest, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
    }

    // Implementation can be subject to different pricing strategies
    private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
        return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
                / (1000 * 60 * 60 * 24D));
    }

    public Reservation confirmQuote(Quote quote) throws ReservationException {
        logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
        List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
        if (availableCars.isEmpty()) {
            throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
                    + " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
        }
        Car car = availableCars.get((int) (Math.random() * availableCars.size()));

        Reservation res = new Reservation(quote, car.getId());
        car.addReservation(res);
        return res;
    }

    public void cancelReservation(Reservation res) {
        logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
        getCar(res.getCarId()).removeReservation(res);
    }
    
    public Set<Reservation> getReservationsBy(String renter) {
        logger.log(Level.INFO, "<{0}> Retrieving reservations by {1}", new Object[]{name, renter});
        Set<Reservation> out = new HashSet<Reservation>();
        for(Car c : cars) {
            for(Reservation r : c.getReservations()) {
                if(r.getCarRenter().equals(renter))
                    out.add(r);
            }
        }
        return out;
    }
}