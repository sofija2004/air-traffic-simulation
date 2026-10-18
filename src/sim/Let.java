package sim;


public class Let {
	
	String od, ka;          // sifre 3 slova 
    int hh, mm;             // polazak (sat, minut)
    int trajanjeMin;       
    Let(String od, String ka, int hh, int mm, int trajanjeMin) {
        this.od = od;
        this.ka = ka;
        this.hh = hh;
        this.mm = mm; 
        this.trajanjeMin = trajanjeMin;
    }
    String vremeStr() { return String.format("%02d:%02d", hh, mm); }
}