package chat;

import java.awt.EventQueue;
import javax.swing.UIManager;
import chat.server.VentanaServer;

public class MainServidor {
    public static void main(String[] args) {
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    // UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                } catch (Exception e) {
                    System.err.println("Error al cargar libreria de Swing: " + e.getMessage());
                }
                new VentanaServer().setVisible(true);
            }
        });
    }
}
