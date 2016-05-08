package tk.jalmas.tcc.visualisador;

/**
 * Created by jeajjr on 03/05/2016.
 */
public interface StateListener {
    void onCharacterReceived();
    void onBluetoothStateChanged(boolean connected);
    void onErrorOccurred();
}
