package com.example.gamepadkeyboard; // Use your actual package name

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RadialMenuView extends View {

    private Paint slicePaint;
    private Paint textPaint;

    private List<String> menuItems = new ArrayList<>(); // Initialize as empty
    private RectF sliceArcRect = new RectF(); // Reusable RectF for drawing arcs

    private float centerX, centerY;
    private float outerRadius;
    private float innerRadiusRatio = 0.4f; // 40% of outerRadius for the hollow center
    private float actualInnerRadius;
    private Paint hoverSlicePaint;
    private int hoveredSliceIndex = -1; // -1 means no slice is hovered


    public RadialMenuView(Context context) {
        super(context);
        init();
    }

    public RadialMenuView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadialMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        hoverSlicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hoverSlicePaint.setStyle(Paint.Style.FILL);
        hoverSlicePaint.setColor(Color.GRAY); // Hover color
        slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slicePaint.setStyle(Paint.Style.FILL);
        slicePaint.setColor(Color.BLACK); // Slices are black

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE); // Text is white
        textPaint.setTextSize(60f); // Adjusted for better visibility
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public void setMenuItems(List<String> newItems) {
        this.menuItems.clear(); // Clear existing items
        if (newItems != null) {
            this.menuItems.addAll(newItems); // Add all new items
        }
        // No need to call calculateSliceData() here if onSizeChanged and onDraw handle it
        invalidate(); // Request a redraw because the content has changed
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        outerRadius = (Math.min(w, h) / 2f) * 0.95f; // Use 95% of available space
        actualInnerRadius = outerRadius * innerRadiusRatio;

        // Define the bounding box for the arcs (the outer circle)
        sliceArcRect.set(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius);
    }

    /**
     * Updates the hovered slice based on X and Y coordinates relative to the center of this view.
     *
     * @param relativeX The X coordinate of the hover point, relative to the view's center.
     *                  (e.g., joystickX - centerX_of_joystick_view_itself)
     * @param relativeY The Y coordinate of the hover point, relative to the view's center.
     *                  (e.g., joystickY - centerY_of_joystick_view_itself)
     */
    public void updateHover(float relativeX, float relativeY) {
        if (menuItems.isEmpty()) {
            if (hoveredSliceIndex != -1) {
                hoveredSliceIndex = -1;
                invalidate();
            }
            return;
        }

        // Calculate the angle of the input vector.
        // Math.atan2 returns values from -PI to PI.
        // We convert it to degrees: -180 to 180.
        double angleRadians = Math.atan2(relativeY, relativeX);
        float angleDegrees = (float) Math.toDegrees(angleRadians);

        // Normalize angle to be 0-360, where 0 degrees is to the right (positive X-axis).
        // Our drawing starts with the first slice centered at -90 degrees (top),
        // so we need to map correctly.
        if (angleDegrees < 0) {
            angleDegrees += 360;
        }

        float sweepAngle = 360f / menuItems.size();
        // The start angle of the *drawing* for the first slice's edge.
        // The first slice is centered at -90 (top). Its drawing starts at -90 - sweep/2.
        float firstSliceDrawStartAngle = -90f - (sweepAngle / 2f);

        int newHoveredIndex = -1;

        // Check if the input vector is outside the dead zone (inner circle)
        // and inside the outer radius. This is a simplified check.
        // A more robust check would involve the actual magnitude of (relativeX, relativeY).
        // For now, we assume any non-zero vector means we should check the angle.
        // A true dead zone check for the joystick should happen before calling this.
        if (relativeX != 0 || relativeY != 0) { // Basic check: if joystick is moved
            // Shift the angleDegrees to align with our drawing coordinate system
            // where the first slice's edge starts at firstSliceDrawStartAngle.
            // We want to find which segment the angleDegrees falls into.
            float adjustedAngle = angleDegrees - firstSliceDrawStartAngle;
            if (adjustedAngle < 0) {
                adjustedAngle += 360;
            }
            if (adjustedAngle >= 360) { // Should not happen if logic is correct, but for safety
                adjustedAngle -= 360;
            }

            newHoveredIndex = (int) (adjustedAngle / sweepAngle);

            // Ensure the index is within bounds (0 to menuItems.size() - 1)
            if (newHoveredIndex >= menuItems.size()) {
                newHoveredIndex = menuItems.size() - 1;
            }
            if (newHoveredIndex < 0) { // Should not happen
                newHoveredIndex = 0;
            }

        } else { // Joystick is centered (or input is 0,0)
            newHoveredIndex = -1;
        }

        // Also, ensure the hover is within the interactive ring (not in the hollow center)
        // This requires knowing the magnitude of the joystick input relative to its max travel,
        // and comparing that to innerRadiusRatio.
        // For now, this angle-based selection assumes the joystick is outside the dead zone.
        // A more complete solution would pass joystick magnitude here too.
        // If joystick magnitude < innerRadiusRatio * max_joystick_travel, then newHoveredIndex = -1.

        if (hoveredSliceIndex != newHoveredIndex) {
            hoveredSliceIndex = newHoveredIndex;
            invalidate(); // Redraw
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (menuItems.isEmpty() || outerRadius <= 0) {
            return;
        }

        float sweepAngle = 360f / menuItems.size();
        // Start drawing from the top, adjusting so the middle of the first slice is at 12 o'clock
        float currentDrawAngle = -90f - (sweepAngle / 2f);

        // For text measurement and positioning
        Rect textBounds = new Rect();

        for (int i = 0; i < menuItems.size(); i++) {
            // 1. Draw the slice
            if (i == hoveredSliceIndex) {
                canvas.drawArc(sliceArcRect, currentDrawAngle, sweepAngle, true, hoverSlicePaint);
            } else {
                canvas.drawArc(sliceArcRect, currentDrawAngle, sweepAngle, true, slicePaint);
            }

            // 2. Draw the text
            String text = menuItems.get(i);
            // Measure text to help center it
            textPaint.getTextBounds(text, 0, text.length(), textBounds);

            // Calculate the angle for the center of the current slice's arc
            float textAngleRad = (float) Math.toRadians(currentDrawAngle + sweepAngle / 2);

            // Calculate the radius for the text (midpoint between inner and outer radius)
            float textRadius = actualInnerRadius + (outerRadius - actualInnerRadius) / 2;

            // Calculate text position
            float textX = centerX + textRadius * (float) Math.cos(textAngleRad);
            // Adjust Y for text height to truly center it vertically within its drawn height
            float textY = centerY + textRadius * (float) Math.sin(textAngleRad) + textBounds.height() / 2f;

            canvas.drawText(text, textX, textY, textPaint);

            currentDrawAngle += sweepAngle;
        }

        // 3. Draw the hollow center (optional, if innerRadiusRatio > 0)
        if (actualInnerRadius > 0) {
            Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            // Set color to whatever the background of RadialMenuView's parent is.
            // For simplicity, using WHITE. If your keyboard background is different,
            // you might want to pass this color in or make it transparent.
            centerPaint.setColor(Color.parseColor("#333333")); // A common dark keyboard background
            // If your RadialMenuView itself has a background color set in XML,
            // you could try to match that, or use a transparent clearing mode.
            // For now, a solid color matching a typical dark theme.
            centerPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(centerX, centerY, actualInnerRadius, centerPaint);
        }
    }

    public String getHoveredKey() {
        if (hoveredSliceIndex >= 0 && hoveredSliceIndex < menuItems.size())
            return this.menuItems.get(hoveredSliceIndex);
        else return null;
    }
}