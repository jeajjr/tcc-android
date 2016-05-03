package tk.jalmas.tcc.visualisador;

/**
 * Created by jeajjr on 29/04/2016.
 */
public class Settings {
    /*********************************
     * Phone -> uC communication
     *********************************/

    private static final char MASK_COMMAND = 0b10000000;
    private static final char MASK_SUB_COMMAND = 0b01100000;
    private static final char MASK_COMMAND_VALUE = 0b00011111;

    private static final char COMMAND = 0b10000000;

    // TRIGGER LEVEL
    private static final char SET_TRIGGER_LEVEL = 0b00000000;

    private static final char TRIGGER_LEVEL_0 = 0b00000000;
    private static final char TRIGGER_LEVEL_100 = 0b00011110;
    private static final char TRIGGER_LEVEL_OFF = 0b00011111;

    private static boolean isTriggerEnabled = false;
    private static char currentTriggerValue = (TRIGGER_LEVEL_0 + TRIGGER_LEVEL_100)/2;

    // TIME SCALE
    private static final char SET_TIME_SCALE = 0b01000000;

    private static final char TIME_SCALE_10US = 0b00000000;
    private static final char TIME_SCALE_50US = 0b00000001;
    private static final char TIME_SCALE_100US = 0b00000010;
    private static final char TIME_SCALE_200US = 0b00000011;
    private static final char TIME_SCALE_500US = 0b00000100;
    private static final char TIME_SCALE_1MS = 0b00000101;
    private static final char TIME_SCALE_5MS = 0b00000110;
    private static final char TIME_SCALE_10MS = 0b00000111;
    private static final char TIME_SCALE_50MS = 0b00001000;
    private static final char TIME_SCALE_100MS = 0b00001001;
    private static final char TIME_SCALE_200MS = 0b00001010;
    private static final char TIME_SCALE_500MS = 0b00001011;
    private static final char TIME_SCALE_1S = 0b00001100;
    private static final String[] TIME_SCALE_LABELS = {"10us", "50us", "100us", "200us", "500us",
            "1ms", "5ms", "10ms", "50ms", "100ms", "200ms", "500ms", "1s"};

    private static char currentTimeScale = TIME_SCALE_500MS;
    private static char LAST_TIME_SCALE_CONT = Settings.TIME_SCALE_1S;
    private static char LAST_TIME_SCALE_BULK = Settings.TIME_SCALE_10MS;

    // number of samples in a time frame (osciloscope screen)
    private static final char HOLD_OFF_START_VALUE = 0;

    /*********************************
     * uC -> Phone communication
     *********************************/
    private static final char DATA = 0x20;
    private static final char CHANNEL_1 = 0x01;
    private static final char CHANNEL_2 = 0x02;

    public static char composeTimeScaleCommand() {
        System.out.println("composeTimeScaleCommand " + TIME_SCALE_LABELS[currentTimeScale]);
        return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TIME_SCALE) | (MASK_COMMAND_VALUE & currentTimeScale)) & 0xFF);
    }

    public static char composeTriggerCommand() {
        if (isTriggerEnabled)
            return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TRIGGER_LEVEL) | (MASK_COMMAND_VALUE & currentTriggerValue)) & 0xFF);
        else
            return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TRIGGER_LEVEL) | (MASK_COMMAND_VALUE & TRIGGER_LEVEL_OFF)) & 0xFF);
    }

    public static char getSetLastTimeScaleBulkCommand() {
        System.out.println("getSetLastTimeScaleBulkCommand " + TIME_SCALE_LABELS[LAST_TIME_SCALE_BULK]);

        currentTimeScale = LAST_TIME_SCALE_BULK;

        return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TIME_SCALE) | (MASK_COMMAND_VALUE & LAST_TIME_SCALE_BULK)) & 0xFF);
    }

    public static char getSetLastTimeScaleContinuousCommand() {
        System.out.println("getSetLastTimeScaleContinuousCommand  " + TIME_SCALE_LABELS[LAST_TIME_SCALE_CONT]);

        currentTimeScale = LAST_TIME_SCALE_CONT;

        return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TIME_SCALE) | (MASK_COMMAND_VALUE & LAST_TIME_SCALE_CONT)) & 0xFF);
    }

    public static void setCurrentTimeScaleAsLastBulk() {
        LAST_TIME_SCALE_BULK = currentTimeScale;
        System.out.println("LAST_TIME_SCALE_BULK set as " + TIME_SCALE_LABELS[LAST_TIME_SCALE_BULK]);
    }

    public static void setCurrentTimeScaleAsLastContinuous() {
        LAST_TIME_SCALE_CONT = currentTimeScale;
        System.out.println("LAST_TIME_SCALE_BULK set as " + TIME_SCALE_LABELS[LAST_TIME_SCALE_CONT]);
    }

    public static String getCurrentTimeScaleLabel() {
        return TIME_SCALE_LABELS[currentTimeScale];
    }

    public static void increaseTimeScale() {
        if (currentTimeScale != TIME_SCALE_1S)
            currentTimeScale++;
    }

    public static void decreaseTimeScale() {
        if (currentTimeScale != TIME_SCALE_10US)
            currentTimeScale--;
    }

    public static void setIsTriggerEnabled (boolean isTriggerEnabled) {
        Settings.isTriggerEnabled = isTriggerEnabled;
    }

    public static boolean isTriggerEnabled() {
        return Settings.isTriggerEnabled;
    }

    public static void setTriggerValue (int percentage) {
        Settings.currentTriggerValue = (char) (TRIGGER_LEVEL_100 * percentage /100);
    }
}
