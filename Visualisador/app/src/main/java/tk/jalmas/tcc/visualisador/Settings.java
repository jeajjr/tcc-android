package tk.jalmas.tcc.visualisador;

/**
 * Created by jeajjr on 29/04/2016.
 */
public class Settings {
    /*********************************
     * Phone -> uC communication
     *********************************/

    private static final char MASK_COMMAND =       0b11100000;
    private static final char MASK_COMMAND_VALUE = 0b00011111;

    /**************************************************
     *                  TRIGGER LEVEL
     *************************************************/
    private static final char SET_TRIGGER_OFF =  0b10000000;
    private static final char SET_TRIGGER_RISE = 0b10100000;
    private static final char SET_TRIGGER_FALL = 0b11000000;

    private static final char TRIGGER_LEVEL_0 =   0b00000000;
    private static final char TRIGGER_LEVEL_100 = 0b00011111;

    public enum TRIGGER_STATES {OFF, RISE, FALL}
    private static TRIGGER_STATES currentTriggerState = TRIGGER_STATES.OFF;

    private static char currentTriggerValuePercent = 50;

    private static char TRIGGER_STEP_PERCENT = 5;

    public static char composeTriggerCommand() {
        switch (currentTriggerState) {
            case OFF:
                return (char) ((MASK_COMMAND & SET_TRIGGER_OFF) & 0xFF);
            case RISE:
                return (char) (((MASK_COMMAND & SET_TRIGGER_RISE) | (MASK_COMMAND_VALUE & getTriggerValue())) & 0xFF);
            case FALL:
                return (char) (((MASK_COMMAND & SET_TRIGGER_FALL) | (MASK_COMMAND_VALUE & getTriggerValue())) & 0xFF);
            default:
                return 0;
        }
    }

    public static void toggleTriggerState() {
        switch (currentTriggerState) {
            case OFF:
                Settings.currentTriggerState = TRIGGER_STATES.RISE;
                break;
            case RISE:
                Settings.currentTriggerState = TRIGGER_STATES.FALL;
                break;
            case FALL:
                Settings.currentTriggerState = TRIGGER_STATES.OFF;
                break;
        }
    }

    public static TRIGGER_STATES getTriggerState() {
        return Settings.currentTriggerState;
    }

    public static char getTriggerValuePercent() {
        return currentTriggerValuePercent;
    }

