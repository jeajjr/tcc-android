package tk.jalmas.tcc.visualisador;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MySimpleGraph extends View {

    public enum CURSORS_STATES {VOLTAGE, TIME, OFF}
    private CURSORS_STATES currentCursorsState;

    private enum CURSOR_HANDLERS_STATES {NONE_SELECTED, VOLT0_SELECTED, VOLT1_SELECTED, TIME0_SELECTED, TIME1_SELECTED}
    private CURSOR_HANDLERS_STATES currentCursorHandlerState;

    private boolean isGraphPaused;
    private float[] voltageCursorsPosition;         //current position in pixels over screen height
    private float[] voltageCursorsHandlePosition;   //current position in pixels over screen width
    private float[] timeCursorsPosition;            //current position in pixels over screen width
    private float[] timeCursorsHandlePosition;      //current position in pixels over screen height

    private DataPoint[] data;

    private boolean isLastPositionEnabled;
    private int lastPosition;
    private int REFRESH_GAP = (int) (0.05 * Settings.getCurrentBlockSize());

    private char triggerLevelPercent;
    private float maxYValue;

    private Paint axisPaint;
    private final int AXIS_OFFSET = 2;
    private final int AXIS_STROKE = 4;

    private Paint gridPaint;
    private final int GRID_STROKE = 1;
    private final int GRID_X_COUNT = 10;
    private final int GRID_Y_COUNT = 8;

    private Paint pointPaint;
    private final int POINT_RADIUS = 4;
    private final int POINT_STROKE = 2;

    private Paint cursorPaint;
    private final int CURSOR_STROKE = 5;

    private Paint cursorClickedPaint;
    private final int CURSOR_CLIKED_STROKE = 20;

    private Paint cursorHandlePaint;
    private final int CURSOR_HANDLE_RADIUS = 20;
    private final int CURSOR_HANDLE_CLICKED_RADIUS = 100;

    private Paint cursorHandleClickedPaint;
    private final int CURSOR_HANDLE_CLIKED_RADIUS = 150;

    public void init() {
        isLastPositionEnabled = false;
        lastPosition = 0;

        triggerLevelPercent = 0;

        axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(AXIS_STROKE);

        gridPaint = new Paint();
        gridPaint.setColor(Color.BLACK);
        gridPaint.setStrokeWidth(GRID_STROKE);

        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStrokeWidth(POINT_STROKE);

        cursorPaint = new Paint();
        cursorPaint.setColor(Color.BLUE);
        cursorPaint.setStrokeWidth(CURSOR_STROKE);

        cursorClickedPaint = new Paint();
        cursorClickedPaint.setColor(Color.BLUE);
        cursorClickedPaint.setStrokeWidth(CURSOR_CLIKED_STROKE);

        cursorHandlePaint = new Paint();
        cursorHandlePaint.setColor(Color.GRAY);
        cursorHandlePaint.setStrokeWidth(CURSOR_HANDLE_RADIUS);

        cursorHandleClickedPaint = new Paint();
        cursorHandleClickedPaint.setColor(Color.GRAY);
        cursorHandleClickedPaint.setStrokeWidth(CURSOR_HANDLE_CLICKED_RADIUS);

        this.setBackgroundColor(Color.WHITE);

        maxYValue = 0;

        currentCursorsState = CURSORS_STATES.TIME; //TODO CURSORS_STATES.VOLTAGE;
        currentCursorHandlerState = CURSOR_HANDLERS_STATES.NONE_SELECTED;
        isGraphPaused = false; //TODO true;

        voltageCursorsPosition = new float[]{0.1f, 0.6f}; //TODO {0, 0};
        voltageCursorsHandlePosition = new float[]{0.3f, 0.5f};

        timeCursorsPosition = new float[]{0.1f, 0.6f}; //TODO {0, 0};
        timeCursorsHandlePosition = new float[]{0.3f, 0.5f};
    }

    public MySimpleGraph(Context context) {
        super(context);
        init();
    }

    public MySimpleGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySimpleGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MySimpleGraph(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = canvas.getHeight() - AXIS_OFFSET;
        int width = canvas.getWidth() - AXIS_OFFSET;

        // Draw axis
        canvas.drawLine(
                AXIS_OFFSET, AXIS_OFFSET,
                AXIS_OFFSET, height,
                axisPaint);

        canvas.drawLine(
                AXIS_OFFSET, height,
                width, height,
                axisPaint);

        // Draw grid
        for (int i=0; i<=GRID_X_COUNT; i++)
            canvas.drawLine(
                    width * i / GRID_X_COUNT, AXIS_OFFSET,
                    width * i / GRID_X_COUNT, height - AXIS_OFFSET,
                    gridPaint);

        for (int i=0; i<=GRID_Y_COUNT; i++)
            canvas.drawLine(
                    AXIS_OFFSET, height * i / GRID_Y_COUNT,
                    width - AXIS_OFFSET, height * i / GRID_Y_COUNT,
                    gridPaint);

        // Draw trigger line
        int triggerY = (int) (height * triggerLevelPercent / 100.0);
        for (int i=0; i<=width; i+=40)
            canvas.drawLine(
                    i, height - triggerY,
                    i + 20, height - triggerY,
                    axisPaint);

        // Draw points
        if (data != null) {
            float minX = data[0].x;
            float maxX = data[data.length - 1].x;
            float deltaX = (maxX - minX);

            float factorY = (float) height / this.maxYValue;

            int lastX = 0;
            int lastY = 0;

            for (int i = 0; i < data.length; i++) {
                int newX = (int) ((data[i].x - minX) * width / deltaX);
                int newY = (int) ((this.maxYValue - data[i].y) * factorY);

                if (!isLastPositionEnabled || i < lastPosition || (lastPosition + REFRESH_GAP) < i)
                    if (i != 0)
                    canvas.drawLine(lastX, lastY, newX, newY, pointPaint);

                lastX = newX;
                lastY = newY;
            }
        }

        // Draw cursors
        if (currentCursorsState == CURSORS_STATES.VOLTAGE) {
            int voltCursor0Y = (int) (height * timeCursorsPosition[0]);
            int voltCursor1Y = (int) (height * timeCursorsPosition[1]);

            int voltCursorHandle0Y = (int) (width * voltageCursorsHandlePosition[0]);
            int voltCursorHandle1Y = (int) (width * voltageCursorsHandlePosition[1]);


            canvas.drawLine(
                AXIS_OFFSET, voltCursor0Y,
                width - AXIS_OFFSET, voltCursor0Y,
                    (currentCursorHandlerState == CURSOR_HANDLERS_STATES.VOLT0_SELECTED) ?
                            cursorClickedPaint :
                            cursorPaint);

            canvas.drawCircle(voltCursorHandle0Y, voltCursor0Y,
                    (currentCursorHandlerState == CURSOR_HANDLERS_STATES.VOLT0_SELECTED) ?
                            CURSOR_HANDLE_CLICKED_RADIUS :
                            CURSOR_HANDLE_RADIUS,
                    (currentCursorHandlerState == CURSOR_HANDLERS_STATES.VOLT0_SELECTED) ?
                        cursorHandleClickedPaint :
                        cursorHandlePaint);

            canvas.drawLine(
                AXIS_OFFSET, voltCursor1Y,
                width - AXIS_OFFSET, voltCursor1Y,
                    cursorPaint);
            canvas.drawCircle(voltCursorHandle1Y, voltCursor1Y, CURSOR_HANDLE_RADIUS, cursorHandlePaint);

        }
        else if (currentCursorsState == CURSORS_STATES.TIME) {

        }
    }

    private boolean detectHandleClick(double touchX, double touchY, double cursorX, double cursorY) {
        System.out.println("distance: " + Math.sqrt(Math.pow(touchY - cursorY, 2.0) + Math.pow(touchX - cursorX, 2.0)));
        return (Math.sqrt(Math.pow(touchY - cursorY, 2.0) + Math.pow(touchX - cursorX, 2.0)))
                <= CURSOR_HANDLE_CLICKED_RADIUS;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isGraphPaused) {
            System.out.println(event.getAction() + ": " + event.getX() + ", " + event.getY());

            float touchX = event.getX();
            float touchY = event.getY();

            int screenWidth = getWidth();
            int screenHeight = getHeight();

            if (event.getAction() == MotionEvent.ACTION_DOWN) {


                System.out.println("ACTION_DOWN");

                // Detect if it was on any cursor handle
                if (detectHandleClick(touchX, touchY,
                        voltageCursorsHandlePosition[0] * screenWidth, voltageCursorsPosition[0] * screenHeight)) {
                    currentCursorHandlerState = CURSOR_HANDLERS_STATES.VOLT0_SELECTED;
                    System.out.println("VOLT0_SELECTED");
                }
                else {
                    currentCursorHandlerState = CURSOR_HANDLERS_STATES.NONE_SELECTED;
                    System.out.println("NONE_SELECTED");
                }
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                switch (currentCursorHandlerState) {
                    case VOLT0_SELECTED:

                        voltageCursorsPosition[0] = touchY / screenHeight;
                        voltageCursorsHandlePosition[0] = touchX / screenWidth;

                        break;
                }
            }
                else if (event.getAction() == MotionEvent.ACTION_UP ||
                     event.getAction() == MotionEvent.ACTION_CANCEL) {
                currentCursorHandlerState = CURSOR_HANDLERS_STATES.NONE_SELECTED;
            }
            this.invalidate();
        }

        return true;
    }

    public void setGraphPaused(boolean graphPaused) {
        this.isGraphPaused = graphPaused;
    }

    public void toggleCurrentCursorsState() {
        switch (currentCursorsState) {
            case VOLTAGE:
                currentCursorsState = CURSORS_STATES.TIME;
                break;
            case TIME:
                currentCursorsState = CURSORS_STATES.OFF;
                break;
            case OFF:
                currentCursorsState = CURSORS_STATES.VOLTAGE;
                break;
        }
    }

    public CURSORS_STATES getCurrentCursorsState() {
        return this.currentCursorsState;
    }

    /**
     * @return position of voltage cursors, in screen fraction (0 is the lowest positon, 1 is the highest)
     */
    public float[] getVoltageCursors () {
        return voltageCursorsPosition;
    }

    /**
     * @return position of time cursors, in screen fraction (0 is the leftmost positon, 1 is the rightmost)
     */
    public float[] getTImeCursors () {
        return timeCursorsPosition;
    }

    public void setMaxYValue(float maxYValue) {
        this.maxYValue = maxYValue;
    }

    public void updateData(DataPoint[] data, char triggerLevelPercent) {
        updateData(data, -1, triggerLevelPercent);
    }

    public void updateData(DataPoint[] data, int lastPosition, char triggerLevelPercent) {
        this.data = data;

        if (lastPosition != -1) {
            isLastPositionEnabled = true;
            this.lastPosition = lastPosition;
        }
        else
            isLastPositionEnabled = false;

        this.triggerLevelPercent = triggerLevelPercent;

        REFRESH_GAP = (int) (0.05 * Settings.getCurrentBlockSize());

        this.invalidate();
    }

    public static class DataPoint {
        public float x;
        public float y;

        public DataPoint (float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
