package session;

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
import rental.RentalStore;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    EntityManager em;
    
    
    @Override
    public void addCRC(String crc){
        em.persist(crc);
    }
    
    @Override
    public void addCarType(String cartype){
        em.persist(cartype);
    }
    
    @Override
    public void addCar(int carId){
        em.persist(carId);
    }
    @Override
    public Set<CarType> getCarTypes(String company) {
        try {
            List<CarType> cartypeslist = em.createNamedQuery("allCarTypesOfCompany")
                .setParameter("companyName", company)
                .getResultList();
            Set<CarType> cartypes = new HashSet<CarType>(cartypeslist);
            return cartypes;
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