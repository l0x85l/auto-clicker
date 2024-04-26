package clicker;

import com.google.gson.JsonObject;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.util.HashMap;
import java.util.Random;

public class Util {

    public static final HashMap<String, Integer> VK_CODES = new HashMap<>();

    public static User32 INSTANCE = User32.INSTANCE;

    static {
        VK_CODES.put("A", 0x41);
        VK_CODES.put("B", 0x42);
        VK_CODES.put("C", 0x43);
        VK_CODES.put("D", 0x44);
        VK_CODES.put("E", 0x45);
        VK_CODES.put("F", 0x46);
        VK_CODES.put("G", 0x47);
        VK_CODES.put("H", 0x48);
        VK_CODES.put("I", 0x49);
        VK_CODES.put("J", 0x4A);
        VK_CODES.put("K", 0x4B);
        VK_CODES.put("L", 0x4C);
        VK_CODES.put("M", 0x4D);
        VK_CODES.put("N", 0x4E);
        VK_CODES.put("O", 0x4F);
        VK_CODES.put("P", 0x50);
        VK_CODES.put("Q", 0x51);
        VK_CODES.put("R", 0x52);
        VK_CODES.put("S", 0x53);
        VK_CODES.put("T", 0x54);
        VK_CODES.put("U", 0x55);
        VK_CODES.put("V", 0x56);
        VK_CODES.put("W", 0x57);
        VK_CODES.put("X", 0x58);
        VK_CODES.put("Y", 0x59);
        VK_CODES.put("Z", 0x5A);
        VK_CODES.put("LMB", 0x01);
        VK_CODES.put("RMB", 0x02);
        VK_CODES.put("MMB", 0x04);
    }

    public static int getKey(String key) {
        return VK_CODES.getOrDefault(key, 1337);
    }

    public static boolean isPressed(int keyCode) {
        return (INSTANCE.GetAsyncKeyState(keyCode) & 0x8000) != 0;
    }

    public static void PostMessage(WinDef.HWND hwnd, int message) {
        INSTANCE.PostMessage(hwnd, message, null, null);
    }

    public static void SendKey(char key) {
        WinUser.INPUT input = new WinUser.INPUT();

        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wScan = new WinDef.WORD(0);
        input.input.ki.time = new WinDef.DWORD(0);
        input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);

        input.input.ki.wVk = new WinDef.WORD(key);
        input.input.ki.dwFlags = new WinDef.DWORD(0);
        INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
        input.input.ki.wVk = new WinDef.WORD(key);
        input.input.ki.dwFlags = new WinDef.DWORD(2);
        INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
    }


    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static float randomCPS(float min, float max) {
        Random random = new Random();
        return random.nextFloat() * (max - min) + min;
    }


    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static String convertButtonToString(int button) {
        switch (button) {
            case 1:
                return "LMB";
            case 2:
                return "MMB";
            case 3:
                return "RMB";
            case 4:
                return "MB4";
            case 5:
                return "MB5";
            default:
                return "Unknown";
        }
    }

    public static float getCPS() {
        JsonObject autoClicker = GUI.config.getAsJsonObject("autoClicker");
        if(autoClicker != null) {
            float cps = randomCPS(autoClicker.get("minCPS").getAsInt(), autoClicker.get("maxCPS").getAsInt());
            return ((1000.0f / cps * 0.1f) * 10.0f);
        }
        return 1;
    }
}
