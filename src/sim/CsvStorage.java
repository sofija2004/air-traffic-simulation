package sim;

import java.io.*;
import java.util.*;

public class CsvStorage {

    // cuvanje trenutnog modela u csv fajl
	
    static String save(Model model, File file) {
    	
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(file));
            // Aerodromi: AERODROM,kod,naziv,x,y
            for (Aerodrom a : model.aerodromi.values()) {
                pw.println("AERODROM," + a.kod + "," + a.naziv + "," + a.x + "," + a.y);
            }
            // Letovi: LET,od,ka,hh,mm,trajanje
            for (Let l : model.letovi) {
                pw.println("LET," + l.od + "," + l.ka + "," + l.hh + "," + l.mm + "," + l.trajanjeMin);
            }
            return null;
        } catch (IOException e) {
            return "Greška pri čuvanju fajla: " + e.getMessage();
        } finally {
            if (pw != null) pw.close();
        }
    }

    // ucitavam iz csv fajla u model 
    
    static String load(Model model, File file) {
       
    	BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

            // čitamo sve linije, preskačemo prazne i komentare
            
            List<String> lines = new ArrayList<>();
            String linija;
            while ((linija = br.readLine()) != null) {
                linija = linija.trim();
                if (linija.isEmpty()) continue;
                if (linija.startsWith("#")) continue;
                lines.add(linija);
            }
            
            //Privremeni akumulatori da omoguce bilo koji rapsored aerodroma i letova u csv fajlu
            
            Map<String, Aerodrom> aerTmp = new LinkedHashMap<>();
            List<Let> letTmp = new ArrayList<>();

            int row = 0;
            for (String s : lines) {
                row++;
                String[] delovi = s.split(",");
                if (delovi.length == 0) continue;

                String type = delovi[0].trim();
                
                if (type.equalsIgnoreCase("AERODROM")) {
                    if (delovi.length < 5)
                        return greska(row, "Nedovoljno kolona za AERODROM (ocekivano 5: AERODROM,kod,naziv,x,y).");
                    
                    String kod = delovi[1].trim().toUpperCase();
                    String naziv = delovi[2].trim();
                    int x, y;
                    try {
                        x = Integer.parseInt(delovi[3].trim());
                        y = Integer.parseInt(delovi[4].trim());
                        
                    } catch (NumberFormatException nfe) {
                        return greska(row, "x i y moraju biti celi brojevi.");
                    }
                    // osnovna validacija ovde (duplikati i opseg)
                    if (kod.length() != 3) return greska(row, "Kod aerodroma mora imati tačno 3 slova.");
                    if (aerTmp.containsKey(kod)) return greska(row, "Duplikat koda aerodroma: " + kod);
                    if (x < -90 || x > 90 || y < -90 || y > 90) return greska(row, "Koordinate su van opsega [-90,90].");

                    aerTmp.put(kod, new Aerodrom(naziv, kod, x, y));

                } else if (type.equalsIgnoreCase("LET")) {
                    if (delovi.length < 6)
                        return greska(row, "Nedovoljno kolona za LET (očekivano 6: LET,od,ka,hh,mm,trajanje).");
                    String od = delovi[1].trim().toUpperCase();
                    String ka = delovi[2].trim().toUpperCase();
                    int hh, mm, trajanje;
                    try {
                        hh = Integer.parseInt(delovi[3].trim());
                        mm = Integer.parseInt(delovi[4].trim());
                        trajanje = Integer.parseInt(delovi[5].trim());
                    } catch (NumberFormatException nfe) {
                        return greska(row, "hh, mm i trajanje moraju biti celi brojevi.");
                    }
                    if (od.equals(ka)) return greska(row, "Polazni i odredišni aerodrom ne mogu biti isti.");
                    if (hh < 0 || hh > 23 || mm < 0 || mm > 59) return greska(row, "Vreme mora biti HH:mm (0–23, 0–59).");
                    if (trajanje <= 0) return greska(row, "Trajanje mora biti > 0.");

                    // Napomena: aerodromi možda nisu još pročitani; validiraćemo posle
                    letTmp.add(new Let(od, ka, hh, mm, trajanje));

                } else {
                    return greska(row, "Nepoznat TYPE: " + type + " (dozvoljeno: AERODROM ili LET).");
                }
            }

            // sekundarna validacija LET-ova: moraju postojati u aerTmp
            for (Let l : letTmp) {
                if (!aerTmp.containsKey(l.od))
                    return "U CSV je nađen let sa polaznim aerodromom (" + l.od + ") koji nije definisan.";
                if (!aerTmp.containsKey(l.ka))
                    return "U CSV je nađen let sa odredišnim aerodromom (" + l.ka + ") koji nije definisan.";
            }

            // Ako je sve OK—prebaci u model, jednom obavesti UI
            model.clear(); // briše stari sadržaj i (obično) pošalje 1 notify
            for (Aerodrom a : aerTmp.values()) model.addAerodromSilent(a);
            for (Let l : letTmp) model.addLetSilent(l);
            // Obavezno obavesti UI posle silent dodavanja:
            
            model.fireChange();
            return null;

        } catch (FileNotFoundException e) {
            return "Fajl ne postoji ili nije dostupan.";
        } catch (IOException e) {
            return "Greška pri čitanju fajla: " + e.getMessage();
        } catch (Exception e) {
            return "Neočekivana greška pri učitavanju: " + e.getMessage();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                // nećemo prebrisati prethodnu poruku – ovo eventualno prijavi u konzoli
                System.err.println("Greška pri zatvaranju fajla: " + e.getMessage());
            }
        }
    }

    private static String greska(int row, String msg) {
        return "Greška u redu " + row + ": " + msg + "\nProverite format CSV fajla.";
    }
}

/*
 
 Button b = new Button("Klikni me!");
b.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        System.out.println("Kliknuto!");
    }
});
 public static void dodajAkciju(AbstractButton komponenta, String poruka) {
    komponenta.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println(poruka);
        }
    });
    
}
JButton dugme = new JButton("Snimi");
dodajAkciju(dugme, "Snimi kliknut!");

JToggleButton tgl = new JToggleButton("Uključi");
dodajAkciju(tgl, "Taster uključen!");
import java.awt.*;
import java.awt.event.*;

public class PrimerKeyListener {
    public static void main(String[] args) {
        Frame f = new Frame("Test tastature");
        f.setSize(400, 300);

        f.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                System.out.println("Tast tipovan: " + e.getKeyChar());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("Pritisnut kod: " + e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println("Pustio si: " + e.getKeyChar());
            }
        });

        f.setVisible(true);
        f.setFocusable(true);
    }
}


 */
