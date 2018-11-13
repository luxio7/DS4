package session;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
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
        List lijst = em.createNamedQuery("getAllRentalCompaniesName").getResultList();
        Set set = new HashSet(lijst);
        return set;
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
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
        //find the carrentalcompany class by its ID, in this case its name that is stored in the string 'company'
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
    
    @Resource
    private SessionContext context;


    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();
        String nameCompany;
        CarRentalCompany crc;
        try {
            for (Quote quote : quotes) {
                nameCompany = quote.getRentalCompany();
                crc = em.find(CarRentalCompany.class,nameCompany);
                done.add(crc.confirmQuote(quote));
            }
        } catch (Exception e) {
            context.setRollbackOnly();
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
   
  @Override
    public String getCheapestCarType(Date start, Date end, String region){
        List<String> cartype = em.createNamedQuery("getCheapestCarType")
                .setParameter("startdate", start)
                .setParameter("enddate",end)
                .setParameter("region",region)
                .getResultList();
        
        return cartype.get(0);
    }
}