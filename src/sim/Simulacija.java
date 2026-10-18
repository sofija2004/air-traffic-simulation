package sim;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Simulacija extends Frame {

    public static void main(String[] args) {
        new Simulacija();
    }

    
    private final Model model = new Model();
    private final Mapa canvas;
    private final TajmerNeaktivnosti idle;
    private final ListaPodatakaPanel pregled = new ListaPodatakaPanel(model);
    private final Label lblClock = new Label("Time: 00:00");
    private java.util.Timer uiClock;
    private SimulacijaLeta sim;

    
    public Simulacija() {

        super("Simulacija letova");
        setLayout(new BorderLayout());

        // Tajmer neaktivnosti
        idle = new TajmerNeaktivnosti(this, new Runnable() {
            public void run() { dispose(); }
        });

        setMenuBar(makeMenu());

        
        canvas = new Mapa(model, new Mapa.IdlePauser() {
            public void setPaused(boolean p) { idle.setPaused(p); }
        });
        sim = new SimulacijaLeta(model, canvas, new Runnable() {
            public void run() { canvas.repaint(); }
        });
        canvas.setSimulacija(sim);
        add(canvas, BorderLayout.CENTER);

      
        pregled.setPreferredSize(new Dimension(340, 600));
        add(pregled, BorderLayout.EAST);

       
        Panel south = new Panel(new GridLayout(1, 2, 10, 0));

        PanelUnosAerodroma pAer = new PanelUnosAerodroma(model, new Runnable() {
            public void run() { osvezi(); }
        });

        PanelUnosLeta pLet = new PanelUnosLeta(model, new Runnable() {
            public void run() { osvezi(); }
        });
     
        Panel controls = new Panel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        Button start = new Button("Start");
        Button pause = new Button("Pause");
        Button reset = new Button("Reset");

        controls.add(start); 
        controls.add(pause);
        controls.add(reset);
    
        controls.add(new Label("  |  "));
        controls.add(lblClock);
        add(controls, BorderLayout.NORTH);
        Color pink = new Color(255, 230, 235);
        pAer.setBackground(pink);
        pLet.setBackground(pink);

        south.add(pAer);
        south.add(pLet);
        south.setPreferredSize(new Dimension(900, 220));
        add(south, BorderLayout.SOUTH);

        // globalni listener za aktivnost
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent e) {
                idle.userAction();
            }
        }, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

        // prozor
        pack();
        setLocation(640, 160);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
      
        canvas.setSimulacija(sim);

        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sim.start();
                startClockUpdater();     
                idle.setPaused(true);
            }
        });
        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sim.start();
                idle.setPaused(true); // pauzira neaktivnost
            }
        });
        pause.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){ sim.pause(); }
        });
        reset.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sim.stop();
                stopClockUpdater();      // <<< NOVO
                lblClock.setText("Time: 00:00");
                idle.setPaused(false);
            }
        });
        
        setVisible(true);
    }

   
    private void startClockUpdater(){
        stopClockUpdater();
        uiClock = new java.util.Timer(true);
        uiClock.scheduleAtFixedRate(new java.util.TimerTask(){
            @Override public void run(){
                // osvezi labelu na AWT niti
                EventQueue.invokeLater(new Runnable(){
                    public void run(){
                        if (sim!= null) lblClock.setText("Time: " + sim.getTimeStr());
                    }
                });
            }
        }, 0, 500); // na 0.5s
    }
    private void stopClockUpdater(){
        if (uiClock != null) { uiClock.cancel(); uiClock = null; }
    }

    private void osvezi() {
        canvas.repaint();
        pregled.refresh();
    }

    private MenuBar makeMenu() {

        MenuBar mb = new MenuBar();
        Menu file = new Menu("Fajl");

        MenuItem open = new MenuItem("Učitaj CSV...");
        MenuItem save = new MenuItem("Sačuvaj CSV...");

        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { doOpen(); }
        });

        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { doSave(); }
        });

        file.add(open);
        file.add(save);
        mb.add(file);
        return mb;
    }

    private void doOpen() {
        FileDialog fd = new FileDialog(this, "Učitaj CSV", FileDialog.LOAD);
        fd.setVisible(true);
        if (fd.getFile() == null) return;

        File f = new File(fd.getDirectory(), fd.getFile());
        String greska = CsvStorage.load(model, f);
        if (greska != null) {
            showError(greska);
        } else {
            osvezi();
            showMessage("Podaci su uspešno učitani.");
        }
    }

    private void doSave() {
        FileDialog fd = new FileDialog(this, "Sačuvaj CSV", FileDialog.SAVE);
        fd.setFile("letovi.csv");
        fd.setVisible(true);
        if (fd.getFile() == null) return;

        File f = new File(fd.getDirectory(), fd.getFile());
        String greska = CsvStorage.save(model, f);
        if (greska != null) showError(greska);
        else showMessage("Podaci su uspešno sačuvani.");
    }

    private void showError(String poruka) {
        Dialog d = new Dialog(this, "Greška", true);
        d.setLayout(new BorderLayout());
        Label l = new Label(poruka, Label.CENTER);
        d.add(l, BorderLayout.CENTER);
        Button ok = new Button("U redu");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { d.dispose(); }
        });
        Panel p = new Panel(); p.add(ok);
        d.add(p, BorderLayout.SOUTH);
        d.setSize(400, 150);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showMessage(String poruka) {
        Dialog d = new Dialog(this, "Informacija", true);
        d.setLayout(new BorderLayout());
        d.add(new Label(poruka, Label.CENTER), BorderLayout.CENTER);
        Button ok = new Button("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { d.dispose(); }
        });
        Panel p = new Panel(); p.add(ok);
        d.add(p, BorderLayout.SOUTH);
        d.setSize(300, 130);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}
/*
AERODROM,AAA,Tesla,40,45,
AERODROM,BBB,Nis,-20,60,
AERODROM,XXX,Subotica,-50,20,
LET,XXX,BBB,1,20,120
LET,XXX,AAA,1,20,100
LET,AAA,BBB,2,20,200

*/