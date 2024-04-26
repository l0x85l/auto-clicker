package clicker;

import com.google.gson.JsonObject;
import com.sun.jna.platform.win32.WinDef;

public class Macro {

    public static void start() {
        JsonObject config = GUI.config;
        JsonObject autoClicker = config.getAsJsonObject("autoClicker");
        JsonObject autoThrow = config.getAsJsonObject("autoThrow");

        handleAutoClicker(autoClicker);
        handleAutoThrow(autoThrow);
    }

    private static void handleAutoClicker(JsonObject autoClicker) {
        if (autoClicker != null && autoClicker.get("enabled").getAsBoolean()) {
            String keybind = autoClicker.get("keybind").getAsString();
            if (Util.isPressed(Util.getKey(keybind))) {
                Attack(GUI.hwnd);
            }
        }
    }

    private static void handleAutoThrow(JsonObject autoThrow) {
        if (autoThrow != null && autoThrow.get("enabled").getAsBoolean()) {
            String keybind = autoThrow.get("keybind").getAsString();
            if (Util.isPressed(Util.getKey(keybind))) {
                Throw(GUI.hwnd, autoThrow);
            }
        }
    }

    private static void Throw(WinDef.HWND hwnd, JsonObject autoThrow) {
        char rodSlot = Integer.toString(autoThrow.get("selectedSlot").getAsInt()).charAt(0);
        Util.SendKey(rodSlot);
        Util.PostMessage(hwnd, 0x0204);
        Util.PostMessage(hwnd, 0x0205);
        Util.sleep(autoThrow.get("throwDelay").getAsInt());
        Util.SendKey('1');
    }

    private static void Attack(WinDef.HWND hwnd) {
        Util.PostMessage(hwnd, 0x0201);
        Util.PostMessage(hwnd, 0x0202);
        Util.sleep((long) Util.getCPS());
    }
}