// PanelFilterAerodroma.java
package sim;

import java.awt.*;
import java.util.*;

class PanelFilterAerodroma extends Panel implements Model.Observer {
	
    private final Model model;
    private final Panel list = new Panel(new GridLayout(0,1,0,4)); // redovi

    PanelFilterAerodroma(Model model){
    	
        super(new BorderLayout(8,8));
        this.model = model;
        model.addObserver(this);

        Label title = new Label("Airports", Label.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 13));
        add(title, BorderLayout.NORTH);

        ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        sp.add(list);
        add(sp, BorderLayout.CENTER);

        setPreferredSize(new Dimension(320, 520));
        setBackground(new Color(245,245,250));
        refresh();
    }

   
    private void refresh() {

        list.removeAll();

        for (Aerodrom a : model.aerodromi.values()) {
            String label = a.kod + " — " + a.naziv + " (" + a.x + ", " + a.y + ")";
            Checkbox cb = new Checkbox(label, model.isVisible(a.kod));
            
            cb.addItemListener(e -> model.setAerodromVisible(a.kod, cb.getState()));
            
            list.add(cb);
        }
        
        list.revalidate();
        list.repaint();
    }
    /*
    private void refresh() {
        list.removeAll();

        for (Aerodrom a : model.aerodromi.values()) {
            // Napravi red-panel (da bi mogao Checkbox + dugme)
            Panel row = new Panel(new BorderLayout(6, 0));

            
            String label = a.kod + " — " + a.naziv + " (" + a.x + ", " + a.y + ")";
            Checkbox cb = new Checkbox(label, model.isVisible(a.kod));
            cb.addItemListener(e -> model.setAerodromVisible(a.kod, cb.getState()));
            row.add(cb, BorderLayout.CENTER);

            
            Button btn = new Button("X");
            btn.setForeground(Color.RED);
            btn.setFont(new Font("Dialog", Font.BOLD, 12));
            btn.addActionListener(ev -> model.removeAerodrom(a.kod));
            row.add(btn, BorderLayout.EAST);

            
            list.add(row);
        }

      
        list.revalidate();
        list.repaint();
    }
*/

    @Override public void modelChanged(){ refresh(); }
}


/*
 * cb.addItemListener(new ItemListener() {
    @Override
    public void itemStateChanged(ItemEvent e) {
        model.setAerodromVisible(a.kod, cb.getState());
    }
});

 */
