package tk.ardentbot.utils.updaters;

public class WebsiteDaemon implements Runnable {
    @Override
    public void run() {
        try {
            Runtime.getRuntime().exec("(cd /root/Ardent/web/ && exec npm start)");
        }
        catch (Exception ex) {
        }
    }
}
