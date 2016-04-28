package tk.jalmas.tcc.visualisador;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MySimpleGraph extends View {
    private DataPoint[] data;

    private Paint axisPaint;
    private Paint gridPaint;
    private Paint pointPaint;

    private final int AXIS_OFFSET = 2;
    private final int AXIS_STROKE = 4;

    private final int GRID_STROKE = 1;
    private final int GRID_X_COUNT = 10;
    private final int GRID_Y_COUNT = 5;

    private final int POINT_RADIUS = 4;
    private final int POINT_STROKE = 2;

    public void init() {
        axisPaint = new Paint(Color.BLACK);
        axisPaint.setStrokeWidth(AXIS_STROKE);

        gridPaint = new Paint(Color.BLACK);
        gridPaint.setStrokeWidth(GRID_STROKE);

        pointPaint = new Paint(Color.RED);
        pointPaint.setStrokeWidth(POINT_STROKE);

        this.setBackgroundColor(Color.WHITE);
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

        // Draw points
        if (data != null) {
            float minX = data[0].x;
            float maxX = data[data.length - 1].x;
            float deltaX = (maxX - minX);

            float factorY = (float) height / 256;

            int lastX = 0;
            int lastY = 0;

            for (int i = 0; i < data.length; i++) {
                int newX = (int) ((data[i].x - minX) * width / deltaX);
                int newY = (int) ((256 - data[i].y) * factorY);
                //canvas.drawCircle(newX, newY, POINT_RADIUS, pointPaint);
                //System.out.printf("(%f, %f) -> (%d, %d)\n", data[i].x, data[i].y, newX, newY);

                if (i != 0)
                    canvas.drawLine(lastX, lastY, newX, newY, pointPaint);

                lastX = newX;
                lastY = newY;
            }
        }
    }

    public void updateData(DataPoint[] data) {
        this.data = data;

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
