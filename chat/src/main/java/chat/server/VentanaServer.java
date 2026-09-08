package chat.server;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;

public class VentanaServer extends JFrame {

    private final String DEFAULT_PORT = "666";
    private final String DEFAULT_DATA_PORT = "777";
    private final Servidor server;

    private JScrollPane scrollPanel;
    private JTextArea txtClientes;

    public VentanaServer() {
        crearComponentes();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String[] puertos = getPorts();
        String puerto = puertos[0];
        String puertoDatos = puertos[1];
        this.server = new Servidor(puerto, puertoDatos, this);
    }

    private void crearComponentes() {
        this.scrollPanel = new JScrollPane();
        this.txtClientes = new JTextArea();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("### Servidor ###");

        this.scrollPanel.setBorder(BorderFactory.createTitledBorder("Log del Servidor"));

        this.txtClientes.setEditable(false);
        this.txtClientes.setColumns(20);
        this.txtClientes.setRows(5);
        this.txtClientes.setWrapStyleWord(true);
        this.txtClientes.setLineWrap(true);
        this.scrollPanel.setViewportView(this.txtClientes);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(scrollPanel, GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
            .addContainerGap())
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(this.scrollPanel, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();

    }

    private String[] getPorts() {
        String[] puertos = new String[2];
        String puerto = DEFAULT_PORT;
        String puertoDatos = DEFAULT_DATA_PORT;

        JTextField portInput = new JTextField(20);
        portInput.setText(puerto);
        JTextField dataPortInput = new JTextField(20);
        dataPortInput.setText(puertoDatos);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,2));
        panel.add(new JLabel("Puerto de conexion: "));
        panel.add(portInput);
        panel.add(new JLabel("Puerto de transmision: "));
        panel.add(dataPortInput);
        
        int resultado = JOptionPane.showConfirmDialog(null, panel, "Configuracion de la comunicacion", JOptionPane.OK_CANCEL_OPTION);
        
        if (resultado == JOptionPane.OK_OPTION) {
            puertos[0] = portInput.getText();
            puertos[1] = dataPortInput.getText();
        } else {
            System.exit(0);
        }

        return puertos;
    }

    void addRunningServer() {
        this.txtClientes.setText("Iniciando Servidor... [Ok]");
    }

    void addLog(String txt) {
        this.txtClientes.append(txt);
    }

}
