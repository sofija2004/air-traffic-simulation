package sim;

public class Aerodrom {
	
     final String naziv;
     final String kod;
     int x, y;

    public Aerodrom(String naziv, String kod, int x, int y) {
        this.naziv = naziv;
        this.kod = kod;
        this.x = x;
        this.y = y;
    }
}