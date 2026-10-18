package sim;

import java.awt.*;
import java.awt.event.*;

class PanelUnosAerodroma extends Panel {
	
    PanelUnosAerodroma(Model model, Runnable onChange) {
    	
        super(new GridBagLayout());
        setBackground(new Color(238, 240, 252));
        setFont(new Font("Dialog", Font.PLAIN, 13));

        GridBagConstraints c = new GridBagConstraints();
        //dodaje razmak gore dole levo desno
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // naslov sekcije
        Label title = bold("Add Airport");
        c.gridx = 0; c.gridy = 0; 
        c.gridwidth = 2;
        c.weightx = 1; //dozvoljava da uzme raspolozivu sirinu ako se prozor siri
        add(title, c);

        // U redu 1 je naziv aerodroma
        c.gridwidth = 1; c.weightx = 0;
        c.gridy = 1; c.gridx = 0; add(right("Airport name:"), c);
        TextField tNaziv = new TextField();
        pref(tNaziv, 20);
        c.gridx = 1; c.weightx = 1; add(tNaziv, c);

        // U redu 2 je kod aerodroma
        c.gridy = 2; c.gridx = 0; 
        c.weightx = 0; 
        add(right("Code (AAA):"), c);
        TextField tKod = new TextField();
        pref(tKod, 8);
        c.gridx = 1; c.weightx = 1;
        add(tKod, c);

        // U redu 3 je X koordinata
        c.gridy = 3; c.gridx = 0;
        c.weightx = 0; add(right("X (-90,90):"), c);
        TextField tX = new TextField();
        pref(tX, 10);
        c.gridx = 1; c.weightx = 1;
        add(tX, c);

        // U redu 4 je Y koordinata
        c.gridy = 4; c.gridx = 0; 
        c.weightx = 0;
        add(right("Y (-90,90):"), c);
        TextField tY = new TextField();
        pref(tY, 10);
        c.gridx = 1; c.weightx = 1;
        add(tY, c);

        // U redu 5 je dugme za dodavanje aerodroma
        Button btn = new Button("Add Airport");
        c.gridy = 5; c.gridx = 0;
        c.gridwidth = 2; c.weightx = 1;
        add(btn, c);

        btn.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    String naziv = tNaziv.getText().trim();
                    String kod   = tKod.getText().trim().toUpperCase();
                    int x = Integer.parseInt(tX.getText().trim());
                    int y = Integer.parseInt(tY.getText().trim());

                    String greska = model.addAerodrom(new Aerodrom(naziv, kod, x, y));
                    if (greska == null) {
                        // uspesno
                        tNaziv.setText("");
                        tKod.setText(""); 
                        tX.setText("");
                        tY.setText("");
                        onChange.run();
                    } else {
                        showError(greska);
                    }
                } catch (NumberFormatException ex) {
                    showError("X i Y moraju biti celi brojevi u opsegu [-90, 90].");
                } catch (Exception ex) {
                    showError("Neocekivana greška: " + ex.getMessage());
                }
            }
        });

    }
    public void showError(String msg) {
        Dialog d = new Dialog((Frame)getParent().getParent(), "Greška", true);
        d.setLayout(new BorderLayout());
        d.add(new Label(msg, Label.CENTER), BorderLayout.CENTER);
        Button ok = new Button("U redu");
        ok.addActionListener(ev -> d.dispose());
        Panel p = new Panel(); p.add(ok);
        d.add(p, BorderLayout.SOUTH);
        d.setSize(420, 160);
        d.setLocationRelativeTo(null);
        d.setVisible(true);
    }

  
    private static Label bold(String s){ 
    	Label l= new Label(s);
	    l.setFont(new Font("Dialog", Font.BOLD, 13)); 
	    return l; 
	    }
    
    private static Label right(String s){ 
    	Label l = new Label(s, Label.RIGHT);
    	return l; }
    
    private static void pref(TextField tf, int cols){ 
    	tf.setPreferredSize(new Dimension(cols*10, 22)); 
    }
}
