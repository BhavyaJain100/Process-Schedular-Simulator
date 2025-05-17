// Main.java
import javax.swing.*;
import GUI.SchedulerUI;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SchedulerUI ui = new SchedulerUI();
            ui.setVisible(true);
        });
    }
}