# Air Traffic Simulation

A Java AWT desktop application for managing airports and scheduled flights and displaying animated aircraft on a 2D map.

## Features

- Airport and flight entry with validation.
- Airport selection, filtering and visibility controls.
- Start, pause and reset controls with a simulated clock.
- A simulation worker that updates flight positions every 200 ms.
- CSV loading and saving.
- Observer-based model notifications and an inactivity timer.

## Build and run

Requires a JDK and a graphical desktop. No external Java dependencies are used.

```sh
javac -encoding UTF-8 -d out src/sim/*.java
java -cp out sim.Simulacija
```

In the application, choose **Fajl > Ucitaj CSV...** and open `examples/flights.csv`, then press **Start**. Menu labels may contain Serbian diacritics.

## CSV format

```text
AERODROM,BEG,Belgrade,0,0
AERODROM,NIS,Nis,50,-30
LET,BEG,NIS,0,10,60
```

Airport rows contain code, name, x and y. Flight rows contain departure airport, destination airport, hour, minute and duration in minutes. Airport codes must contain 3 characters; coordinates must be in [-90, 90]. Names containing commas and quoted CSV fields are not supported.

## Structure

- `Simulacija`: application window and controls.
- `Model`, `Aerodrom`, `Let`: domain data and observer notifications.
- `Mapa`: map rendering and interaction.
- `SimulacijaLeta`: worker thread and aircraft position updates.
- `CsvStorage`: CSV persistence.
- `Panel*`, `ListaPodatakaPanel`: entry, filtering and list views.
- `TajmerNekativnosti.java`: inactivity timer (original filename retained).

## Version and review

This package preserves the source from `vs230340oop2proj.zip`. Compilation passed with JDK 25. Model validation and a CSV round trip were checked separately; full GUI behavior was not tested during packaging.

This supplied version implements **CSV**, not JSON persistence. It should not be described as a JSON/CSV implementation unless a different source version is provided. Observer-style notifications are visible in the code; Factory/Strategy claims are not established by this archive.

The code is a complete course application, with remaining polish issues: the Start button has two listeners, some timer callbacks update AWT components outside the event-dispatch thread, and closing the window does not explicitly stop the simulation worker. These findings are documented rather than silently redesigning the submitted application.
