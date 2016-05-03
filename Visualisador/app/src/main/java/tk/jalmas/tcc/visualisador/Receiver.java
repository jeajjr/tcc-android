package tk.jalmas.tcc.visualisador;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jeajjr on 27/04/2016.
 */
public class Receiver extends Thread {
    private enum STATES {CONTINUOUS, BULK_IN, BULK_OUT};
    private STATES currentState;

    private final int[] SUB_ARRAY_OK = {'O', 'K'};
    private final int SUB_ARRAY_LENGTH_OK = SUB_ARRAY_OK.length;
    private final int[] SUB_ARRAY_USP_BK = {'U', 'S', 'P', 'B', 'K'};
    private final int SUB_ARRAY_LENGTH_USP_BK = SUB_ARRAY_USP_BK.length;
    private final int[] SUB_ARRAY_USP_OK = {'U', 'S', 'P', 'O', 'K'};
    private final int SUB_ARRAY_LENGTH_USP_OK = SUB_ARRAY_USP_OK.length;

    private int[] buffer;
    private int buffIndex;
    private int currentMessageStart;

    private final int BUFFER_SIZE = 1500;
    private final int CONTINUOUS_BLOCK_SIZE = 500;

    // index where the first byte of the message length is
    private final int LENGTH_START_INDEX =  3;
    // number of bytes on the bulk message that are not data bytes
    private final int NON_DATA_MESSAGE_BYTES = 10;
    // control for the BULK_IN state of the current message size, to monitor for message size violation/overflow
    private int bulkMsgLen = 0;
    private boolean isBulkMsgLenSet = false;

    /* number of most recent samples to not send on continuous mode update. This servers as a gap to remove the command
     * to change to continuous mode from the buffer.
     */
    private final int CONTINUOUS_MODE_PRINT_OFFSET = 10;


    private Updater updater;

    BluetoothSocket btSocket;
    private DataInputStream dataIS;
    private DataOutputStream dataOS;

    private UUID myUUID;

    private Activity context;

    private static final String DEVICE_NAME = "TCC";

    public Receiver (Activity context) {
        this.context = context;
        //myUUID = UUID.fromString(((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
        myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        btSocket = null;

        if (BUFFER_SIZE % CONTINUOUS_BLOCK_SIZE != 0) {
            System.out.println("Error: BUFFER_SIZE must be a multiple of CONTINUOUS_BLOCK_SIZE");
            System.exit(1);
        }
        buffer = new int[BUFFER_SIZE];
        for (int i=0; i<BUFFER_SIZE; i++)
            buffer[i] = 0;
        buffIndex = 0;
        currentMessageStart = 0;
        currentState = STATES.BULK_OUT;
    }

    public void setUpdater(Updater updater) {
        this.updater = updater;
    }
    public void run() {
        try {
            BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> btDevices = myBluetooth.getBondedDevices();
            BluetoothDevice dispositivo = null;

            System.out.println("Listing devices:");
            for (BluetoothDevice dev : btDevices) {
                System.out.println("BT : " + dev.getName() + "," + dev.getAddress());

                if (dev.getName().compareTo(DEVICE_NAME) == 0) {
                    dispositivo = dev;
                    break;
                }
            }

            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            btSocket.connect();

            dataIS = new DataInputStream(btSocket.getInputStream());
            dataOS = new DataOutputStream(btSocket.getOutputStream());

            while (true) {
                buffer[buffIndex] = readCharacters();

                checkBuffer();

                buffIndex = incrementIndex(buffIndex);
            }
        }
        catch (IOException e) {
            e.printStackTrace();

            cleanup();
        }
    }

    public boolean sendCommand(char a) {
        if (dataOS == null)
            return false;

        try {
            System.out.println("command " + (int) a);
            dataOS.writeChar(a);
        }
        catch (IOException e) {
            //e.printStackTrace();
            return false;
        }

        return true;
    }

    private char readCharacters() throws IOException{
        return (char) (dataIS.readByte() & 0xFF);
    }


