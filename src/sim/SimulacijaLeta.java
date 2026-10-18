package sim;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class SimulacijaLeta implements Runnable {

    private final Model model;
    private final Mapa mapa;
    private final Runnable onStop;
   // letovi koji su vec pokrenuti
    private final Set<Let> pokrenuti = new HashSet<>();

    private volatile boolean running = false;
    private volatile boolean paused  = false;

   
    private final List<AktivanLet> aktivni = new CopyOnWriteArrayList<AktivanLet>();

    // vreme simulacije u minutima od 00:00
    private int vremeMin = 0;

    // poslednji dozvoljeni polazak (u min simulacije)
    private final Map<String,Integer> lastDepartMin = new HashMap<String,Integer>();

    private Thread nit;

    SimulacijaLeta(Model model, Mapa mapa, Runnable onStop) {
        this.model  = model;
        this.mapa   = mapa;
        this.onStop = onStop;
    }

    void start() {
        if (running) return;
        running = true;
        paused  = false;
        nit = new Thread(this, "SimLeta");
        nit.start();
    }

    void pause() { paused = !paused; }

    void stop() {
    	
    	pokrenuti.clear();

        running = false;
        paused  = false;
        aktivni.clear();
        vremeMin = 0;
        lastDepartMin.clear();
        mapa.repaint();
        if (onStop != null) onStop.run();
    }

    public void run() {
        long acc = 0L;
        final long tick = 200L;  //  200 ms

        try {
            while (running) {
                long start = System.currentTimeMillis();

                if (!paused) {
                    acc += tick;

                    // 10 min simulacije po 1000 ms realnog vremena
                    double simMinExact = vremeMin + (acc / 1000.0) * 10.0;

                    // jednom u 1s poveca  vreme i pokrene nove letove
                    if (acc >= 1000L) {
                        acc -= 1000L;
                        vremeMin += 10;
                        pokreniNoveLetove();
                        simMinExact = vremeMin + (acc / 1000.0) * 10.0;
                    }

                    azurirajPozicije(simMinExact);  // azuriranje svakih 200 ms
                    mapa.repaint();
                }

                long dt = System.currentTimeMillis() - start;
                long sleep = tick - dt;
                if (sleep < 0) sleep = 0;
                Thread.sleep(sleep);
            }
        } catch (InterruptedException ignore) {}
    }

    private void pokreniNoveLetove() {
     
        List<Let> plan = new ArrayList<>(model.letovi);
        Collections.sort(plan, new Comparator<Let>() {
            public int compare(Let a, Let b) {
                int ta = a.hh*60 + a.mm;
                int tb = b.hh*60 + b.mm;
                return ta - tb;
            }
        });

        for (Let l : plan) {
            int tPolazak = l.hh*60 + l.mm;
            if (tPolazak > vremeMin) break;         // jos nije vreme po planu
            //if (tPolazak >vremeMin +30) continue;
            if (pokrenuti.contains(l)) continue;    // vec je pokrenut jednom
            if (postojiAktivan(l)) continue;        // vec leti

            Integer last = lastDepartMin.get(l.od);
            if (last == null || (vremeMin - last) >= 10) {
                int start = Math.max(tPolazak, vremeMin); // ne pre plana i ne u proslosti
                aktivni.add(new AktivanLet(l, start));
            	//int start = tPolazak - 30;   // pojavi se 30 min ranije
            	//if (start < 0) start = 0;
            	//aktivni.add(new AktivanLet(l, start));


            	
            	lastDepartMin.put(l.od, start);
                pokrenuti.add(l);                   //  obelezi kao pokrenut
            }
            // inace ceka sledeci tick
        }

    }

    private boolean postojiAktivan(Let l) {
        for (AktivanLet a : aktivni)
            if (a.let == l) return true;
        return false;
    }

    //Svakih 200ms pomera aktivne avione i uklanja zavrsene
     
    private void azurirajPozicije(double simMinExact) {
        aktivni.removeIf(a -> {
            a.update(simMinExact);
            return a.zavrsen;
        });
    }
   /*
        for (Let l : model.letovi) {
            int tPolazak = l.hh * 60 + l.mm;

        
            if (tPolazak - 30 <= vremeMin && tPolazak > vremeMin && !postojiAktivan(l)) {
                Aerodrom a = model.aerodromi.get(l.od);
                if (a != null) {
                    mapa.showPreflightCircle(a.x, a.y);
                }
            }
        }

    }*/


    class AktivanLet {
        final Let let;
        final int startMinPlan;    // planirani ili odobreni pocetak
        final Point pOd, pKa;      // koordinate
        boolean zavrsen = false;
        double x, y;               // trenutna pozicija
     // u SimulacijaLeta.AktivanLet
        int getStartMin() { return startMinPlan; }
        double getX() { return x; }
        double getY() { return y; }
        
        String getStartTimeStr() {
            int h = (startMinPlan / 60) % 24;
            int m = startMinPlan % 60;
            return String.format("%02d:%02d", h, m);
        }

        AktivanLet(Let l, int startMinPlan) {
            this.let = l;
            this.startMinPlan = startMinPlan;

            Aerodrom od = model.aerodromi.get(l.od);
            Aerodrom ka = model.aerodromi.get(l.ka);
            
            if (od == null || ka == null) {
                pOd = new Point(0,0);
                pKa = new Point(0,0);
                zavrsen = true;
                return;
            }

            pOd = new Point(od.x, od.y);
            pKa = new Point(ka.x, ka.y);
            x = od.x; y = od.y;
        }

        void update(double sadaMinExact) {
        	/*
        	if (sadaMinExact < startMinPlan) {
        	    // još nije poleteo: stoji na polazištu
        	    x = pOd.x;
        	    y = pOd.y;
        	    return;
        	}*/

            double dt = sadaMinExact - (double) startMinPlan; // minuti 
            if (dt < 0.0) return; // jos nije poleteo

            double proc = dt / (double) let.trajanjeMin; // 0.0 .. 1.0
            if (proc >= 1.0) { proc = 1.0; zavrsen = true; }

            // pravolinijsko kretanje konstantnom brzinom
            x = pOd.x + (pKa.x - pOd.x) * proc;
            y = pOd.y + (pKa.y - pOd.y) * proc;
        }
     
        int getPreostaloMinuta(double sadaMinExact) {
            double dt = sadaMinExact - (double) startMinPlan; 
            double ostalo = let.trajanjeMin - dt;
            return (int) Math.max(0, Math.round(ostalo));
        }


    }

    
    List<AktivanLet> getAktivni() { return aktivni; }

    double getCurrentSimTime() {
        return vremeMin;
    }
    String getTimeStr() {
        int h = (vremeMin / 60) % 24;
        int m = vremeMin % 60;
        return String.format("%02d:%02d", h, m);
    }
    
}