    private static char getTriggerValue() {
        if ((currentTriggerValuePercent * voltageScaleMax / 100f) > deviceMaxVoltage)
            return TRIGGER_LEVEL_0;

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
        return (char) (((MASK_COMMAND & SET_HOLD_OFF) | (MASK_COMMAND_VALUE & currentHoldOff)) & 0xFF);
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

    private static final char TIME_SCALE_10US =     0b00000000;
    private static final char TIME_SCALE_20US =     0b00000001;
    private static final char TIME_SCALE_50US =     0b00000010;
    private static final char TIME_SCALE_100US =    0b00000011;
    private static final char TIME_SCALE_200US =    0b00000100;
    private static final char TIME_SCALE_500US =    0b00000101;
    private static final char TIME_SCALE_1MS =      0b00000110;
    private static final char TIME_SCALE_2MS =      0b00000111;
    private static final char TIME_SCALE_5MS =      0b00001000;
    private static final char TIME_SCALE_10MS =     0b00001001;
    private static final char TIME_SCALE_20MS =     0b00001010;
    private static final char TIME_SCALE_50MS =     0b00001011;
    private static final char TIME_SCALE_100MS =    0b00001100;
    private static final char TIME_SCALE_200MS =    0b00001101;
    private static final char TIME_SCALE_500MS =    0b00001110;
    private static final char TIME_SCALE_1S =       0b00001111;
    private static final char TIME_SCALE_2S =       0b00010000;

    private static final Float[] TIME_SCALE_LABELS_VALUE =
            {1f, 2.5f, 5f, 10f, 25f, 50f, 100f, 250f, 500f, 1f,
                    2.5f, 5f, 10f, 25f, 50f, 100f, 250f};

    private static final String[] TIME_SCALE_LABELS_UNIT =
            {"us", "us", "us", "us", "us", "us", "us", "us", "us", "ms",
                    "ms", "ms", "ms", "ms", "ms", "ms", "ms"};

    private static final int[] CONTINUOUS_MODE_BLOCK_SIZE =
        {2, 2, 2, 2, 2, 5, 10, 20, 50, 100, 200, 500, 500, 500, 500, 500, 500};

    private static char currentTimeScale = TIME_SCALE_500MS;


    public static int getCurrentBlockSize() {
        return CONTINUOUS_MODE_BLOCK_SIZE[currentTimeScale];
    }

    public static char composeTimeScaleCommand() {
        return (char) (((MASK_COMMAND & SET_TIME_SCALE) | (MASK_COMMAND_VALUE & currentTimeScale)) & 0xFF);
    }

    public static String getCurrentTimeScaleCompleteLabel() {
        return TIME_SCALE_LABELS_VALUE[currentTimeScale] + TIME_SCALE_LABELS_UNIT[currentTimeScale];
    }

    public static Float getCurrentTimeScaleLabelValue() {
        return TIME_SCALE_LABELS_VALUE[currentTimeScale];
    }

    public static String getCurrentTimeScaleLabelUnit() {
        return TIME_SCALE_LABELS_UNIT[currentTimeScale];
    }

    public static void increaseTimeScale() {
        if (currentTimeScale != TIME_SCALE_2S)
            currentTimeScale++;
    }

    public static void decreaseTimeScale() {
        if (currentTimeScale != TIME_SCALE_1MS)
            currentTimeScale--;
    }

    public static String getMinTimeScaleLabel() {
        return TIME_SCALE_LABELS_VALUE[TIME_SCALE_1MS] + TIME_SCALE_LABELS_UNIT[TIME_SCALE_1MS];
    }

    public static String getMaxTimeScaleLabel() {
        return TIME_SCALE_LABELS_VALUE[TIME_SCALE_2S] + TIME_SCALE_LABELS_UNIT[TIME_SCALE_2S];
    }


    /**************************************************
     *              TIME SCALE OFFSET
     *************************************************/

    private static final String[] TIME_OFFSET_LABELS = {"-50%", "-45%", "-40%", "-35%", "-30%", "-25%", "-20%",
            "-15%", "-10%", "-5%", "0%", "+5%", "+10%", "+15%", "+20%", "+25%", "+30%", "+35%", "+40%", "+45%", "+50%"};

    private static final float[] TIME_OFFSET_FACTOR = {-0.50f, -0.45f, -0.40f, -0.35f, -0.30f, -0.25f, -0.20f, -0.15f,
            -0.10f, -0.05f, +0.00f, +0.05f, +0.10f, +0.15f, +0.20f, +0.25f, +0.30f, +0.35f, +0.40f, +0.45f, +0.50f};

    private static final int TIME_OFFSET_MIN = 0;
    private static final int TIME_OFFSET_MAX = TIME_OFFSET_LABELS.length - 1;

    private static int currentTimeOffset = (TIME_OFFSET_MIN + TIME_OFFSET_MAX)/2;

    public static String getCurrentTimeOffsetLabel() {
        return TIME_OFFSET_LABELS[currentTimeOffset];
    }

    public static float getCurrentTimeOffsetFactor() {
        return TIME_OFFSET_FACTOR[currentTimeOffset];
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

    public static final float deviceMaxVoltage = 3.33f;


    /*********************************
     * uC -> Phone communication
     *********************************/
    private static final char DATA = 0x20;
    private static final char CHANNEL_1 = 0x01;
    private static final char CHANNEL_2 = 0x02;
}
