package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javafx.util.Pair;
import javax.naming.InitialContext;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import session.CarRentalSessionRemote;
import session.ManagerSessionRemote;

public class Main extends AbstractTestManagement<CarRentalSessionRemote, ManagerSessionRemote> {
 
    public Main(String scriptFile) {
        super(scriptFile);
    }

    public static void main(String[] args) throws Exception {
        // TODO: use updated manager interface to load cars into companies
                
        CarRentalCompany hertzCompany = loadRental("hertz.csv");
        CarRentalCompany dockxCompany = loadRental("dockx.csv");
        
        Main main = new Main("trips");
        
        ManagerSessionRemote managerSession = main.getNewManagerSession("", "");
        System.out.println("add hertz..");
        managerSession.addCRC(hertzCompany);
        System.out.println("..hertz geadd");
        System.out.println("add dockx..");
        managerSession.addCRC(dockxCompany);
        System.out.println("..dockx geadd");
        
        main.run();
    }
    
    public static CarRentalCompany loadRental(String datafile) throws NumberFormatException, IOException {
        CrcData data = loadData(datafile);
        CarRentalCompany company = new CarRentalCompany(data.name, data.regions, data.cars);
        return company;
    }
    
    private static int nextuid = 0;
    
    public static CrcData loadData(String datafile)
            throws NumberFormatException, IOException {

        CrcData out = new CrcData();
        StringTokenizer csvReader;
       
        //open file from jar
        BufferedReader in = new BufferedReader(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream(datafile)));
        
        try {
            while (in.ready()) {
                String line = in.readLine();
                
                if (line.startsWith("#")) {
                    // comment -> skip					
                } else if (line.startsWith("-")) {
                    csvReader = new StringTokenizer(line.substring(1), ",");
                    out.name = csvReader.nextToken();
                    out.regions = Arrays.asList(csvReader.nextToken().split(":"));
                } else {
                    csvReader = new StringTokenizer(line, ",");
                    //create new car type from first 5 fields
                    CarType type = new CarType(csvReader.nextToken(),
                            Integer.parseInt(csvReader.nextToken()),
                            Float.parseFloat(csvReader.nextToken()),
                            Double.parseDouble(csvReader.nextToken()),
                            Boolean.parseBoolean(csvReader.nextToken()),
                            out.name);
                    //create N new cars with given type, where N is the 5th field
                    for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
                        nextuid++;
                        System.out.println(i + " adding car " + nextuid);
                        out.cars.add(new Car(nextuid, type));
                    }        
                }
            } 
        } finally {
            in.close();
        }

        return out;
    }
    
    static class CrcData {
            public List<Car> cars = new LinkedList<Car>();
            public String name;
            public List<String> regions =  new LinkedList<String>();
    }

    @Override
    protected Set<String> getBestClients(ManagerSessionRemote ms) throws Exception {
        return ms.getBestClients();
    }

    @Override
    protected String getCheapestCarType(CarRentalSessionRemote session, Date start, Date end, String region) throws Exception {
        return session.getCheapestCarType(start, end, region);
    }

    @Override
    protected CarType getMostPopularCarTypeIn(ManagerSessionRemote ms, String carRentalCompanyName, int year) throws Exception {
        return ms.getMostPopularCarTypeIn(carRentalCompanyName, year);
    }

    @Override
    protected CarRentalSessionRemote getNewReservationSession(String name) throws Exception {
        CarRentalSessionRemote out = (CarRentalSessionRemote) (new InitialContext()).lookup(CarRentalSessionRemote.class.getName());
        out.setRenterName(name);
        return out;
    }

    @Override
    protected ManagerSessionRemote getNewManagerSession(String name, String carRentalName) throws Exception {
        ManagerSessionRemote out = (ManagerSessionRemote) (new  InitialContext()).lookup(ManagerSessionRemote.class.getName());
        return out;
    }

    @Override
    protected void checkForAvailableCarTypes(CarRentalSessionRemote session, Date start, Date end) throws Exception {
        List<CarType> cartypes = session.getAvailableCarTypes(start, end);
        for (CarType ct : cartypes){
            System.out.println(ct);
        }
    }

    @Override
    protected void addQuoteToSession(CarRentalSessionRemote session, String name, Date start, Date end, String carType, String region) throws Exception {
        ReservationConstraints rs = new ReservationConstraints(start,end,carType,region);
        session.createQuote(name, rs);
    }

    @Override
    protected List<Reservation> confirmQuotes(CarRentalSessionRemote session, String name) throws Exception {
        List<Reservation> res = session.confirmQuotes();
        return res;
    }

    @Override
    protected int getNumberOfReservationsBy(ManagerSessionRemote ms, String clientName) throws Exception {
        return ms.getNumberOfReservationsBy(clientName);
    }

    @Override
    protected int getNumberOfReservationsForCarType(ManagerSessionRemote ms, String carRentalName, String carType) throws Exception {
        return ms.getNumberOfReservationsForCarType(carRentalName, carType);
    }
}