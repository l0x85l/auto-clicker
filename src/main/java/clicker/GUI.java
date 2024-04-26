package clicker;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProIJTheme;
import com.formdev.flatlaf.ui.FlatSliderUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.istack.internal.NotNull;
import com.sun.jna.platform.win32.WinDef;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicSpinnerUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Map;

public class GUI {


    public static WinDef.HWND hwnd = Util.INSTANCE.FindWindow("LWJGL", null);

    private static final String CONFIG_FILE_PATH = "config.json";

    private static JPanel clickerPanel;
    private static JPanel throwPanel;

    private static JCheckBox clickerToggle;

    private static JSlider minCPSlider;

    private static JSlider maxCPSlider;

    private static JCheckBox throwToggle;

    private static JSpinner throwDelay;


    private static JButton selectedButton = null;

    private static JButton[] slots;

    private static int selectedSlotIndex = -1;
    private static boolean clickerListening = false;
    private static boolean throwListening = false;

    private static String autoClickerKey = "null";
    private static String autoThrowKey = "null";

    public static JsonObject config = config();


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatMonokaiProIJTheme());
            } catch (Exception ignored) {}
            if (hwnd == null) {
                JOptionPane.showMessageDialog(null, "no active process found for cube game!", Util.generateRandomString(12), JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            JFrame frame = new JFrame(Util.generateRandomString(12));
            frame.setIconImage(new ImageIcon(GUI.class.getResource("/spicy.png")).getImage());
            frame.setSize(450, 240);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getRootPane().setBorder(null);
            frame.setVisible(true);

            if (!new File(CONFIG_FILE_PATH).exists()) {
                JsonObject defaultConfig = createDefaultConfig();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (Writer writer = new FileWriter(CONFIG_FILE_PATH)) {
                    gson.toJson(defaultConfig, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            JsonObject autoClicker = config().getAsJsonObject("autoClicker");
            clickerToggle = new JCheckBox("enabled");
            clickerToggle.setFont(new Font("Verdana", Font.PLAIN, 9));
            clickerToggle.setBounds(20, 25, 70, 20);
            clickerToggle.setFocusPainted(false);
            clickerToggle.addActionListener(e -> saveConfig());
            frame.add(clickerToggle);


            JLabel minCPSLabel = new JLabel();
            minCPSLabel.setFont(new Font("Verdana", Font.PLAIN, 9));
            minCPSLabel.setBounds(20, 50, 100, 20);
            minCPSlider = addSlider(10, 70, 2, 20);
            minCPSlider.setValue(autoClicker.get("minCPS").getAsInt());
            minCPSLabel.setText("minimum cps [" + minCPSlider.getValue() + "]");
            frame.add(minCPSLabel);
            frame.add(minCPSlider);

            JLabel maxCPSLabel = new JLabel();
            maxCPSLabel.setFont(new Font("Verdana", Font.PLAIN, 9));
            maxCPSLabel.setBounds(20, 100, 110, 20);
            maxCPSlider = addSlider(10, 120, 2, 20);
            maxCPSlider.setValue(autoClicker.get("maxCPS").getAsInt());
            maxCPSLabel.setText("maximum cps [" + maxCPSlider.getValue() + "]");
            frame.add(maxCPSLabel);
            frame.add(maxCPSlider);

            minCPSlider.addChangeListener(e -> {
                int minCPSValue = minCPSlider.getValue();
                int maxCPSValue = maxCPSlider.getValue();
                if (minCPSValue > maxCPSValue) {
                    minCPSlider.setValue(maxCPSValue);
                }
                saveConfig();
                minCPSLabel.setText("minimum cps [" + minCPSlider.getValue() + "]");
            });

            maxCPSlider.addChangeListener(e -> {
                int minCPSValue = minCPSlider.getValue();
                int maxCPSValue = maxCPSlider.getValue();
                if (maxCPSValue < minCPSValue) {
                    maxCPSlider.setValue(minCPSValue);
                }
                saveConfig();
                maxCPSLabel.setText("maximum cps [" + maxCPSlider.getValue() + "]");
            });

            clickerPanel = new JPanel();
            clickerPanel.setFont(new Font("Verdana", Font.PLAIN, 25));
            TitledBorder autoClickerBorder = BorderFactory.createTitledBorder("auto clicker [" + autoClicker.get("keybind").getAsString() + "]");
            autoClickerBorder.setTitleColor(Color.WHITE);
            autoClickerBorder.setBorder(BorderFactory.createLineBorder(new Color(58, 59, 61)));
            autoClickerBorder.setTitleFont(new Font("Verdana", Font.PLAIN, 10));
            clickerPanel.setBorder(autoClickerBorder);
            clickerPanel.setBounds(10, 2, 200, 180);
            frame.add(clickerPanel);

            JPanel clickerListener = new JPanel();
            clickerListener.setFocusable(true);
            clickerListener.setBounds(5, 5, 80, 10);

            clickerListener.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!clickerListening) {
                        clickerListening = true;
                        clickerListener.requestFocus();
                    } else {
                        autoClickerKey = Util.convertButtonToString(e.getButton());
                        updateAutoClickerText(frame, Util.convertButtonToString(e.getButton()));
                        saveConfig();
                        clickerListening = false;
                    }
                }
            });

            clickerListener.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (clickerListening) {
                        autoClickerKey = getKeyCode(KeyEvent.getKeyText(e.getKeyCode()));
                        updateAutoClickerText(frame, getKeyCode(KeyEvent.getKeyText(e.getKeyCode())));
                        saveConfig();
                        clickerListening = false;
                    }
                }
            });

            frame.add(clickerListener);


            throwToggle = new JCheckBox("enabled");
            throwToggle.setFont(new Font("Verdana", Font.PLAIN, 9));
            throwToggle.setBounds(235, 25, 70, 20);
            throwToggle.setFocusPainted(false);
            throwToggle.addActionListener(e -> saveConfig());
            frame.add(throwToggle);

            JLabel throwDelayLabel = new JLabel("throw delay (ms)");
            throwDelayLabel.setFont(new Font("Verdana", Font.PLAIN, 9));
            throwDelayLabel.setBounds(235, 50, 100, 20);
            frame.add(throwDelayLabel);

            throwDelay = new JSpinner();
            throwDelay.setUI(new BasicSpinnerUI());
            throwDelay.setFocusable(false);
            throwDelay.setBounds(235, 75, 100, 20);
            throwDelay.addChangeListener(e -> saveConfig());
            frame.add(throwDelay);


            JsonObject autoThrow = config().getAsJsonObject("autoThrow");
            JLabel selectedSlotLabel = new JLabel();
            selectedSlotLabel.setText("selected slot [" + autoThrow.get("selectedSlot").getAsInt() + "]");
            selectedSlotLabel.setBounds(235, 110, 100, 20);
            frame.add(selectedSlotLabel);

            JPanel slotPanel = new JPanel(new GridLayout(1, 9));
            slotPanel.setBounds(235, 140, 180, 20);
            slots = new JButton[10];
            selectedSlotIndex = autoThrow.get("selectedSlot").getAsInt();
            for (int i = 1; i < slots.length; i++) {
                slots[i] = new JButton();
                slots[i].setOpaque(true);
                slots[i].setBorderPainted(false);
                int finalI = i;
                slots[i].addActionListener(e -> {
                    JButton clickedButton = (JButton) e.getSource();
                    if (selectedButton != null) {
                        selectedButton.setBackground(new Color(74, 71, 75));
                    }
                    selectedButton = clickedButton;
                    selectedButton.setBackground(new Color(255, 216, 102));
                    selectedSlotIndex = finalI;
                    selectedSlotLabel.setText("selected slot [" + selectedSlotIndex + "]");
                    saveConfig();
                });
                slotPanel.add(slots[i]);
            }
            frame.add(slotPanel);

            throwPanel = new JPanel();
            throwPanel.setFont(new Font("Verdana", Font.PLAIN, 25));
            TitledBorder autoThrowBorder = BorderFactory.createTitledBorder("auto throw [" + autoThrow.get("keybind").getAsString() + "]");
            autoThrowBorder.setTitleColor(Color.WHITE);
            autoThrowBorder.setBorder(BorderFactory.createLineBorder(new Color(58, 59, 61)));
            autoThrowBorder.setTitleFont(new Font("Verdana", Font.PLAIN, 10));
            throwPanel.setBorder(autoThrowBorder);
            throwPanel.setBounds(225, 2, 200, 180);
            frame.add(throwPanel);

            JPanel throwListener = new JPanel();
            throwListener.setFocusable(true);
            throwListener.setBounds(230, 5, 60, 10);
            throwListener.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!throwListening) {
                        throwListening = true;
                        throwListener.requestFocus();
                    }
                }
            });

            throwListener.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (throwListening) {
                        autoThrowKey = getKeyCode(KeyEvent.getKeyText(e.getKeyCode()));
                        updateAutoThrowText(frame, getKeyCode(KeyEvent.getKeyText(e.getKeyCode())));
                        saveConfig();
                        throwListening = false;
                    }
                }
            });

            frame.add(throwListener);
            frame.setLayout(null);
            loadConfig();
            new Thread(() -> {
                while (true) {
                    Macro.start();

                }
            }).start();
        });
    }



    @NotNull private static JsonObject config() {
        if (new File(CONFIG_FILE_PATH).exists()) {
            try (Reader reader = new FileReader(CONFIG_FILE_PATH)) {
                Gson gson = new Gson();
                return gson.fromJson(reader, JsonObject.class);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static void loadConfig() {
        try (Reader reader = new FileReader(CONFIG_FILE_PATH)) {
            Gson gson = new Gson();
            JsonObject data = gson.fromJson(reader, JsonObject.class);
            JsonObject autoClicker = data.getAsJsonObject("autoClicker");
            JsonObject autoThrow = data.getAsJsonObject("autoThrow");
            autoClickerKey = autoClicker.get("keybind").getAsString();
            clickerToggle.setSelected(autoClicker.get("enabled").getAsBoolean());
            minCPSlider.setValue(autoClicker.get("minCPS").getAsInt());
            maxCPSlider.setValue(autoClicker.get("maxCPS").getAsInt());
            autoThrowKey = autoThrow.get("keybind").getAsString();
            throwToggle.setSelected(autoThrow.get("enabled").getAsBoolean());
            throwDelay.setValue(autoThrow.get("throwDelay").getAsInt());
            selectedSlotIndex = autoThrow.get("selectedSlot").getAsInt();
            if (selectedSlotIndex >= 1 && selectedSlotIndex < slots.length) {
                selectedButton = slots[selectedSlotIndex];
                selectedButton.setBackground(new Color(255, 216, 102));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonObject createDefaultConfig() {
        JsonObject defaultConfig = new JsonObject();
        JsonObject autoClicker = new JsonObject();
        autoClicker.addProperty("keybind", "LMB");
        autoClicker.addProperty("enabled", false);
        autoClicker.addProperty("minCPS", 10);
        autoClicker.addProperty("maxCPS", 10);
        defaultConfig.add("autoClicker", autoClicker);
        JsonObject autoThrow = new JsonObject();
        autoThrow.addProperty("keybind", "X");
        autoThrow.addProperty("enabled", false);
        autoThrow.addProperty("throwDelay", 100);
        autoThrow.addProperty("selectedSlot", 1);
        defaultConfig.add("autoThrow", autoThrow);

        return defaultConfig;
    }


    private static void saveConfig() {
        JsonObject data = new JsonObject();
        JsonObject autoClicker = new JsonObject();
        JsonObject autoThrow = new JsonObject();
        autoClicker.addProperty("keybind", autoClickerKey);
        autoClicker.addProperty("enabled", clickerToggle.isSelected());
        autoClicker.addProperty("minCPS", minCPSlider.getValue());
        autoClicker.addProperty("maxCPS", maxCPSlider.getValue());
        autoThrow.addProperty("keybind", autoThrowKey);
        autoThrow.addProperty("enabled", throwToggle.isSelected());
        autoThrow.addProperty("throwDelay", (int) throwDelay.getValue());
        autoThrow.addProperty("selectedSlot", selectedSlotIndex);
        data.add("autoClicker", autoClicker);
        data.add("autoThrow", autoThrow);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(CONFIG_FILE_PATH)) {
            gson.toJson(data, writer);
        } catch (Exception ignored) {
        }
        config = config();
    }


    private static JSlider addSlider(int x, int y, int min, int max) {
        JSlider slider = new JSlider(min, max);
        slider.setFocusable(false);
        slider.setUI(new FlatSliderUI());
        slider.setBounds(x, y, 170, 20);
        return slider;
    }

    private static String getKeyCode(String key) {
        for (Map.Entry<String, Integer> e : Util.VK_CODES.entrySet()) {
            if (e.getKey().equalsIgnoreCase(key)) {
                return key;
            }
        }
        return "null";
    }

    private static void updateAutoClickerText(JFrame frame, String newText) {
        TitledBorder autoClickerBorder = BorderFactory.createTitledBorder("auto clicker [" + newText + "]");
        autoClickerBorder.setTitleColor(Color.WHITE);
        autoClickerBorder.setBorder(BorderFactory.createLineBorder(new Color(58, 59, 61)));
        autoClickerBorder.setTitleFont(new Font("Verdana", Font.PLAIN, 10));
        clickerPanel.setBorder(autoClickerBorder);
        frame.revalidate();
        frame.repaint();
    }


    private static void updateAutoThrowText(JFrame frame, String newText) {
        TitledBorder autoThrowBorder = BorderFactory.createTitledBorder("auto throw [" + newText + "]");
        autoThrowBorder.setTitleColor(Color.WHITE);
        autoThrowBorder.setBorder(BorderFactory.createLineBorder(new Color(58, 59, 61)));
        autoThrowBorder.setTitleFont(new Font("Verdana", Font.PLAIN, 10));
        throwPanel.setBorder(autoThrowBorder);
        frame.revalidate();
        frame.repaint();
    }
}