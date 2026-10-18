package sim;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;


class ListaPodatakaPanel extends Panel implements Model.Observer {

    private final Model model;

    // Levo je lista aerodroma sa checkbox-ovima
    
    private final Panel pnlAirList = new Panel(new GridLayout(0, 1, 0, 4));
    private final Map<String, Checkbox> cbByKod = new LinkedHashMap<String, Checkbox>();

    // Desno je samo tektualni pregled letova
    
    private final TextArea taLet = new TextArea("", 12, 30, TextArea.SCROLLBARS_VERTICAL_ONLY);
    
    ListaPodatakaPanel(Model model) {
  
        super(new BorderLayout(8, 8));
        this.model = model;
        model.addObserver(this);

        // Prvi red ce biti naslovi
        Panel header = new Panel(new GridLayout(1, 2, 8, 0));
        
        header.add(heading("Airports (show/hide)"));
        header.add(heading("Flights"));
        add(header, BorderLayout.NORTH);

        // Levo je lista aerodroma (sa skrolom)
        Panel leftWrap = new Panel(new BorderLayout());
        leftWrap.add(pnlAirList, BorderLayout.NORTH);  // ovo ih skuplja

        ScrollPane spLeft = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        spLeft.add(leftWrap);

        // Desno: letovi
        taLet.setEditable(false);

        Panel center = new Panel(new GridLayout(1, 2, 8, 0));
        center.add(spLeft);
        center.add(taLet);
        add(center, BorderLayout.CENTER);

        setBackground(new Color(250, 250, 250));
        setPreferredSize(new Dimension(340, 600));

        refresh();
    }

    private static Label heading(String s) {
        Label l = new Label(s, Label.CENTER);
        l.setFont(new Font("Dialog", Font.BOLD, 13));
        return l;
    }

   
    void refresh() {
        refreshAirports();
        refreshFlights();
    }

    
    private void refreshAirports() {
        // Dodajem nove ili osvezavam stare checkboxove
    	
    	
        for (final Aerodrom a : model.aerodromi.values()) {
        	
            Checkbox cb = cbByKod.get(a.kod);
            String label = a.kod + " - " + a.naziv + " (" + a.x + "," + a.y + ")";
           
            if (cb == null) {
                cb = new Checkbox(label, model.isVisible(a.kod));
                final String kodFinal = a.kod;
                
                cb.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        boolean checked = (e.getStateChange() == ItemEvent.SELECTED);
                        model.setAerodromVisible(kodFinal, checked);
                    }
                });
                
                cbByKod.put(a.kod, cb);
                
                
                Panel row = new Panel (new BorderLayout());
                row.add(cb,BorderLayout.CENTER);
                
                Button btnDel = new Button("X");
                btnDel.setForeground(Color.RED);
                btnDel.addActionListener(ev-> model.removeAerodrom(kodFinal));
                row.add(btnDel,BorderLayout.EAST);
                
                
                pnlAirList.add(cb);
            } else {
                cb.setLabel(label);
                cb.setState(model.isVisible(a.kod));
            }
        }

    

        pnlAirList.revalidate();
        pnlAirList.repaint();
    }

    // Desno je lista letova
    
    private void refreshFlights() {
        StringBuilder sb = new StringBuilder();
        for (Let l : model.letovi) {
  
            // if (!model.isVisible(l.od) || !model.isVisible(l.ka)) continue;
            // prikazujem letove izmedju vidljivih aerodroma
            sb.append(l.od)
              .append(" -> ").append(l.ka)
              .append(" ")
              .append(l.vremeStr())
              .append("  (")
              .append(l.trajanjeMin)
              .append(" min)\n");
        }
        
        taLet.setText(sb.toString());
    }

    // Pozvace sa automatski kad se model promeni
    @Override
    public void modelChanged() {
        refresh();
    }
}
/*
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 

private void refreshAirports() {
    
    pnlAirList.removeAll();

    for (final Aerodrom a : model.aerodromi.values()) {
      
        Panel row = new Panel(new FlowLayout(FlowLayout.LEFT, 6, 0));

        // 2) Checkbox
        String label = a.kod + " - " + a.naziv + " (" + a.x + "," + a.y + ")";
        final Checkbox cb = new Checkbox(label, model.isVisible(a.kod));
        cb.addItemListener(e -> model.setAerodromVisible(a.kod, cb.getState()));

        // 3) Dugme "X" (malo, crveno)
        Button btn = new Button("X");
        btn.setForeground(Color.RED);
        btn.setFont(new Font("Dialog", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(22, 22));  // prisilno da se vidi
        btn.addActionListener(ev -> model.removeAerodrom(a.kod));

        
        row.add(cb);
        row.add(btn);

        // 5) Ubaci red u listu
        pnlAirList.add(row);
    }

   
   
    pnlAirList.invalidate();
    pnlAirList.validate();
    pnlAirList.repaint();

    if (pnlAirList.getParent() != null) {
        pnlAirList.getParent().validate(); // leftWrap
        if (pnlAirList.getParent().getParent() != null)
            pnlAirList.getParent().getParent().validate(); // ScrollPane
    }
}
*/
