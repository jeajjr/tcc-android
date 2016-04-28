package tk.jalmas.tcc.visualisador;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jeajjr on 27/04/2016.
 */
public class Receiver extends Thread {
    private Updater updater;
    private DataInputStream dataIS;

    private UUID myUUID;

    private Activity context;

    private static final String DEVICE_NAME = "TCC";

    public Receiver (Activity context) {
        this.context = context;
        //myUUID = UUID.fromString(((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
        myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    public void setUpdater(Updater updater) {
        this.updater = updater;
    }
    public void run() {
        BluetoothSocket btSocket = null;

        if (updater != null)
            updater.onUpdate(null, 66);

        try {
            BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();

            Set<BluetoothDevice> btDevices = myBluetooth.getBondedDevices();

            BluetoothDevice dispositivo = null;
            boolean gotit = false;

                    System.out.println("Listing devices");
            for (BluetoothDevice dev : btDevices) {
                System.out.println("BT : " + dev.getName() + "," + dev.getAddress());

                if (dev.getName().compareTo(DEVICE_NAME) == 0) {
                    dispositivo = dev;
                    break;
                }
            }

            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            btSocket.connect();//start connection
            InputStream in = btSocket.getInputStream();
            dataIS = new DataInputStream(in);

            while (true) {
                char data = readCharacters();

                if (updater != null)
                    updater.onUpdate(null, data);
            }
        }
        catch (IOException e) {
            e.printStackTrace();

            try {
                dataIS.close();
            }
            catch (IOException e1) {}

            try {
            if (btSocket != null)
                btSocket.close();
            }
            catch (IOException e1) {}

        }
    }

    private char readCharacters() throws IOException{
        return (char) (dataIS.readByte() & 0xFF);
    }
}
