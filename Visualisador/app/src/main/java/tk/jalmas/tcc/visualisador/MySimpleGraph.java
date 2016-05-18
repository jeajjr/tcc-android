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

    private enum CURSOR_CLICK_STATES {NONE_SELECTED, VOLT0_SELECTED, VOLT1_SELECTED, TIME0_SELECTED, TIME1_SELECTED}
    private CURSOR_CLICK_STATES currentCursorClickState;

    private boolean isGraphPaused;
    private float[] voltageCursorsPosition;         //current position in pixels over screen height
    private float[] timeCursorsPosition;            //current position in pixels over screen width

    private OnCursorMovedListener onCursorMovedListener;

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
    public final int GRID_X_COUNT = 10;
    public final int GRID_Y_COUNT = 8;

    private Paint pointPaint;
    private final int POINT_RADIUS = 4;
    private final int POINT_STROKE = 2;

    private Paint cursorPaint;
    private final int CURSOR_STROKE = 2;

    private Paint cursorClickedPaint;
    private final int CURSOR_CLIKED_STROKE = 10;
    private final int CURSOR_CLICK_DISTANCE = 80;

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

        this.setBackgroundColor(Color.WHITE);

        maxYValue = 0;

        currentCursorsState = CURSORS_STATES.OFF;
        currentCursorClickState = CURSOR_CLICK_STATES.NONE_SELECTED;
        isGraphPaused = false;

        voltageCursorsPosition = new float[]{0.1f, 0.6f};
        timeCursorsPosition = new float[]{0.1f, 0.6f};
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
        if (isGraphPaused) {
            if (currentCursorsState == CURSORS_STATES.VOLTAGE) {
                int voltCursor0Y = (int) (height * voltageCursorsPosition[0]);
                int voltCursor1Y = (int) (height * voltageCursorsPosition[1]);

                canvas.drawLine(
                        AXIS_OFFSET, voltCursor0Y,
                        width - AXIS_OFFSET, voltCursor0Y,
                        (currentCursorClickState == CURSOR_CLICK_STATES.VOLT0_SELECTED) ?
                                cursorClickedPaint :
                                cursorPaint);

                canvas.drawLine(
                        AXIS_OFFSET, voltCursor1Y,
                        width - AXIS_OFFSET, voltCursor1Y,
                        (currentCursorClickState == CURSOR_CLICK_STATES.VOLT1_SELECTED) ?
                                cursorClickedPaint :
                                cursorPaint);
            } else if (currentCursorsState == CURSORS_STATES.TIME) {
                int timeCursor0Y = (int) (width * timeCursorsPosition[0]);
                int timeCursor1Y = (int) (width * timeCursorsPosition[1]);

                canvas.drawLine(
                        timeCursor0Y,AXIS_OFFSET,
                        timeCursor0Y, height - AXIS_OFFSET,
                        (currentCursorClickState == CURSOR_CLICK_STATES.TIME0_SELECTED) ?
                                cursorClickedPaint :
                                cursorPaint);

                canvas.drawLine(
                        timeCursor1Y, AXIS_OFFSET,
                        timeCursor1Y, height - AXIS_OFFSET,
                        (currentCursorClickState == CURSOR_CLICK_STATES.TIME1_SELECTED) ?
                                cursorClickedPaint :
                                cursorPaint);
            }
        }
    }

    private boolean detectHandleClick(double touchCoordinate, double cursorCoordinate) {
        return (Math.abs(touchCoordinate - cursorCoordinate))
                <= CURSOR_CLICK_DISTANCE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isGraphPaused) {
            float touchX = event.getX();
            float touchY = event.getY();

            int screenWidth = getWidth();
            int screenHeight = getHeight();

            switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                System.out.println("ACTION_DOWN");

                boolean touchedHandle = false;

                // Detect if it was on voltage cursor handle
                if (currentCursorsState == CURSORS_STATES.VOLTAGE) {
                    if (detectHandleClick(touchY, voltageCursorsPosition[0] * screenHeight)) {
                        currentCursorClickState = CURSOR_CLICK_STATES.VOLT0_SELECTED;
                        System.out.println("VOLT0_SELECTED");
                        touchedHandle = true;
                    } else if (detectHandleClick(touchY, voltageCursorsPosition[1] * screenHeight)) {
                        currentCursorClickState = CURSOR_CLICK_STATES.VOLT1_SELECTED;
                        System.out.println("VOLT1_SELECTED");
                        touchedHandle = true;
                    }
                }

                // Detect if it was on voltage cursor handle
                if (currentCursorsState == CURSORS_STATES.TIME) {
                    if (detectHandleClick(touchX, timeCursorsPosition[0] * screenWidth)) {
                        currentCursorClickState = CURSOR_CLICK_STATES.TIME0_SELECTED;
                        System.out.println("TIME0_SELECTED");
                        touchedHandle = true;

                    } else if (detectHandleClick(touchX, timeCursorsPosition[1] * screenWidth)) {
                        currentCursorClickState = CURSOR_CLICK_STATES.TIME1_SELECTED;
                        System.out.println("TIME1_SELECTED");
                        touchedHandle = true;
                    }
                }

                if (!touchedHandle) {
                    currentCursorClickState = CURSOR_CLICK_STATES.NONE_SELECTED;
                    System.out.println("NONE_SELECTED");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                System.out.println("ACTION_MOVE");

                switch (currentCursorClickState) {
                    case VOLT0_SELECTED:
                        voltageCursorsPosition[0] = touchY / screenHeight;
                        if (voltageCursorsPosition[0] < 0) voltageCursorsPosition[0] = 0;
                        if (voltageCursorsPosition[0] > 1) voltageCursorsPosition[0] = 1;

                        if (onCursorMovedListener != null)
                            onCursorMovedListener.onVoltageCursorMoved(voltageCursorsPosition[0], voltageCursorsPosition[1]);
                        break;

                    case VOLT1_SELECTED:
                        voltageCursorsPosition[1] = touchY / screenHeight;
                        if (voltageCursorsPosition[1] < 0) voltageCursorsPosition[1] = 0;
                        if (voltageCursorsPosition[1] > 1) voltageCursorsPosition[1] = 1;

                        if (onCursorMovedListener != null)
                            onCursorMovedListener.onVoltageCursorMoved(voltageCursorsPosition[0], voltageCursorsPosition[1]);
                        break;

                    case TIME0_SELECTED:
                        timeCursorsPosition[0] = touchX / screenWidth;
                        if (timeCursorsPosition[0] < 0) timeCursorsPosition[0] = 0;
                        if (timeCursorsPosition[0] > 1) timeCursorsPosition[0] = 1;

                        if (onCursorMovedListener != null)
                            onCursorMovedListener.onTimeCursorMoved(timeCursorsPosition[0], timeCursorsPosition[1]);
                        break;

                    case TIME1_SELECTED:
                        timeCursorsPosition[1] = touchX / screenWidth;
                        if (timeCursorsPosition[1] < 0) timeCursorsPosition[1] = 0;
                        if (timeCursorsPosition[1] > 1) timeCursorsPosition[1] = 1;

                        if (onCursorMovedListener != null)
                            onCursorMovedListener.onTimeCursorMoved(timeCursorsPosition[0], timeCursorsPosition[1]);
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                System.out.println("ACTION_UP/ACTION_CANCEL");

                currentCursorClickState = CURSOR_CLICK_STATES.NONE_SELECTED;
                break;
            }

            this.invalidate();
        }

        return true;
    }

    public void setGraphPaused(boolean graphPaused) {
        this.isGraphPaused = graphPaused;

        this.invalidate();
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
        this.invalidate();
    }

    void setOnCursorMovedListener(OnCursorMovedListener onCursorMovedListener) {
        this.onCursorMovedListener = onCursorMovedListener;
    }

    public CURSORS_STATES getCurrentCursorsState() {
        return this.currentCursorsState;
    }

    /**
     * @return position of voltage cursors, in screen fraction (0 is the lowest position, 1 is the highest)
     */
    public float[] getVoltageCursors () {
        return new float[] {1.0f - voltageCursorsPosition[0], 1.0f - voltageCursorsPosition[1]};
    }

    /**
     * @return position of time cursors, in screen fraction (0 is the leftmost positon, 1 is the rightmost)
     */
    public float[] getTimeCursors() {
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
/*
        System.out.println(data.length);
        for (DataPoint d : data)
            System.out.print(d.y + " ");
        System.out.println();
*/
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
