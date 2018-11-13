package session;

import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type, int carId);
    
    public int getNumberOfReservations(String company, String type);
    
    public void addCRC(CarRentalCompany crc);
    
    public void addCarToCompany(Car car, String crc);
    
    public void addCar(Car car, CarRentalCompany crc);
    
    
      
}