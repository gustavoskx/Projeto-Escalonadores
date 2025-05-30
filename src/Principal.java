import gui.JanelaPrincipal;

import javax.swing.*;

/**
 * Classe principal que inicia a aplicação de simulação de escalonamento de processos.
 */
public class Principal {
    public static void main(String[] args) {
        // Configura o look and feel para parecer com o sistema operacional
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inicia a aplicação na thread de eventos do Swing
        SwingUtilities.invokeLater(() -> {
            JanelaPrincipal janelaPrincipal = new JanelaPrincipal();
            janelaPrincipal.setVisible(true);
        });
    }
}