package sim;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;

class Mapa extends Canvas implements Model.Observer {
  
	interface IdlePauser { 
		void setPaused (boolean p);
	}
	
	private final Model model;
	private final IdlePauser pauser;
	private final Color GRAY = new Color(160,160,160);
	private Timer blinkTimer;
	private boolean blinkOn = false;
	private SimulacijaLeta sim;
	
	/*private Point tempCircle = null;
	private Aerodrom dragAer = null;
	private int dragDx = 0, dragDy = 0; // offset između klika i centra kvadrata
	private static final int AIRPORT_HIT_R = 8;
	private float zoom = 1.0f;
	private int offsetX = 0, offsetY = 0;
	*/
	// pamti poslednjih N tacaka po aktivnom letu
	//private final java.util.Map<SimulacijaLeta.AktivanLet, java.util.Deque<Point>> trail = new java.util.HashMap<>();
	//private static final int TRAIL_LEN = 80;   // duzia traga u tačkama

/*
	void showCircle(int x, int y) {
	    tempCircle = new Point(x, y);
	    repaint();
	}
*/

    Mapa(Model model, IdlePauser pauser){
    	
        this.model = model;
        this.pauser = pauser;
        model.addObserver(this);
        setBackground(new Color(220, 240, 255));
        
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                /*
                SimulacijaLeta.AktivanLet hitPlane = hitTestPlane(e.getPoint());
                if (hitPlane != null) {
                    showFlightDialog(e.getXOnScreen(), e.getYOnScreen(), hitPlane);
                    return; 
                }*/
                
                String hit = hitTest(e.getPoint());
                if (hit == null) {
                    // klik u prazno deselektuj ako je nešto bilo selektovano
                    if (model.getSelectedKod() != null) model.toggleSelection(model.getSelectedKod());
                } else {
                    model.toggleSelection(hit);
                }
                updateBlinking(); // pokreni/zaustavi treperenje i pauzu
            }
        });
    }
    
       
    @Override public void modelChanged() {
        // osvezavam UI cim se model promeni (dodavanje aerodroma ili leta)
       
    	updateBlinking();
    	repaint();
    }

    @Override public Dimension getPreferredSize(){ return new Dimension(900, 520); }

    @Override public void paint(Graphics g){
        drawBorder(g);
        drawGrid(g);
        drawAirports(g);
        //drawFlights(g);
       // drawFlightsLive(g);
        if (sim != null) drawPlanes(g);
        //drawFlightsTrail( g);
        /*
        if (preflight != null) {
            Point p = mapToPix(preflight.x, preflight.y);
            g.setColor(new Color(0, 100, 255, 180));
            g.fillOval(p.x - 5, p.y - 5, 10, 10);
        }
        */


    }
    private void drawPlanes(Graphics g){
        if (sim == null) return;
        g.setColor(new Color(0,100,255));
        for (SimulacijaLeta.AktivanLet a : sim.getAktivni()){
            Point p = mapToPix((int)a.x, (int)a.y);
            g.fillOval(p.x-4, p.y-4, 8, 8);
        }
    }

    private void drawBorder(Graphics g){
        g.setColor(Color.DARK_GRAY);
        g.drawRect(20, 20, getWidth()-40, getHeight()-40);
    }

    private void drawGrid(Graphics g){
        g.setFont(new Font("Dialog", Font.PLAIN, 11));
        g.setColor(new Color(230,230,230));
        for (int i=-90;i<=90;i+=30){
            Point p1 = mapToPix(i, -90), p2 = mapToPix(i, 90);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
            Point p3 = mapToPix(-90, i), p4 = mapToPix(90, i);
            g.drawLine(p3.x, p3.y, p4.x, p4.y);
        }
        g.setColor(Color.GRAY);
        g.drawString("x,y ∈ [-90,90]", 28, getHeight()-28);
    }

    private void drawAirports(Graphics g){
    	String sel = model.getSelectedKod();
        for (Aerodrom a : model.aerodromi.values()){
            if (!model.isVisible(a.kod)) continue;
        	Point p = mapToPix(a.x, a.y);
            int R = 10;
            boolean selected = a.kod.equals(sel);
            Color fill = selected ? (blinkOn ? Color.RED : GRAY) : GRAY;

            g.setColor(fill);
            g.fillRect(p.x - R/2, p.y - R/2, R, R);

            g.setColor(Color.BLACK);
            g.drawRect(p.x - R/2, p.y - R/2, R, R);      // ivica
            g.drawString(a.kod, p.x + R + 3, p.y - 2);
        }
    }
    /*
    private void drawAerodromi(Graphics g) {
        for (Aerodrom a : model.aerodromi.values()) {
            if (!model.isVisible(a.kod)) continue;
            Point p = mapToPixZ(a.x, a.y);
            // nacrtaj mali kvadrat/krug — fiksne veličine
            int s = 10;
            g.setColor(java.awt.Color.BLUE);
            g.drawRect(p.x - s/2, p.y - s/2, s, s);
            g.setColor(java.awt.Color.BLACK);
            g.drawString(a.kod, p.x + s/2 + 3, p.y + s/2);
        }
    }
*/
    // koji je aerodrom kliknut (ako je klik unutar kvadrata)
    private String hitTest(Point m){
    	
        for (Aerodrom a : model.aerodromi.values()){
        	int R = 10;
            if (!model.isVisible(a.kod)) continue;
            Point p = mapToPix(a.x, a.y);
            Rectangle box = new Rectangle(p.x - R/2, p.y - R/2, R, R);
            if (box.contains(m)) return a.kod;
        }
        return null;
    }
    
    void setSimulacija(SimulacijaLeta s) { this.sim = s; }
    
    private void updateBlinking(){
        boolean hasSel = (model.getSelectedKod() != null);

        // Pauziraj tj nastavi tajmer neaktivnosti
        if (pauser != null) pauser.setPaused(hasSel);

        if (hasSel) {
            if (blinkTimer == null) {
                blinkTimer = new Timer(true);
                blinkTimer.scheduleAtFixedRate(new TimerTask(){
                    @Override public void run(){
                        blinkOn = !blinkOn;
                        repaint();
                    }
                }, 0, 400); // treperi na 0.4
            }
        } else {
            blinkOn = false;
            if (blinkTimer != null) { blinkTimer.cancel(); blinkTimer = null; }
        }
    }

    
    // mapiranje [-90,90] u unutrasnjost okvira (20px margine)
    private Point mapToPix(int gx, int gy){
    	
        int w = getWidth()-40, h = getHeight()-40;
        double nx = (gx + 90) / 180.0;     // 0..1
        double ny = 1.0 - (gy + 90) / 180.0; // invert Y
        int x = 20 + (int)Math.round(nx * w);
        int y = 20 + (int)Math.round(ny * h);
        return new Point(x,y);
        
    }
    /*
    private Point pixToMapZ(int px, int py) {
        int mx = Math.round((px - offsetX) / zoom);
        int my = Math.round((py - offsetY) / zoom);
        return new Point(mx, my);
    }
    private Point mapToPixZ(int mx, int my) {
        int x = Math.round(mx * zoom) + offsetX;
        int y = Math.round(my * zoom) + offsetY;
        return new Point(x, y);
    }

*/

    private SimulacijaLeta.AktivanLet hitTestPlane(Point m) {
        if (sim == null) return null;
        final int R = 8; // tolerancija u pikselima (isti kao precnik kruga)
        for (SimulacijaLeta.AktivanLet a : sim.getAktivni()) {
            Point p = mapToPix((int) a.getX(), (int) a.getY());
            if (Math.abs(m.x - p.x) <= R && Math.abs(m.y - p.y) <= R) {
                return a;
            }
        }
        return null;
    }
    /*
    public void addTrailSample(SimulacijaLeta.AktivanLet a) {
        if (a == null || a.zavrsen) return;
        Point p = mapToPix((int)Math.round(a.getX()), (int)Math.round(a.getY()));
        java.util.Deque<Point> q = trail.computeIfAbsent(a, k -> new java.util.ArrayDeque<>());
        // izbegni duplikate ako se nije pomerio
        Point last = q.peekLast();
        if (last == null || last.x != p.x || last.y != p.y) {
            q.addLast(p);
            while (q.size() > TRAIL_LEN) q.removeFirst();
        }
    }*/
    /*
    private Aerodrom hitAirport(int mxPix, int myPix) {
        for (Aerodrom a : model.aerodromi.values()) {
            if (!model.isVisible(a.kod)) continue;
            Point p = mapToPixZ(a.x, a.y);     // centar aerodroma u pikselima
            int dx = mxPix - p.x, dy = myPix - p.y;
            if (dx*dx + dy*dy <= AIRPORT_HIT_R*AIRPORT_HIT_R) return a;
        }
        return null;
    }
*/

}
/*
private void drawFlights(Graphics g){
	
    g.setColor(new Color(180, 0, 0));
    for (Let l : model.letovi){
        Aerodrom od = model.aerodromi.get(l.od);
        Aerodrom ka = model.aerodromi.get(l.ka);
        if (od == null || ka == null) continue;

        // crtamo samo ako su oba aerodroma trenutno vidljiva
        if (!model.isVisible(od.kod) || !model.isVisible(ka.kod)) continue;
        
        Point p1 = mapToPix(od.x, od.y),p2 = mapToPix(ka.x, ka.y);
        g.drawLine(p1.x, p1.y, p2.x, p2.y);

        int mx = (p1.x + p2.x)/2, my = (p1.y + p2.y)/2;
        g.drawString(l.vremeStr(), mx + 4, my - 4);
    }
}

*/
/*
private void drawFlightsLive(Graphics g) {
    // tanja crvena linija od polaznog aerodroma do trenutne pozicije
    Graphics2D g2 = (Graphics2D) g;
    Stroke old = g2.getStroke();
    Color oldC = g2.getColor();

    g2.setStroke(new BasicStroke(2f));
    g2.setColor(new Color(200, 30, 30));

    // crtamo SAMO aktivne letove iz simulacije
    java.util.List<SimulacijaLeta.AktivanLet> aktivni = sim.getAktivni();
    if (aktivni != null) {
        for (SimulacijaLeta.AktivanLet a : aktivni) {
            if (a == null || a.zavrsen) continue;

            Let l = a.let;
            Aerodrom od = model.aerodromi.get(l.od);
            if (od == null) continue;
            if (!model.isVisible(od.kod)) continue;

            // trenutna pozicija aviona (u map koord.) -> pikseli
            Point pCur = mapToPix((int)Math.round(a.getX()), (int)Math.round(a.getY()));
            Point pStart = mapToPix(od.x, od.y);

            // segment od starta do trenutne pozicije
            g2.drawLine(pStart.x, pStart.y, pCur.x, pCur.y);

            // mala strelica/marker na trenutnoj poziciji (po želji)
            g2.fillOval(pCur.x - 2, pCur.y - 2, 4, 4);
        }
    }

    g2.setStroke(old);
    g2.setColor(oldC);
}
*/
    /*
    private void drawFlightsTrail(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Stroke old = g2.getStroke();
        Color oldC = g2.getColor();

        // 1) polilinija (trag) – tanje i providnije
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(200, 30, 30, 150)); // blago providno

        for (java.util.Map.Entry<SimulacijaLeta.AktivanLet, java.util.Deque<Point>> e : trail.entrySet()) {
            SimulacijaLeta.AktivanLet a = e.getKey();
            if (a == null || a.zavrsen) continue;

            java.util.Deque<Point> q = e.getValue();
            if (q == null || q.size() < 2) continue;

            Point prev = null;
            for (Point p : q) {
                if (prev != null) g2.drawLine(prev.x, prev.y, p.x, p.y);
                prev = p;
            }

            // 2) marker na zadnjoj tački
            Point last = q.peekLast();
            if (last != null) g2.fillOval(last.x - 2, last.y - 2, 4, 4);

            // 3) (opciono) od starta do trenutne pozicije – kao u A)
            Let l = a.let;
            Aerodrom od = model.aerodromi.get(l.od);
            if (od != null && model.isVisible(od.kod)) {
                Point pStart = mapToPix(od.x, od.y);
                Point pCur   = last != null ? last : mapToPix((int)Math.round(a.getX()), (int)Math.round(a.getY()));
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(150, 0, 0, 120));
                g2.drawLine(pStart.x, pStart.y, pCur.x, pCur.y);
            }
        }

        g2.setStroke(old);
        g2.setColor(oldC);
    }
   

}

/*

    private void showFlightDialog(int screenX, int screenY, SimulacijaLeta.AktivanLet a) {
        Frame owner = (Frame) getParent().getParent();
        final Dialog d = new Dialog(owner, "Flight", false); // modeless sto znaci da ne blokira glavni prozor
        d.setLayout(new BorderLayout(8, 8));

        // veca labela i okvir
        final Label lbl = new Label("", Label.LEFT);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 14));
        lbl.setPreferredSize(new Dimension(380, 60));
        d.add(lbl, BorderLayout.CENTER);

        Button close = new Button("Close");
        close.addActionListener(ev -> d.dispose());
        Panel south = new Panel(); south.add(close);
        d.add(south, BorderLayout.SOUTH);

        d.pack();
        d.setSize(400, 100); // ❗ inicijalno veći dijalog
        d.setLocation(screenX + 10, screenY + 10);
        d.setVisible(true);

        java.util.Timer timer = new java.util.Timer(true);

        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override public void run() {
                if (!d.isVisible()) { cancel(); return; }
                if (sim == null || a == null || a.zavrsen || !sim.getAktivni().contains(a)) {
                    cancel();
                    EventQueue.invokeLater(d::dispose);
                    return;
                }

                double now = sim.getCurrentSimTime();
                int left  = a.getPreostaloMinuta(now);
                double xx = a.getX(), yy = a.getY();
                String txt = String.format(
                    "x=%.1f, y=%.1f | dep=%s->%s %s | left=%d min",
                    xx, yy, a.let.od, a.let.ka, a.getStartTimeStr(), left
                );

                Point planePix   = mapToPix((int)Math.round(xx), (int)Math.round(yy));
                Point canvasOnSc = getLocationOnScreen();
                int dlgX = canvasOnSc.x + planePix.x + 14;
                int dlgY = canvasOnSc.y + planePix.y - d.getHeight() - 12;
				// osvezava tekst u tabeli  i pomera dijalov
                EventQueue.invokeLater(() -> {
                    lbl.setText(txt);
                    d.setLocation(dlgX, dlgY);
                });
                // ako je let zavtsio - zatvori dijalog
                if (left <= 0) {
                    cancel();
                    EventQueue.invokeLater(d::dispose);
                }
            }
        }, 0, 300);
        // svakih 0.3 sekundi azurira
        // ciscenje tajmera
        d.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { timer.cancel(); }
            @Override public void windowClosed (WindowEvent e) { timer.cancel(); }
        });
    }
}
/*
	private void showFlightDialog(int screenX, int screenY, SimulacijaLeta.AktivanLet a) {
	    Frame owner = (Frame) getParent().getParent(); // zavisi od tvoje hijerarhije; ovo radi u tvom kodu
	    Dialog d = new Dialog(owner, "Flight info", true);
	    d.setLayout(new BorderLayout(10, 10));
	
	    double simMinExact = sim == null ? 0 : sim.getCurrentSimTime(); // moraš imati getter
		int ostalo = a.getPreostaloMinuta(simMinExact);
		
		String txt = String.format(
		    "Flight: %s → %s%n"
		  + "x = %.1f,  y = %.1f%n"
		  + "Departure: %s%n"
		  + "Time left: %d min",
		    a.let.od, a.let.ka,
		    a.getX(), a.getY(),
		    a.getStartTimeStr(),
		    ostalo
		);

	
	    TextArea ta = new TextArea(txt, 5, 28, TextArea.SCROLLBARS_NONE);
	    ta.setEditable(false);
	    d.add(ta, BorderLayout.CENTER);
	
	    Button ok = new Button("Close");
	    ok.addActionListener(ev -> d.dispose());
	    Panel south = new Panel(); south.add(ok);
	    d.add(south, BorderLayout.SOUTH);
	
	    d.pack();
	    d.setLocation(screenX + 8, screenY + 8); // prikazi tik do mesta klika
	    d.setVisible(true);
	}
	 addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                Aerodrom hit = hitAirport(e.getX(), e.getY());
                if (hit != null) {
                    dragAer = hit;
                    // zapamti offset da ne iskoci ispod kursora
                    Point p = mapToPix(hit.x, hit.y);
                    dragDx = e.getX() - p.x;
                    dragDy = e.getY() - p.y;
                }
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                dragAer = null;
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseDragged(java.awt.event.MouseEvent e) {
                if (dragAer == null) return;

                
                int px = e.getX() - dragDx;
                int py = e.getY() - dragDy;

                
                Point m = pixToMapZ(px, py);
                dragAer.x = m.x;
                dragAer.y = m.y;

                
                // model.notifyAllObservers();

                repaint();
            }
        });

        
    }
}

*/


