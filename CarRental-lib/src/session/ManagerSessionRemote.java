package session;

import java.util.Date;
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
    
    public int getNumberOfReservationsForCarType(String company, String type);
    
    public void addCRC(CarRentalCompany crc);
    
    public void addCarToCompany(Car car, String crc);
    
    public void addCar(Car car, CarRentalCompany crc);
    
    public Set<String> getBestClients();
    
    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year);
    
    public int getNumberOfReservationsBy(String clientName);
    
      
}