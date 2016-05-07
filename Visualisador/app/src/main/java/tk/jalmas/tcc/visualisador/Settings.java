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

    /**************************************************
     *                  TRIGGER LEVEL
     *************************************************/
    private static final char SET_TRIGGER_LEVEL = 0b00000000;

    private static final char TRIGGER_LEVEL_0 = 0b00000000;
    private static final char TRIGGER_LEVEL_100 = 0b00011110;
    private static final char TRIGGER_LEVEL_OFF = 0b00011111;

    private static boolean isTriggerEnabled = false;
    private static char currentTriggerValue = (TRIGGER_LEVEL_0 + TRIGGER_LEVEL_100)/2;

    public static char composeTriggerCommand() {
        if (isTriggerEnabled)
            return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TRIGGER_LEVEL) | (MASK_COMMAND_VALUE & currentTriggerValue)) & 0xFF);
        else
            return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TRIGGER_LEVEL) | (MASK_COMMAND_VALUE & TRIGGER_LEVEL_OFF)) & 0xFF);
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


    /**************************************************
     *              HOLD OFF LENGTH
     *************************************************/
    private static final char SET_HOLD_OFF = 0b00100000;

    private static final char HOLD_OFF_MIN = 0b00000001;
    private static final char HOLD_OFF_MAX = 0b00000111;

    private static char currentHoldOff = HOLD_OFF_MIN;

    private static final String[] HOLD_OFF_LABELS = {"n/a", "1/8", "2/8", "3/8", "4/8", "5/8", "6/8", "7/8"};

    public static char composeHoldOffCommand() {
        System.out.println("composeHoldOffCommand " + HOLD_OFF_LABELS[currentHoldOff]);
        return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_HOLD_OFF) | (MASK_COMMAND_VALUE & currentHoldOff)) & 0xFF);
    }

    public static String getCurrentHoldOffLabel() {
        return HOLD_OFF_LABELS[currentHoldOff];
    }

    public static void increaseHoldOff() {
        if (currentHoldOff != HOLD_OFF_MAX)
            currentHoldOff++;
    }

    public static void decreaseHoldOff() {
        if (currentHoldOff != HOLD_OFF_MIN)
            currentHoldOff--;
    }

    public static String getMinHoldOffLabel() {
        return HOLD_OFF_LABELS[HOLD_OFF_MIN];
    }

    public static String getMaxHoldOffLabel() {
        return HOLD_OFF_LABELS[HOLD_OFF_MAX];
    }

    /**************************************************
     *                  TIME SCALE
     *************************************************/
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


    public static char composeTimeScaleCommand() {
        System.out.println("composeTimeScaleCommand " + TIME_SCALE_LABELS[currentTimeScale]);
        return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TIME_SCALE) | (MASK_COMMAND_VALUE & currentTimeScale)) & 0xFF);
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

    public static String getMinTimeScaleLabel() {
        return TIME_SCALE_LABELS[TIME_SCALE_10US];
    }

    public static String getMaxTimeScaleLabel() {
        return TIME_SCALE_LABELS[TIME_SCALE_1S];
    }


    /**************************************************
     *              TIME SCALE OFFSET
     *************************************************/
    private static final char SET_TIME_OFFSET = 0b01100000;

    private static final char TIME_OFFSET_MIN = 0b00000000;
    private static final char TIME_OFFSET_MAX = 0b00001010;

    private static char currentTimeOffset = (TIME_OFFSET_MIN + TIME_OFFSET_MAX)/2;

    private static final String[] TIME_OFFSET_LABELS = {"-50%", "-40%", "-30%", "-20%", "-10%", "0%",
            "+10%", "+20%", "+30%", "+40%", "+50%"};

    public static char composeTimeOffsetCommand() {
        System.out.println("composeTimeOffsetCommand " + TIME_OFFSET_LABELS[currentTimeOffset]);
        return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TIME_OFFSET) | (MASK_COMMAND_VALUE & currentTimeOffset)) & 0xFF);
    }

    public static String getCurrentTimeOffsetLabel() {
        return TIME_OFFSET_LABELS[currentTimeOffset];
    }

    public static void increaseTimeOffset() {
        if (currentTimeOffset != TIME_OFFSET_MAX)
            currentTimeOffset++;
    }

    public static void decreaseTimeOffset() {
        if (currentTimeOffset != TIME_OFFSET_MIN)
            currentTimeOffset--;
    }

    public static String getMinTimeOffsetLabel() {
        return TIME_OFFSET_LABELS[TIME_OFFSET_MIN];
    }

    public static String getMaxTimeOffsetLabel() {
        return TIME_OFFSET_LABELS[TIME_OFFSET_MAX];
    }


    /*********************************
     * uC -> Phone communication
     *********************************/
    private static final char DATA = 0x20;
    private static final char CHANNEL_1 = 0x01;
    private static final char CHANNEL_2 = 0x02;
}
