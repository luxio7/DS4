package session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    EntityManager em;
    
    public List<CarRentalCompany> getAllCarRentalCompanies(){
        List<CarRentalCompany> allCrc = em.createNamedQuery("getAllRentalCompaniesObject").getResultList();
        return allCrc;
    }
    
    @Override
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
            car.setType(carType);
        crc.addCar(car);
    }
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        try {
            return new HashSet(em.createNamedQuery("getAllCarTypesByCarRentalCompanyName").setParameter("givenName",company).getResultList());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            List<Integer> idlist = em.createNamedQuery("allCarIdsOfType")
                       .setParameter("companyName", company)
                       .setParameter("carTypeName", type)
                       .getResultList();
            Set<Integer> id = new HashSet<Integer>(idlist);
            return id;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {
        try {
            List<Reservation> nbres = em.createNamedQuery("allReservationsForCarId")
                .setParameter("companyName", company)
                .setParameter("carId", id)
                .getResultList();
            return nbres.size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public int getNumberOfReservations(String company, String type) {
        try {
            List<Reservation> nbres = em.createNamedQuery("allReservationsForCarId")
                .setParameter("companyName", company)
                .getResultList();
            return nbres.size();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }

    }

}