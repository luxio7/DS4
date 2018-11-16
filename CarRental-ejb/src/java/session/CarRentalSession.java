package session;

import java.util.ArrayList;
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
    public void createQuote(String client,ReservationConstraints constraint) throws Exception{
    boolean go = false;
    Exception reservationexception = null;

    if (!getAllRentalCompanies().isEmpty()){
            go = true;
    }

    for (String s: getAllRentalCompanies()){
          try{
              CarRentalCompany crc = em.find(CarRentalCompany.class, s);
              Quote quote = crc.createQuote(constraint, client);
              quotes.add(quote);
              go = false;
          }
          catch (Exception ex){
              reservationexception = ex;
          }

    }
    if (go){
        throw reservationexception;
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
        
        List<CarType> cartype = em.createNamedQuery("getCheapestCarType")
                .setParameter("startdate", start)
                .setParameter("enddate",end)
                .getResultList();
        
        
        List<CarRentalCompany> crcList = new ArrayList();
        for(String crc : getAllRentalCompanies()){
            CarRentalCompany crc1 = em.find(CarRentalCompany.class, crc);
            if(crc1.getRegions().contains(region)){
                crcList.add(crc1);
            }
                              
        }
        List<CarType> ctregion = new ArrayList();
        for(CarRentalCompany crc : crcList){
            for (CarType ctreg : cartype){
                if (crc.getAllTypes().contains(ctreg)){
                    ctregion.add(ctreg);
                }

            }
        }       
        Double minprice = ctregion.get(0).getRentalPricePerDay();
        String cct = "";
        for(CarType ct : ctregion){
            if (ct.getRentalPricePerDay() <= minprice){
                minprice = ct.getRentalPricePerDay();
                cct = ct.toString();
            }
        }
        
        return cct;
    }
}