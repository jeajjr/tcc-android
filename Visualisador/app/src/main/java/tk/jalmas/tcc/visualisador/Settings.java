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
    private static char currentTriggerValuePercent = 50;

    private static char TRIGGER_STEP_PERCENT = 5;

    public static char composeTriggerCommand() {
        if (isTriggerEnabled)
            return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TRIGGER_LEVEL) | (MASK_COMMAND_VALUE & getTriggerValue())) & 0xFF);
        else
            return (char) (((MASK_COMMAND & COMMAND) | (MASK_SUB_COMMAND & SET_TRIGGER_LEVEL) | (MASK_COMMAND_VALUE & TRIGGER_LEVEL_OFF)) & 0xFF);
    }

    public static void setIsTriggerEnabled (boolean isTriggerEnabled) {
        Settings.isTriggerEnabled = isTriggerEnabled;
    }

    public static boolean isTriggerEnabled() {
        return Settings.isTriggerEnabled;
    }

    public static char getTriggerValuePercent() {
        return currentTriggerValuePercent;
    }

    private static char getTriggerValue() {
        if ((currentTriggerValuePercent * voltageScaleMax / 100f) > deviceMaxVoltage)
            return TRIGGER_LEVEL_OFF;

        return (char) (TRIGGER_LEVEL_100 * currentTriggerValuePercent * voltageScaleMax / (100f * deviceMaxVoltage));
    }

    public static void increaseTriggerValue() {
        if (currentTriggerValuePercent + TRIGGER_STEP_PERCENT <= 100)
            currentTriggerValuePercent += TRIGGER_STEP_PERCENT;
    }

    public static void decreaseTriggerValue() {
        if (currentTriggerValuePercent - TRIGGER_STEP_PERCENT >= 0)
            currentTriggerValuePercent -= TRIGGER_STEP_PERCENT;
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
    private static final String[] TIME_SCALE_LABELS = {"1us", "5us", "10us", "20us", "50us",
            "100us", "500us", "1ms", "5ms", "10ms", "20ms", "50ms", "100ms"};

    private static char currentTimeScale = TIME_SCALE_500MS;


    public static char composeTimeScaleCommand() {
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

    /**************************************************
     *                  VOLTAGE SCALE
     *************************************************/
    public static final float voltageScaleMax = 4.0f;

    public static final float deviceMaxVoltage = 3.0f;


    /*********************************
     * uC -> Phone communication
     *********************************/
    private static final char DATA = 0x20;
    private static final char CHANNEL_1 = 0x01;
    private static final char CHANNEL_2 = 0x02;
}
