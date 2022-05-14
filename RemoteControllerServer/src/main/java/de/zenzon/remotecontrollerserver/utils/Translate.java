package de.zenzon.remotecontrollerserver.utils;

import java.util.Arrays;

/**
 * @author : ZenZon
 * @emailto : florian@einerhand.net
 * @since : 14.05.2022, Sa.
 **/
public class Translate {

    byte[] state = new byte[]{
            // Button H (MINUS, PLUS, LCLICK, RCLICK, HOME, CAPTURE)
            0x0,
            // Button L (Y, B, A, X, L, R, ZL, ZR)
            0x0,
            // DPAD
            (byte) 0x80,
            // LX
            (byte) 0x80,
            // LY
            (byte) 0x80,
            // RX
            (byte) 0x80,
            // RY
            (byte) 0x80,
            // VendorSpec
            0x0
    };

    public void input(String string) {
        byte[] stateCopy = Arrays.copyOf(state, state.length);
        String s = string.substring(0, 2);
        String value = string.substring(2);
        String[] data = value.split(";");
        switch (s) {
            case "ra":
                stickMoveX(Integer.parseInt(data[0]), R_STICK);
                stickMoveY(Integer.parseInt(data[1]), R_STICK);
                break;
            case "la":
                stickMoveX(Integer.parseInt(data[0]), L_STICK);
                stickMoveY(Integer.parseInt(data[1]), L_STICK);
                break;
            case "tr":
                triggerMove(ZR, Integer.parseInt(value));
                break;
            case "tl":
                triggerMove(ZL, Integer.parseInt(value));
                break;
            case "ba":
                setButton(A, value);
                break;
            case "bb":
                setButton(B, value);
                break;
            case "bx":
                setButton(X, value);
                break;
            case "by":
                setButton(Y, value);
                break;
            case "lb":
                setButton(L, value);
                break;
            case "rb":
                setButton(R, value);
                break;
            case "bs":
                setButton(PLUS, value);
                break;
            case "dp":
                boolean left = data[0].equals("1");
                boolean up = data[1].equals("1");
                boolean right = data[2].equals("1");
                boolean down = data[3].equals("1");
                if (!left && !up && !right && !down) {
                    dpadMove(DpadPos.CENTER);
                    break;
                }
                if (up && right) {
                    dpadMove(DpadPos.UP_RIGHT);
                } else if (up && left) {
                    dpadMove(DpadPos.UP_LEFT);
                } else if (down && right) {
                    dpadMove(DpadPos.DOWN_RIGHT);
                } else if (down && left) {
                    dpadMove(DpadPos.DOWN_LEFT);
                } else if (up) {
                    dpadMove(DpadPos.UP);
                } else if (down) {
                    dpadMove(DpadPos.DOWN);
                } else if (left) {
                    dpadMove(DpadPos.LEFT);
                } else {
                    dpadMove(DpadPos.RIGHT);
                }
                break;
            case "bp"://l,u,r,d   dp0;0;0;0
                setButton(MINUS, value);
                break;
            case "bh":
                setButton(HOME, value);
                break;
            case "lt":
                setButton(LCLICK, value);
                break;
            case "rt":
                setButton(RCLICK, value);
                break;
        }
        if (!Arrays.equals(stateCopy, state)) {
            //send out to switch
        }
    }

    public void setDpad(DpadPos dpadPos, String value) {
        if (value.equals("1")) {
            dpadMove(dpadPos);
        } else {
            dpadMove(DpadPos.CENTER);
        }
    }

    public void setButton(int[] button, String value) {
        if (value.equals("1")) {
            pressButton(button);
        } else
            releaseButton(button);
    }

    //DPAD
    private int DPAD_TOP = 0x00;
    private int DPAD_TOP_RIGHT = 0x01;
    private int DPAD_RIGHT = 0x02;
    private int DPAD_BOTTOM_RIGHT = 0x03;
    private int DPAD_BOTTOM = 0x04;
    private int DPAD_BOTTOM_LEFT = 0x05;
    private int DPAD_LEFT = 0x06;
    private int DPAD_TOP_LEFT = 0x07;
    private int DPAD_CENTER = 0x08;

    //buttons
    private int[] Y = new int[]{1, 0x01};
    private int[] B = new int[]{1, 0x02};
    private int[] A = new int[]{1, 0x04};
    private int[] X = new int[]{1, 0x08};
    private int[] L = new int[]{1, 0x10};
    private int[] R = new int[]{1, 0x20};
    //trigger
    private int[] ZL = new int[]{1, 0x40};
    private int[] ZR = new int[]{1, 0x80};

    //ctrl buttons
    private int[] MINUS = new int[]{0, 0x01};
    private int[] PLUS = new int[]{0, 0x02};
    private int[] LCLICK = new int[]{0, 0x04};
    private int[] RCLICK = new int[]{0, 0x08};
    private int[] HOME = new int[]{0, 0x10};
    private int[] CAPTURE = new int[]{0, 0x20};

    private int[] L_STICK = new int[]{3, 4};
    private int[] R_STICK = new int[]{5, 6};

    public void pressButton(int[] button) {
        state[button[0]] |= button[1];
    }

    public void releaseButton(int[] button) {
        state[button[0]] &= ~button[1];
    }

    public void triggerMove(int[] trigger, int value) {
        if (value > 128) {
            pressButton(trigger);
        } else {
            releaseButton(trigger);
        }
    }

    public void stickMoveX(int value, int[] stick) {
        state[stick[0]] = (byte) ((scaleBetween(value, Short.MIN_VALUE, Short.MAX_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE)) & 0b11111111);
    }

    public void stickMoveY(int value, int[] stick) {
        state[stick[1]] = (byte) ((-scaleBetween(value, Short.MIN_VALUE, Short.MAX_VALUE, Byte.MIN_VALUE, Byte.MAX_VALUE)) & 0b11111111);
    }

    public enum DpadPos {
        UP(0x00),
        DOWN(0x04),
        LEFT(0x06),
        RIGHT(0x02),
        UP_RIGHT(0x01),
        DOWN_RIGHT(0x03),
        DOWN_LEFT(0x05),
        UP_LEFT(0x07),
        CENTER(0x08);

        private final int value;

        DpadPos(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public void dpadMove(DpadPos dpadPos) {
        state[2] = (byte) (dpadPos.getValue() & 0b11111111);
    }

    int STICK_MIN = 0x00;
    int STICK_MAX = 0xff;

    int _a = Short.MIN_VALUE;
    int _b = Short.MAX_VALUE;
    int _a2 = STICK_MIN;
    int _b2 = STICK_MAX;

    public int transform(int v) {
        return (int) Math.ceil(((v - _a) / (_b - _a)) * (_b2 - _a2) + _a2);
    }

    public int scaleBetween(float unscaledNum, float minAllowed, float maxAllowed, float min, float max) {

        return Math.min(Math.max((int) (((unscaledNum + (-minAllowed)) / (maxAllowed + (-minAllowed))) * (max + (-min))), 1), 255);

    }

    public byte[] getState() {
        return state;
    }
}
