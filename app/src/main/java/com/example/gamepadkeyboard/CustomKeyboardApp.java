package com.example.gamepadkeyboard;

import android.accessibilityservice.AccessibilityService;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;


public class CustomKeyboardApp extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private static final String TAG = "CustomKeyboardApp";
    private JoystickCircleView joystickViewLeft, joystickViewRight; // Reference to our custom view
    private RadialMenuView radialMenuView; // Reference to our custom view
    private KeyboardView keyboardView; // Keep a reference if needed elsewhere

    // Dead zone for joystick to prevent drift when centered
    private static final float JOYSTICK_DEAD_ZONE = 0.15f;

    @Override
    public View onCreateInputView() {
        // Inflate the layout that contains both KeyboardView and JoystickCircleView
        View rootLayout = getLayoutInflater().inflate(R.layout.custom_keyboard_layout, null);

        keyboardView = (KeyboardView) rootLayout.findViewById(R.id.keyboard_view); // Get KeyboardView from root
        Keyboard keyboard = new Keyboard(this, R.xml.custom_keypad);
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        // Get a reference to the JoystickCircleView
        joystickViewLeft = (JoystickCircleView) rootLayout.findViewById(R.id.jscv_left);
        joystickViewRight = (JoystickCircleView) rootLayout.findViewById(R.id.jscv_right);
        radialMenuView = (RadialMenuView) rootLayout.findViewById(R.id.radial_menu);

        // You can customize the joystick view here if needed
        // joystickView.setCircleColor(Color.BLUE);
        // joystickView.setCircleRadius(40f);

        return rootLayout; // Return the root layout
    }

    // In CustomKeyboardApp.java

    private void handleTriggerPress(JoystickCircleView jscv) {
        if (jscv == null || keyboardView == null || keyboardView.getKeyboard() == null) {
            Log.w(TAG, "Cannot handle trigger press: joystick or keyboard not initialized.");
            return;
        }

        // 1. Get the current absolute X, Y of the center of the joystick's circle
        //    relative to the screen or the keyboard's parent.
        //    The JoystickCircleView's circleX and circleY are relative to itself.
        //    We need to translate that to coordinates relative to the KeyboardView.

        int[] joystickViewLocation = new int[2];
        jscv.getLocationOnScreen(joystickViewLocation); // Gets top-left of JoystickView on screen

        // Get the center of the circle within the JoystickView (assuming circleX, circleY are center coords)
        float circleCenterXInJoystickView = jscv.getCircleCenterX(); // You'll need to add these getters
        float circleCenterYInJoystickView = jscv.getCircleCenterY();

        // Absolute screen coordinates of the circle's center
        int circleAbsoluteX = (int) (joystickViewLocation[0] + circleCenterXInJoystickView);
        int circleAbsoluteY = (int) (joystickViewLocation[1] + circleCenterYInJoystickView);

        Log.d(TAG, "Joystick circle screen center: X=" + circleAbsoluteX + ", Y=" + circleAbsoluteY);

        // 2. Iterate through the keys of your KeyboardView
        Keyboard currentKeyboard = keyboardView.getKeyboard();
        if (currentKeyboard == null) return;

        for (Keyboard.Key key : currentKeyboard.getKeys()) {
            // 3. Check if the circle's absolute position falls within the key's bounds.
            //    Keyboard.Key coordinates are relative to the KeyboardView's origin.
            int[] keyboardViewLocation = new int[2];
            keyboardView.getLocationOnScreen(keyboardViewLocation); // Gets top-left of KeyboardView on screen

            // Calculate the key's absolute screen bounds
            int keyAbsoluteLeft = keyboardViewLocation[0] + key.x;
            int keyAbsoluteTop = keyboardViewLocation[1] + key.y;
            int keyAbsoluteRight = keyAbsoluteLeft + key.width;
            int keyAbsoluteBottom = keyAbsoluteTop + key.height;

        /*
        Log.d(TAG, "Checking key: " + (char)key.codes[0] +
                     " Abs Bounds: L=" + keyAbsoluteLeft + " T=" + keyAbsoluteTop +
                     " R=" + keyAbsoluteRight + " B=" + keyAbsoluteBottom);
        */

            if (circleAbsoluteX >= keyAbsoluteLeft && circleAbsoluteX < keyAbsoluteRight &&
                    circleAbsoluteY >= keyAbsoluteTop && circleAbsoluteY < keyAbsoluteBottom) {

                Log.i(TAG, "Joystick circle is over key: " + (key.codes != null && key.codes.length > 0 ? (char)key.codes[0] : "N/A"));

                // 4. Simulate a press of that key
                // You can use the OnKeyboardActionListener to simulate the press
                if (key.codes != null && key.codes.length > 0) {
                    // Standard key press
                    onKey(key.codes[0], key.codes);

                    // Optional: Visual feedback on the KeyboardView (shows the key press)
                    // keyboardView.setPreviewEnabled(true); // Ensure preview is on if you want popups
                    // keyboardView.showPreview(key.codes[0]); // This might not work as expected without more setup
                    // or might be automatic with onKey.
                    // A simpler visual feedback might be to temporarily change
                    // the joystick circle color or the key's background if possible.
                }
                return; // Found the key, no need to check others
            }
        }
        Log.d(TAG, "Joystick circle is not over any key.");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (event != null && inputConnection != null) {
            if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {

                Log.d(TAG, "Gamepad KeyDown: KeyCode = " + keyCode + ", Event = " + event.toString()); // Log all gamepad key events

                if (keyCode == KeyEvent.KEYCODE_BUTTON_A /* || ... other buttons you handle */) {
                    Log.d(TAG, "Gamepad 'A' button pressed!");
                    // Your 'A' button logic
                    return true; // Consume the event
                } else if (keyCode == KeyEvent.KEYCODE_BUTTON_B) { // Example for 'B' button
                    Log.d(TAG, "Gamepad 'B' button pressed! Consuming event.");
                    // Add logic for 'B' button if needed, or just consume it
                    return true; // Consume the event to prevent default behavior (like 'Back')
                } else if (keyCode == KeyEvent.KEYCODE_BUTTON_Y) { // Example for 'B' button
                    Log.d(TAG, "Gamepad 'Y' button pressed! Consuming event.");
                    // Add logic for 'Y' button if needed, or just consume it
                    return true; // Consume the event to prevent default behavior (like 'Space')
                } else if (keyCode == KeyEvent.KEYCODE_BUTTON_X) { // Example for 'B' button
                    Log.d(TAG, "Gamepad 'X' button pressed! Consuming event.");
                    // Add logic for 'X' button if needed, or just consume it
                    return true; // Consume the event to prevent default behavior (like 'Delete')
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                        keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    Log.d(TAG, "Gamepad D-PAD pressed! Consuming event. KeyCode: " + keyCode);
                    // Add logic for D-PAD if needed for your IME navigation
                    return true; // Consume D-PAD events
                } else if (keyCode == KeyEvent.KEYCODE_BUTTON_L2 || keyCode == KeyEvent.KEYCODE_BUTTON_Z || keyCode == KeyEvent.KEYCODE_BUTTON_L1) { // Or other potential trigger codes
                    Log.d(TAG, "Left Trigger Pressed!");
                    handleTriggerPress(joystickViewLeft);
                    return true; // Consume the event
                } else if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_BUTTON_R1) { // Or other potential trigger codes
                    Log.d(TAG, "Right Trigger Pressed!");
                    handleTriggerPress(joystickViewRight);
                    return true; // Consume the event
                }
                // Add more 'else if' blocks for other buttons you want to handle or consume

                // If you want to consume ALL gamepad buttons within your IME:
                // Log.d(TAG, "Consuming unhandled Gamepad KeyDown: KeyCode = " + keyCode);
                // return true; // Be careful with this, it might block essential system functions if not intended.


            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // Similarly for onKeyUp
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event != null) {
            if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {

                Log.d(TAG, "Gamepad KeyUp: KeyCode = " + keyCode);

                if (keyCode == KeyEvent.KEYCODE_BUTTON_B /* || other buttons */) {
                    return true; // Consume the release as well
                }
                // ... other specific button handling for key up
                // return true; // If you consumed it in onKeyDown, likely consume here too.
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void setCursorPos(JoystickCircleView jscv, float x, float y) {
        if (Math.abs(x) < JOYSTICK_DEAD_ZONE) {
            x = 0.0f;
        }
        if (Math.abs(y) < JOYSTICK_DEAD_ZONE) {
            y = 0.0f;
        }

        // Log joystick values for debugging
        // Log.d(TAG, "Joystick X: " + x + ", Y: " + y);

        // Update the custom view's circle position
        jscv.updatePosition(x, y);
        radialMenuView.updateHover(x, y);

        // If both x and y are 0 (joystick is centered or within dead zone), reset.
        // This handles the "snap back when let go" part.
        if (x == 0.0f && y == 0.0f) {
            jscv.resetPosition();
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (joystickViewLeft == null || joystickViewRight == null) {
            return super.onGenericMotionEvent(event);
        }

        // Check if the event is from a joystick
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            // Process the left analog stick (common axes)
            // You might need to check event.getDevice().getMotionRanges() to be sure
            // which axes your controller uses for the left stick.
            float x = event.getAxisValue(MotionEvent.AXIS_X);
            float y = event.getAxisValue(MotionEvent.AXIS_Y);
            float rx = event.getAxisValue(MotionEvent.AXIS_Z);
            float ry = event.getAxisValue(MotionEvent.AXIS_RZ);
            setCursorPos(joystickViewLeft, x, y);
            setCursorPos(joystickViewRight, rx, ry);

            Log.d("Idk how tags work", Float.toString(x) + ", " + Float.toString(y) + ", " + Float.toString(rx) + ", " + Float.toString(ry));

            return true; // Indicate that your IME has handled this motion event
        }

        // It's good practice to also check for D-Pad events if they come as AXIS_HAT_X/Y
        // and you want to visualize them or use them for the circle.
        if ((event.getSource() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
            float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

            // Log D-pad values for debugging
            // Log.d(TAG, "D-Pad HatX: " + hatX + ", HatY: " + hatY);

            // You could also update joystickView here if you want D-Pad to control it
            // joystickView.updatePosition(hatX, hatY);
            // if (hatX == 0.0f && hatY == 0.0f) {
            //    joystickView.resetPosition();
            // }

            return true; // Consume D-pad motion events if you handle them
        }


        return super.onGenericMotionEvent(event);
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return;
        }
        inputConnection.commitText(String.valueOf((char) primaryCode), 1);
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
