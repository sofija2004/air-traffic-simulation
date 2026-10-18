package sim;

import java.awt.*;
import java.awt.event.*;

class PanelUnosLeta extends Panel implements Model.Observer {
	
	//referenciram centralni objekat model koji sadrzi sve podatke
    private final Model model;
    // onChange je funkcija koju ce ovaj panel da pozove kad se uspesno doda novi let
    // obavestava spoljasnji deo koda bez da zna koga obavestava
    private final Runnable onChange;
    // pravim padajuci meni za izbor aerodroma polaznih i dolaznih
    private final Choice chOd = new Choice();
    private final Choice chKa = new Choice();
    // vreme kad ce da poelti i koliko ce da traje
    private final TextField tVr = new TextField();
    private final TextField tTr = new TextField();
    // dugme da potvrdim dodavanje leta
    private final Button btn = new Button("Add Flight");
    
   // private final Label hint = new Label("", Label.RIGHT);

    PanelUnosLeta(Model model, Runnable onChange) {
    	
        super(new GridBagLayout());
        this.model = model;
        this.onChange = onChange;
        
        setBackground(new Color(255, 210, 220)); // roze
        setFont(new Font("Dialog", Font.PLAIN, 13));

        model.addObserver(this);
        populateUI();  
        refreshChoices(); // kao neka inicijalizacija
    }

    private void populateUI() {
    	
        GridBagConstraints c = new GridBagConstraints();
        //u ovom c definisem pravila za poziconiranje svakog elementa
        c.insets = new Insets(6, 8, 6, 8);
        //insets su margine 
        c.fill = GridBagConstraints.HORIZONTAL;
        // element ce da se rastegne horizontalno da dopuni sirinu
       
        Label title = bold("Add Flight");
        c.gridx = 0; c.gridy = 0;
        c.gridwidth = 3; c.weightx = 1;
        //zauzima 3 kolone a po sirini se rasteze
        add(title, c);

        // Odakle polece 
        c.gridy = 1; c.gridx = 0;
        c.gridwidth = 1; c.weightx = 0;
        add(right("Origin:"), c);
        
        c.gridx = 1; c.weightx = 1; 
        add(chOd, c);
        
        // Gde slece
        c.gridy = 2; c.gridx = 0; 
        c.weightx = 0; add(right("Destination:"), c);
        c.gridx = 1; c.weightx = 1;
        add(chKa, c);

        // Kada polece
        c.gridy = 3; c.gridx = 0;
        c.weightx = 0; add(right("Departure (HH:mm):"), c);
        pref(tVr, 8);
        c.gridx = 1; c.weightx = 1; add(tVr, c);

        // Koliko dugo leti
        c.gridy = 4; c.gridx = 0; c.weightx = 0; add(right("Duration (min):"), c);
        pref(tTr, 8);
        c.gridx = 1; c.weightx = 1; add(tTr, c);

       
        c.gridy = 5; 
        //c.gridx = 0; 
        // c.weightx = 1; 
        //add(hint, c);
        c.gridx = 1;
        c.weightx = 0; 
        add(btn, c);

        btn.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    String od = chOd.getSelectedItem();
                    String ka = chKa.getSelectedItem();
                    if (od == null || ka == null) return;
                    if (od.equals(ka)) return; // ne dozvoljavam isti aerodrom

                    // proveravam format vremena i konvertujem tekst u brojeve
                    String[] hhmm = tVr.getText().trim().split(":");
                    if (hhmm.length != 2) return;
                    int hh = Integer.parseInt(hhmm[0]);
                    int mm = Integer.parseInt(hhmm[1]);
                    int tr = Integer.parseInt(tTr.getText().trim());
                    //kreiram novi objekat Let i pokusavam da ga dodam u mode
                    //ako addLet vrati null znaci da je uspesno pa brisem unos i pozivam onChange.run()
                    String greska = model.addLet(new Let(od, ka, hh, mm, tr));
                   
                    if (greska == null) {
                        tVr.setText(""); tTr.setText("");
                        onChange.run();
                    } else {
                        showError(greska);
                    }

                } catch (Exception ignore) {}
            }
        });
    }

    private void refreshChoices() {
    	
        chOd.removeAll();
        chKa.removeAll();
        
        for (String kod : model.aerodromi.keySet()) {
            chOd.add(kod);
            chKa.add(kod);
        }
        boolean enabled = (model.aerodromi.size() >= 2);
        
        chOd.setEnabled(enabled);
        chKa.setEnabled(enabled);
        btn.setEnabled(enabled);
        //hint.setText(enabled ? "" : "Add at least 2 airports…");
        revalidate();
        repaint();
    }
    
    public void showError(String msg) {
    	// kreiram greska dijalog
        Dialog d = new Dialog((Frame)getParent().getParent(), "Greska", true);
        d.setLayout(new BorderLayout());
        d.add(new Label(msg, Label.CENTER), BorderLayout.CENTER);
        Button ok = new Button("U redu");
        ok.addActionListener(ev -> d.dispose());
        Panel p = new Panel(); 
        p.add(ok);
        d.add(p, BorderLayout.SOUTH);
        d.setSize(420, 160);
        d.setLocationRelativeTo(null);
        d.setVisible(true);
    }

    // --- helpers za stil ---
    private static Label bold(String s){ 
    	Label l=new Label(s);
    	l.setFont(new Font("Dialog", Font.BOLD, 13)); 
    	return l; 
    }
    private static Label right(String s){ 
    	return new Label(s, Label.RIGHT);
    }
    private static void pref(TextField tf, int cols){ 
    	tf.setPreferredSize(new Dimension(cols*10, 22));
    	}

    // Imlepemntiram metodu iz Observera tj kad se u modelu nesto promeni
    // Model ce pozvati sve svoje observere da se ozveze
    // Kako ce se osveziti zavisi od impelemtaacije:
    
    @Override public void modelChanged() { 
    	refreshChoices(); }
}
