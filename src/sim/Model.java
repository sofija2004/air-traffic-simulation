package sim;

import java.util.*;

class Model {
	
    final Map<String, Aerodrom> aerodromi = new LinkedHashMap<>();
    final List<Let> letovi = new ArrayList<>();

    
    private String selectedKod = null; //nista nije selektovano
    private final Set<String> visible = new HashSet<>(); //kodovi koji se crtaju
   
    String getSelectedKod() {return selectedKod;}
    
    boolean isVisible (String kod) {
    		return visible.contains(kod);
    }
    
    // selektovanje i  deselektovanje aerodroma
    void toggleSelection(String kod) {
        if (kod == null)
            return;

        // ako je vec selektovan isti aerodrom ponistavam selekciju
        if (kod.equals(selectedKod)) {
            selectedKod = null;
        } 
        // inace selektujem novi aerodrom
        else {
            selectedKod = kod;
        }
        notifyAllObservers();
    }


    // postavljanje vidljivosti jednog aerodroma
    
    synchronized void setAerodromVisible(String kod, boolean v) {
        if (v) visible.add(kod); 
        else visible.remove(kod);
        notifyAllObservers();
    }
    
    
    
    // Posmatraci
    interface Observer { void modelChanged(); }
    private final List<Observer> observers = new ArrayList<>();

    void addObserver(Observer o) { observers.add(o); }
    private void notifyAllObservers() { for (Observer o : observers) o.modelChanged(); }

    public void clear() {
    	aerodromi.clear(); 
    	letovi.clear();
    	notifyAllObservers();
    }
    
    public void fireChange() { notifyAllObservers(); }
    
    // Dodavanje aerodroma
    
    String addAerodrom(Aerodrom a) {
        if (a == null) return "Aerodrom nije prosleđen.";
        if (a.kod == null || a.kod.length() != 3) return "Kod aerodroma mora imati tačno 3 slova.";
        if (aerodromi.containsKey(a.kod)) return "Aerodrom sa kodom " + a.kod + " već postoji.";
        if (a.x < -90 || a.x > 90 || a.y < -90 || a.y > 90) return "Koordinate moraju biti u rasponu [-90, 90].";
        aerodromi.put(a.kod, a);
        visible.add(a.kod);
        notifyAllObservers();
        return null;
    }
    
    synchronized void removeAerodrom(String kod) {
        if (!aerodromi.containsKey(kod)) return;
        letovi.removeIf(l -> l.od.equals(kod) || l.ka.equals(kod));
        aerodromi.remove(kod);
        visible.remove(kod);
        if (kod.equals(selectedKod)) selectedKod = null;
        notifyAllObservers();
    }

    
    // Dodavanje letova
    
    String addLet(Let l) {
        if (l == null) return "Let nije prosledjen.";
        if (!aerodromi.containsKey(l.od)) return "Polazni aerodrom (" + l.od + ") ne postoji.";
        if (!aerodromi.containsKey(l.ka)) return "Odredišni aerodrom (" + l.ka + ") ne postoji.";
        if (l.od.equals(l.ka)) return "Polazni i odredišni aerodrom ne mogu biti isti.";
        if (l.hh < 0 || l.hh > 23 || l.mm < 0 || l.mm > 59) return "Vreme mora biti HH:mm (0–23, 0–59).";
        if (l.trajanjeMin <= 0) return "Trajanje leta mora biti veće od 0 minuta.";
        letovi.add(l);
        notifyAllObservers();
        return null;
    }
    void addAerodromSilent(Aerodrom a) { 
    	aerodromi.put(a.kod, a);
    	visible.add(a.kod);
    	}
    void addLetSilent(Let l) { letovi.add(l); }
	
}
