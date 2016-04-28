package tk.jalmas.tcc.visualisador;

/**
 * Created by jeajjr on 27/04/2016.
 */
public interface Updater {
    void onUpdate(int[] data);
    void onUpdate(int[] data, int lastPosition);
}