    private int getBulkMessageCurrentIndex() {
        return decrementIndex(buffIndex, currentMessageStart);
    }
    private void checkBuffer(){
        long start = System.nanoTime();
        //System.out.println(String.format("checkBuffer: buffIndex(%d), currentMessageStart(%d)", buffIndex, currentMessageStart));

        switch (currentState) {
            case CONTINUOUS:
                if (updater != null && (buffIndex%25 == 0))
                    updater.onUpdate(
                            getSubArray(
                                    buffer,
                                    (buffIndex/CONTINUOUS_BLOCK_SIZE) * CONTINUOUS_BLOCK_SIZE + CONTINUOUS_BLOCK_SIZE,
                                    CONTINUOUS_BLOCK_SIZE),
                            buffIndex % CONTINUOUS_BLOCK_SIZE);
                //buffer, buffIndex);
                //getSubArray(buffer, decrementIndex(buffIndex, CONTINUOUS_MODE_PRINT_OFFSET), CONTINUOUS_BLOCK_SIZE));

                if (buffIndex % CONTINUOUS_BLOCK_SIZE == CONTINUOUS_BLOCK_SIZE - 1)
                    for (int i=0; i <CONTINUOUS_BLOCK_SIZE; i++) {
                        int destinationIndex = incrementIndex(buffIndex, i + 1);
                        buffer[destinationIndex] = buffer[decrementIndex(destinationIndex, CONTINUOUS_BLOCK_SIZE)];
                    }
                // Detect command to change to continuous mode and erase it from buffer
                if (isSubArrayEqual(getSubArray(buffer, buffIndex, SUB_ARRAY_LENGTH_USP_OK), SUB_ARRAY_USP_OK)) {
                    buffIndex = decrementIndex(buffIndex, SUB_ARRAY_LENGTH_USP_OK);
                }

                // State change condition
                if (isSubArrayEqual(getSubArray(buffer, buffIndex, SUB_ARRAY_LENGTH_USP_BK), SUB_ARRAY_USP_BK)) {
                    changeCurrentState(STATES.BULK_IN);
                    System.out.println("Detected change to BULK mode");
                }

                break;
            case BULK_IN:
                if (getBulkMessageCurrentIndex() == LENGTH_START_INDEX + 1){
                    bulkMsgLen = buffer[decrementIndex(buffIndex)] * 256 + buffer[buffIndex];
                    isBulkMsgLenSet = true;
                }

                // State change conditions
                if (isBulkMsgLenSet)
                    if (getMessageSize() > bulkMsgLen + NON_DATA_MESSAGE_BYTES) {
                        System.out.println("Detected message overflow");
                        changeCurrentState(STATES.BULK_OUT);
                    }

                if (isSubArrayEqual(getSubArray(buffer, buffIndex, SUB_ARRAY_LENGTH_OK), SUB_ARRAY_OK)) {
                    System.out.println("Detected end of message");
                    changeCurrentState(STATES.BULK_OUT);
                    if (updater != null)
                        updater.onUpdate(getSubArray(buffer, buffIndex, getMessageSize()));
                }

                break;
            case BULK_OUT:
                // State change condition
                if (isSubArrayEqual(getSubArray(buffer, buffIndex, SUB_ARRAY_LENGTH_USP_BK), SUB_ARRAY_USP_BK)) {
                    System.out.println("Detected start of message");
                    changeCurrentState(STATES.BULK_IN);

                }
                break;
        }

        // State change condition
        if (currentState == STATES.BULK_IN || currentState == STATES.BULK_OUT) {
            if (isSubArrayEqual(getSubArray(buffer, buffIndex, SUB_ARRAY_LENGTH_USP_OK), SUB_ARRAY_USP_OK)) {
                System.out.println("Detected change to CONTINUOUS mode");
                changeCurrentState(STATES.CONTINUOUS);
            }
        }

        long end = System.nanoTime();
        //System.out.println((end - start)/1000 + " us");
    }

    private void changeCurrentState(STATES newState) {
        switch (newState) {
            case BULK_IN:
                currentState = STATES.BULK_IN;

                isBulkMsgLenSet = false;
                currentMessageStart = incrementIndex(buffIndex);
                break;
            case BULK_OUT:
                currentState = STATES.BULK_OUT;
                break;
            case CONTINUOUS:
                if (currentState != STATES.CONTINUOUS)
                    for (int i=0; i<BUFFER_SIZE; i++)
                        buffer[i] = 0;

                currentState = STATES.CONTINUOUS;
                break;
        }
    }

    private int getMessageSize() {
        if (buffIndex > currentMessageStart)
            return buffIndex - currentMessageStart + 1;
        else
            return (BUFFER_SIZE - currentMessageStart) + buffIndex + 1;
    }

    private boolean isSubArrayEqual(int[] arrayA, int[] arrayB) {
        if (arrayA.length != arrayB.length)
            return false;

        for (int i=0; i<arrayA.length; i++)
            if (arrayA[i] != arrayB[i])
                return false;

        return true;
    }

    private int[] getSubArray(int[] array, int currentIndex, int size) {
        if (size <= 0)
            return null;

        int[] result = new int[size];

        int index = decrementIndex(currentIndex, size);
        index = incrementIndex(index);

        for (int i=0; i<size; i++) {
            result[i] = array[index];
            index = incrementIndex(index);
        }

        return result;
    }

    private int incrementIndex(int index) {
        if (++index == BUFFER_SIZE)
            return 0;
        return index;
    }

    private int incrementIndex(int index, int addend) {
        index += addend;
        if (index >= BUFFER_SIZE)
            return index - BUFFER_SIZE;
        return index;
    }

    private int decrementIndex(int index) {
        if (--index == -1)
            return BUFFER_SIZE - 1;
        return index;
    }

    private int decrementIndex(int index, int subtrahend) {
        index -= subtrahend;
        if (index < 0)
            return BUFFER_SIZE + index;
        return index;
    }

    public void cleanup() {
        try {
            if (dataIS != null)
                dataIS.close();
        }
        catch (IOException e1) {}

        try {
            if (dataOS != null)
                dataOS.close();
        }
        catch (IOException e1) {}

        try {
            if (btSocket != null)
                btSocket.close();
        }
        catch (IOException e1) {}
    }
}
