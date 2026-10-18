package sim;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

class TajmerNeaktivnosti {
	
	// referenca na glavni prozor
    private final Frame owner;
    // poziva se kad istekne vreme neaktivnosti
    private final Runnable onTimeout;
    // ako vise niti cita/pise u promenjivu promene su odmah vidljive
    private volatile long lastAction = System.currentTimeMillis();
    // true znaci da je demonski tj nece spreciti zatvaranje programa
    private final Timer t = new Timer(true);
    // ako je true, merenje se privremeno preskace
    
    private volatile boolean paused = false;
    
    void setPaused (boolean p) {
    	paused = p;
    	if (p) lastAction = System.currentTimeMillis();
    }
    
    TajmerNeaktivnosti(Frame owner, Runnable onTimeout) {
        this.owner = owner;
        this.onTimeout = onTimeout;
      
        // pokrecem tajmer koji svake sekunde poziva metodu tick()
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { tick(); }
        }, 1000, 1000);
        // prvi poziv za 1s posle toga na svakih 1s zove tick
    }

    void userAction() {
        lastAction = System.currentTimeMillis();
    }

    private void tick() {
    	if(paused) return;
        long idleSec = (System.currentTimeMillis() - lastAction) / 1000;
        if (idleSec == 55) showCountdownDialog(5);
        if (idleSec >= 60) onTimeout.run();
    }

    private void showCountdownDialog(int seconds) {
    	
        Dialog d = new Dialog(owner, "Neaktivnost", true);
        d.setLayout(new BorderLayout());
        Label info = new Label("");
        d.add(info, BorderLayout.CENTER);

        Panel south = new Panel();
        Button nastavi = new Button("Nastavi rad");
        south.add(nastavi);
        d.add(south, BorderLayout.SOUTH);

        d.setSize(320, 160);
        d.setLocationRelativeTo(owner);

        Timer local = new Timer();
        final int[] left = { seconds };
        info.setText("Program će se zatvoriti za " + left[0] + " s ako ne nastavite.");

        local.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                left[0]--;
                info.setText("Program će se zatvoriti za " + left[0] + " s ako ne nastavite.");
                if (left[0] <= 0) { d.setVisible(false); cancel(); }
            }
        }, 1000, 1000);

        nastavi.addActionListener(e -> {
            userAction();
            d.setVisible(false);
        });

        d.setVisible(true);
        local.cancel();
    }
}
