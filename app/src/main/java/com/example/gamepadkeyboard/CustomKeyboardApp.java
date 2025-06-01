package com.example.gamepadkeyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;


public class CustomKeyboardApp extends InputMethodService {

    private static final String TAG = "CustomKeyboardApp";
    private RadialMenuView radialMenuViewLeft, radialMenuViewRight; // Reference to our custom view
    private static final float JOYSTICK_DEAD_ZONE = 0.15f;
    private boolean isL2Pressed;
    private boolean isR2Pressed;
    private boolean isUpper;
    private boolean isAlt;
    private int layerIndex;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<RadialMenuSet> menuSets;
    private static final double deadZoneRadius = 0.1;


    @Override
    public View onCreateInputView() {
        View rootLayout = getLayoutInflater().inflate(R.layout.custom_keyboard_layout, null);

        try {
            String jsonString = "[\n" +
                    "  {\n" +
                    "    \"base\": {\n" +
                    "      \"left\": {\n" +
                    "        \"lower\": [\"q\", \"w\", \"e\", \"r\", \"t\", \"y\", \"u\", \"i\", \"o\", \"p\"],\n" +
                    "        \"upper\": [\"Q\", \"W\", \"E\", \"R\", \"T\", \"A\", \"S\", \"D\", \"F\", \"G\", \"Z\", \"X\", \"C\", \"V\"]\n" +
                    "      },\n" +
                    "      \"right\": {\n" +
                    "        \"lower\": [\"y\", \"u\", \"i\", \"o\", \"p\", \"h\", \"j\", \"k\", \"l\", \"b\", \"n\", \"m\"],\n" +
                    "        \"upper\": [\"Y\", \"U\", \"I\", \"O\", \"P\", \"H\", \"J\", \"K\", \"L\", \"B\", \"N\", \"M\"]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"alt\": {\n" +
                    "      \"left\": {\n" +
                    "        \"lower\": [\"1\", \"2\", \"3\", \"4\", \"5\"],\n" +
                    "        \"upper\": [\"!\", \"@\", \"#\", \"$\", \"%\"]\n" +
                    "      },\n" +
                    "      \"right\": {\n" +
                    "        \"lower\": [\"6\", \"7\", \"8\", \"9\", \"0\"],\n" +
                    "        \"upper\": [\"^\", \"&\", \"*\", \"(\", \")\"]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"base\": {\n" +
                    "      \"left\": {\n" +
                    "        \"lower\": [\"a\", \"s\", \"d\", \"f\", \"g\", \"h\", \"j\", \"k\", \"l\"],\n" +
                    "        \"upper\": [\"Q\", \"W\", \"E\", \"R\", \"T\", \"A\", \"S\", \"D\", \"F\", \"G\"]\n" +
                    "      },\n" +
                    "      \"right\": {\n" +
                    "        \"lower\": [\")\", \"$\", \"&\", \"@\", \"\\\"\", \"!\", \"'\"],\n" +
                    "        \"upper\": [\"Y\", \"U\", \"I\", \"O\", \"P\", \"H\", \"J\", \"K\", \"L\"]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"alt\": {\n" +
                    "      \"left\": {\n" +
                    "        \"lower\": [\"[\", \"]\", \"{\", \"}\", \"#\", \"_\", \"\\\\\", \"|\", \"~\", \"<\"],\n" +
                    "        \"upper\": [\"A\"]\n" +
                    "      },\n" +
                    "      \"right\": {\n" +
                    "        \"lower\": [\"%\", \"^\", \"*\", \"+\", \"=\", \">\", \"€\", \"£\", \"¥\", \"⋅\"],\n" +
                    "        \"upper\": [\"B\"]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"base\": {\n" +
                    "      \"left\": {\n" +
                    "        \"lower\": [\"z\", \"x\", \"c\", \"v\", \"b\", \"n\", \"m\"],\n" +
                    "        \"upper\": [\"Q\", \"W\", \"E\", \"R\", \"T\", \"A\", \"S\", \"D\", \"F\", \"G\"]\n" +
                    "      },\n" +
                    "      \"right\": {\n" +
                    "        \"lower\": [\")\", \"$\", \"&\", \"@\", \"\\\"\", \"!\", \"'\"],\n" +
                    "        \"upper\": [\"Y\", \"U\", \"I\", \"O\", \"P\", \"H\", \"J\", \"K\", \"L\"]\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"alt\": {\n" +
                    "      \"left\": {\n" +
                    "        \"lower\": [\"[\", \"]\", \"{\", \"}\", \"#\", \"_\", \"\\\\\", \"|\", \"~\", \"<\"],\n" +
                    "        \"upper\": [\"A\"]\n" +
                    "      },\n" +
                    "      \"right\": {\n" +
                    "        \"lower\": [\"%\", \"^\", \"*\", \"+\", \"=\", \">\", \"€\", \"£\", \"¥\", \"⋅\"],\n" +
                    "        \"upper\": [\"B\"]\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "]";
            menuSets = objectMapper.readValue(
                    jsonString,
                    new TypeReference<List<RadialMenuSet>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (menuSets == null || menuSets.isEmpty()) {
            Log.e(TAG, "Failed to parse JSON");
            return rootLayout;
        }

        isUpper = isAlt = isR2Pressed = isL2Pressed = false;
        layerIndex = 0;
        radialMenuViewLeft = (RadialMenuView) rootLayout.findViewById(R.id.radial_menu_left);
        radialMenuViewRight = (RadialMenuView) rootLayout.findViewById(R.id.radial_menu_right);

        return rootLayout;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        isUpper = isAlt = isR2Pressed = isL2Pressed = false;
        layerIndex = 0;
        if (radialMenuViewLeft != null && radialMenuViewRight != null) {
            radialMenuViewLeft.updateHover(0,0);
            radialMenuViewRight.updateHover(0,0);
            menuUpdater();
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        if (radialMenuViewLeft != null) radialMenuViewLeft.updateHover(0,0);
        if (radialMenuViewRight != null) radialMenuViewRight.updateHover(0,0);
    }

    private void handleTriggerPress(RadialMenuView rmv) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (rmv == null) {
            return;
        }

        String key = rmv.getHoveredKey();
        if (key != null) inputConnection.commitText(key, 1);
    }

    public void menuUpdater() {
        if (isUpper) {
            radialMenuViewLeft.setMenuItems(menuSets.get(layerIndex).getPair(isAlt).getLeft().getUpper());
            radialMenuViewRight.setMenuItems(menuSets.get(layerIndex).getPair(isAlt).getRight().getUpper());
        } else {
            radialMenuViewLeft.setMenuItems(menuSets.get(layerIndex).getPair(isAlt).getLeft().getLower());
            radialMenuViewRight.setMenuItems(menuSets.get(layerIndex).getPair(isAlt).getRight().getLower());
        }
    }

    public void dpadHandler(int x, int y) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return;
        }
        if (y == -1) {
            isUpper = !isUpper;
            menuUpdater();
        } else if (y == 1) {
            menuUpdater();
        } else if (x == -1) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
        } else if (x == 1) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isInputViewShown()) {
            return super.onKeyDown(keyCode, event);
        }
        InputConnection inputConnection = getCurrentInputConnection();
        if (event != null && inputConnection != null) {
            if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
                if (event.getRepeatCount() > 0) {
                    if (keyCode == KeyEvent.KEYCODE_BUTTON_X) {
                        inputConnection.deleteSurroundingText(1, 0);
                        return true;
                    }
                    return super.onKeyDown(keyCode, event);
                }

                if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                    layerIndex = (layerIndex + 1) % menuSets.size();
                    isUpper = false;
                    menuUpdater();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
                    Log.d(TAG, "Gamepad 'B' button pressed! Consuming event.");
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BUTTON_X) {
                    inputConnection.deleteSurroundingText(1, 0);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
                    inputConnection.commitText(" ", 1);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    dpadHandler(0, 1);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    dpadHandler(0, -1);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BUTTON_L2 || keyCode == KeyEvent.KEYCODE_BUTTON_Z || keyCode == KeyEvent.KEYCODE_BUTTON_R2) { // Or other potential trigger codes
                    if (keyCode == KeyEvent.KEYCODE_BUTTON_L2 || keyCode == KeyEvent.KEYCODE_BUTTON_Z) isL2Pressed = true;
                    if (keyCode == KeyEvent.KEYCODE_BUTTON_R2) isR2Pressed = true;
                    isAlt = true;
                    menuUpdater();
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
                    handleTriggerPress(radialMenuViewLeft);
                    return true;
                }
                else if (keyCode == KeyEvent.KEYCODE_BUTTON_R1) { // Or other potential trigger codes
                    handleTriggerPress(radialMenuViewRight);
                    return true; // Consume the event
                }
                Log.d(TAG, "Gamepad/Joystick - Unhandled KeyCode in switch: " + keyCode);

            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isInputViewShown()) {
            return super.onKeyUp(keyCode, event);
        }
        if (event != null) {
            if ((event.getSource() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {

                 if (keyCode == KeyEvent.KEYCODE_BUTTON_L2 || keyCode == KeyEvent.KEYCODE_BUTTON_Z || keyCode == KeyEvent.KEYCODE_BUTTON_R2) { // Or other potential trigger codes
                     if (keyCode == KeyEvent.KEYCODE_BUTTON_L2 || keyCode == KeyEvent.KEYCODE_BUTTON_Z) isL2Pressed = false;
                     if (keyCode == KeyEvent.KEYCODE_BUTTON_R2) isR2Pressed = false;
                     if (!isL2Pressed && !isR2Pressed) {
                         isAlt = false;
                         menuUpdater();
                     }
                     return true;
                 }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void setCursorPos(RadialMenuView rmv, float x, float y) {
        if (Math.abs(x) < JOYSTICK_DEAD_ZONE) {
            x = 0.0f;
        }
        if (Math.abs(y) < JOYSTICK_DEAD_ZONE) {
            y = 0.0f;
        }

        rmv.updateHover(x, y);

        if (x == 0.0f && y == 0.0f) {
            rmv.updateHover(0, 0);
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (!isInputViewShown()) {
            return super.onGenericMotionEvent(event);
        }

        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getAxisValue(MotionEvent.AXIS_X);
            float y = event.getAxisValue(MotionEvent.AXIS_Y);
            float rx = event.getAxisValue(MotionEvent.AXIS_Z);
            float ry = event.getAxisValue(MotionEvent.AXIS_RZ);

            if (rx > deadZoneRadius || ry > deadZoneRadius) {
                setCursorPos(radialMenuViewLeft, x, y);
                //setCursorPos(radialMenuViewRight, rx, ry);

                double angleRadians = Math.atan2(rx, ry);
                float angleDegrees = ((180 - (float)Math.toDegrees(angleRadians) + 360) % 360);
                double sliceDegrees = 360.0 / (double)menuSets.size();
                int slice = (int)Math.floor(angleDegrees / sliceDegrees);
                layerIndex = slice;
                menuUpdater();
                return true;
            }

            dpadHandler(Math.round(event.getAxisValue(MotionEvent.AXIS_HAT_X)), Math.round(event.getAxisValue(MotionEvent.AXIS_HAT_Y)));
            return true;
        }
        return super.onGenericMotionEvent(event);
    }
}
