package cc.co.evenprime.bukkit.nocheat.debug;

import cc.co.evenprime.bukkit.nocheat.NoCheat;

public class LagMeasureTask implements Runnable {

    private int           ingameseconds            = 1;
    private long          lastIngamesecondTime     = System.currentTimeMillis();
    private long          lastIngamesecondDuration = 2000L;
    private boolean       skipCheck                = false;
    private int           lagMeasureTaskId         = -1;

    private final NoCheat plugin;

    public LagMeasureTask(NoCheat plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // start measuring with a delay of 10 seconds
        lagMeasureTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 20, 20);
    }

    public void run() {

        try {
            boolean oldStatus = skipCheck;
            // If the previous second took to long, skip checks during
            // this second
            skipCheck = lastIngamesecondDuration > 1500;
            
            if(oldStatus != skipCheck && skipCheck) {
                System.out.println("[NoCheat] detected server lag, some checks will not work.");
            }
            else if(oldStatus != skipCheck && !skipCheck) {
                System.out.println("[NoCheat] server lag seems to have stopped, reenabling checks.");
            }

            long time = System.currentTimeMillis();
            lastIngamesecondDuration = time - lastIngamesecondTime;
            if(lastIngamesecondDuration < 1000)
                lastIngamesecondDuration = 1000;
            else if(lastIngamesecondDuration > 3600000) {
                lastIngamesecondDuration = 3600000; // top limit of 1
                                                    // hour per "second"
            }
            lastIngamesecondTime = time;
            ingameseconds++;

            // Check if some data is outdated now and let it be removed
            if(ingameseconds % 62 == 0) {
                plugin.cleanDataMap();
            }
        } catch(Exception e) {
            // Just prevent this thread from dying for whatever reason
        }

    }

    public void cancel() {
        if(lagMeasureTaskId != -1) {
            try {
                plugin.getServer().getScheduler().cancelTask(lagMeasureTaskId);
            } catch(Exception e) {
                System.out.println("NoCheat: Couldn't cancel LagMeasureTask: " + e.getMessage());
            }
            lagMeasureTaskId = -1;
        }
    }

    public int getIngameSeconds() {
        return ingameseconds;
    }

    public long getIngameSecondDuration() {
        return lastIngamesecondDuration;
    }

    public boolean skipCheck() {
        return skipCheck;
    }
}
