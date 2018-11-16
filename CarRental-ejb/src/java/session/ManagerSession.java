package session;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.REQUIRED;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    EntityManager em;
    
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("d/M/y");
    
    public List<CarRentalCompany> getAllCarRentalCompanies(){
        List<CarRentalCompany> allCrc = em.createNamedQuery("getAllRentalCompaniesObject").getResultList();
        return allCrc;
    }
    
    @Override
    @TransactionAttribute(REQUIRED)
    public void addCRC(CarRentalCompany crc){
        List<Car> cars = crc.getCars();
        crc.setCars(new ArrayList<Car>());
        em.persist(crc);
        for (Car car : cars) {
            addCar(car, crc);
        }
        
       
    }    
    //
    @Override
    public void addCarToCompany(Car car, String crc){
        CarRentalCompany crc1 = em.find(CarRentalCompany.class, crc);
        addCar(car, crc1);
    }
    
    @Override
    public void addCar(Car car, CarRentalCompany crc){
        CarType carType = em.find(CarType.class, car.getType().getName());
        if (carType != null) {
            car.setType(carType);
        }
        crc.addCar(car);
    }
    
  
    @Override
    public Set<CarType> getCarTypes(String company) {

            return new HashSet(em.createNamedQuery("getAllCarTypesByCarRentalCompanyName")
                    .setParameter("givenName",company)
                    .getResultList());
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
            return new HashSet(em.createNamedQuery("allCarIdsOfType")
                       .setParameter("companyName", company)
                       .setParameter("carTypeName", type)
                       .getResultList());
    }

    @Override
    public int getNumberOfReservationsBy(String clientName) {
            List<Reservation> nbres = em.createNamedQuery("allReservationsForClient")
                .setParameter("clientName", clientName)
                .getResultList();
            return nbres.size();
    }

    @Override
    public int getNumberOfReservationsForCarType(String company, String type) {

            List<Reservation> nbres = em.createNamedQuery("allReservationsForCarTypeanId")
                .setParameter("companyName", company)
                .setParameter("carTypeName", type)
                .getResultList();
            return nbres.size();
    }
    
    @Override
    public Set<String> getBestClients(){
        Set<String> bestclients = new HashSet();
        List<Object[]> clients = em.createNamedQuery("getBestClient")
                .getResultList();
        Long mostres = (Long) clients.get(0)[1];
        for(Object[] obj : clients){
            if (obj[1] == mostres){
                bestclients.add((String) obj[0]);
            }
        }
        return bestclients;
    }
    
    @Override
    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year) throws Exception{
        List<Object[]> cartype = em.createNamedQuery("mostPopularCarType")
                .setParameter("companyName",carRentalCompanyName)
                .setParameter("year",year)
                .getResultList();
        CarType cartypefound = em.find(CarType.class, ((String)cartype.get(0)[0]+'-'+carRentalCompanyName));
        return cartypefound;
    }
    
    
    
}