// JoystickCircleView.java
package com.example.gamepadkeyboard; // Use your actual package name

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class JoystickCircleView extends View {

    private Paint circlePaint;
    private float circleX, circleY;
    private float initialX, initialY; // To store the initial center position
    private float radius = 30f; // Radius of the circle
    private boolean isInitialized = false;

    public float getCircleCenterX() {
        return circleX; // Assuming circleX is the center
    }

    public float getCircleCenterY() {
        return circleY; // Assuming circleY is the center
    }

    public JoystickCircleView(Context context) {
        super(context);
        init();
    }

    public JoystickCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JoystickCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.RED); // Or any color you like
        circlePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // No need to set isInitialized here if we do it in init() or ensure onDraw waits
        // Initialize circle to the center of this view
        initialX = w / 2f;
        initialY = h / 2f;
        if (!isInitialized) { // Only set initial position if not already moved by joystick
            circleX = initialX;
            circleY = initialY;
        }
        isInitialized = true; // Ensure this is set after initialX/Y are calculated
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInitialized) {
            canvas.drawCircle(circleX, circleY, radius, circlePaint);
        }
    }

    /**
     * Updates the position of the circle.
     * x and y are normalized joystick values (-1 to 1).
     * The view will scale these to its own bounds.
     */
    public void updatePosition(float normalizedX, float normalizedY) {
        if (!isInitialized) return;

        // For simplicity, let's assume the joystick moves the circle within its bounds.
        // You might want a more sophisticated mapping (e.g., a smaller dead zone, max travel distance)
        float maxTravelX = (getWidth() / 2f) - radius;
        float maxTravelY = (getHeight() / 2f) - radius;

        circleX = initialX + (normalizedX * maxTravelX);
        circleY = initialY + (normalizedY * maxTravelY);

        // Keep circle within the bounds of this view
        circleX = Math.max(radius, Math.min(circleX, getWidth() - radius));
        circleY = Math.max(radius, Math.min(circleY, getHeight() - radius));

        invalidate(); // Request a redraw
    }

    /**
     * Resets the circle to its initial center position.
     */
    public void resetPosition() {
        if (!isInitialized) return;
        circleX = initialX;
        circleY = initialY;
        invalidate(); // Request a redraw
    }

    public void setCircleColor(int color) {
        circlePaint.setColor(color);
        invalidate();
    }

    public void setCircleRadius(float radius) {
        this.radius = radius;
        invalidate();
    }
}