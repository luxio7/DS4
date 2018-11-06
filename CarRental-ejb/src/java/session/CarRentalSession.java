package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.RentalStore;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {
    
    @PersistenceContext
    EntityManager em;

    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @Override
    public Set<String> getAllRentalCompanies() {
        //return new HashSet<String>(RentalStore.getRentals().keySet());
        List lijst = em.createNamedQuery("getAllRentalCompaniesName").getResultList();
        Set set = new HashSet(lijst);
        return set;
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        /**
        List<CarType> availableCarTypes = new LinkedList<CarType>();
        for(String crc : getAllRentalCompanies()) {
            for(CarType ct : RentalStore.getRentals().get(crc).getAvailableCarTypes(start, end)) {
                if(!availableCarTypes.contains(ct))
                    availableCarTypes.add(ct);
            }
        }
        return availableCarTypes;
        */
        List<CarRentalCompany> lijst = em.createNamedQuery("getAllRentalCompaniesObject").getResultList();
        Set<CarType> AvailableCarTypes = new HashSet<CarType>();
        
        if (lijst == null) {
            lijst = new LinkedList<CarRentalCompany>();
        }
        
        for(CarRentalCompany crc : lijst) {
           for(CarType cartype : crc.getAvailableCarTypes(start,end)){
               AvailableCarTypes.add(cartype);
           }
        }
        return new LinkedList(AvailableCarTypes);
    }

    @Override
    public Quote createQuote(String company, ReservationConstraints constraints) throws ReservationException {
        CarRentalCompany crc = em.find(CarRentalCompany.class, company);
        try {
            Quote out = crc.createQuote(constraints, renter);
            quotes.add(out);
            return out;
        } catch(Exception e) {
            throw new ReservationException(e);
        }
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        try {
            for (Quote quote : quotes) {
                done.add(RentalStore.getRental(quote.getRentalCompany()).confirmQuote(quote));
            }
        } catch (Exception e) {
            for(Reservation r:done)
                RentalStore.getRental(r.getRentalCompany()).cancelReservation(r);
            throw new ReservationException(e);
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
}