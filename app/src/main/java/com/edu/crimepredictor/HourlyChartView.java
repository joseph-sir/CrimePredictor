package com.edu.crimepredictor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class HourlyChartView extends View {

    private int[] scores = new int[24];
    private int selectedHour = 12;

    private Paint barPaint;
    private Paint highlightPaint;
    private Paint textPaint;
    private Paint linePaint;

    public HourlyChartView(Context context) {
        super(context);
        init();
    }

    public HourlyChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(3f);
        highlightPaint.setColor(Color.parseColor("#121212"));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#68635B"));
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#DCD5C9"));
        linePaint.setStrokeWidth(1.5f);
    }

    public void setData(int[] hourlyScores, int selectedHour) {
        if (hourlyScores != null && hourlyScores.length == 24) {
            System.arraycopy(hourlyScores, 0, this.scores, 0, 24);
        }
        this.selectedHour = selectedHour;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float bottomY = height - 36f;
        float chartHeight = bottomY - 15f;

        // Baseline line
        canvas.drawLine(10f, bottomY, width - 10f, bottomY, linePaint);

        float barSlotWidth = (width - 20f) / 24f;
        float barWidth = Math.max(6f, barSlotWidth * 0.75f);
        float offset = (barSlotWidth - barWidth) / 2f;

        for (int h = 0; h < 24; h++) {
            float x = 10f + (h * barSlotWidth) + offset;
            int score = scores[h];
            float barH = (score / 100f) * chartHeight;
            float topY = bottomY - barH;

            // Bar color
            int color;
            if (score < 30) color = Color.parseColor("#2E6948");
            else if (score < 55) color = Color.parseColor("#C4820A");
            else if (score < 75) color = Color.parseColor("#A8322D");
            else color = Color.parseColor("#7D1A16");

            barPaint.setColor(color);
            barPaint.setAlpha(h == selectedHour ? 255 : 140);

            RectF rect = new RectF(x, topY, x + barWidth, bottomY);
            canvas.drawRoundRect(rect, 4f, 4f, barPaint);

            if (h == selectedHour) {
                canvas.drawRoundRect(rect, 4f, 4f, highlightPaint);
            }

            // Labels at key intervals
            if (h == 0 || h == 6 || h == 12 || h == 18 || h == 23) {
                String lbl = String.format("%02dh", h);
                canvas.drawText(lbl, x + (barWidth / 2f), height - 8f, textPaint);
            }
        }
    }
}
