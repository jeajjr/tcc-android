package tk.jalmas.tcc.visualisador;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MySimpleGraph extends View {
    private DataPoint[] data;

    private boolean isLastPositionEnabled;
    private int lastPosition;
    private final int OFFSET = 50;

    private char triggerLevelPercent;
    private float maxYValue;

    private Paint axisPaint;
    private Paint gridPaint;
    private Paint pointPaint;

    private final int AXIS_OFFSET = 2;
    private final int AXIS_STROKE = 4;

    private final int GRID_STROKE = 1;
    private final int GRID_X_COUNT = 10;
    private final int GRID_Y_COUNT = 8;

    private final int POINT_RADIUS = 4;
    private final int POINT_STROKE = 2;

    public void init() {
        isLastPositionEnabled = false;
        lastPosition = 0;

        triggerLevelPercent = 0;

        axisPaint = new Paint(Color.BLACK);
        axisPaint.setStrokeWidth(AXIS_STROKE);

        gridPaint = new Paint(Color.BLACK);
        gridPaint.setStrokeWidth(GRID_STROKE);

        pointPaint = new Paint(Color.RED);
        pointPaint.setStrokeWidth(POINT_STROKE);

        this.setBackgroundColor(Color.WHITE);

        maxYValue = 0;
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

                if (!isLastPositionEnabled || i < lastPosition || (lastPosition + OFFSET) < i)
                    if (i != 0)
                    canvas.drawLine(lastX, lastY, newX, newY, pointPaint);

                lastX = newX;
                lastY = newY;
            }
        }
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
