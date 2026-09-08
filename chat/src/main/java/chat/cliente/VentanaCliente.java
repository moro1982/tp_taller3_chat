package chat.cliente;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class VentanaCliente extends JFrame {
    
    private final String DEFAULT_HOST = "localhost";
    private final String DEFAULT_PORT = "666";
    private final String DEFAULT_DATA_PORT = "777";
    private final Cliente cliente;
    
    private JButton btnArchivo;
    private JButton btnEnviar;
    private JComboBox<String> contactos;
    private JScrollPane scrollPanel;
    private JTextArea txtHistorial;
    private JTextField txtMensaje;
    private JTextField txtArchivo;
    private JLabel etiqueta;

    public VentanaCliente() {
        crearComponentes();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        String host_port_data_name[] = getHostPortDataName();
        String host = host_port_data_name[0];
        String port = host_port_data_name[1];
        String dataPort = host_port_data_name[2];
        String userName = host_port_data_name[3];
        cliente = new Cliente(host, Integer.valueOf(port), Integer.valueOf(dataPort), userName, this);
    }

    private String[] getHostPortDataName() {
        String[] data = new String[4];
        data[0] = DEFAULT_HOST;
        data[1] = DEFAULT_PORT;
        data[2] = DEFAULT_DATA_PORT;
        JTextField campoHost = new JTextField(25);
        JTextField campoPort = new JTextField(25);
        JTextField campoDataPort = new JTextField(25);
        JTextField campoNombre = new JTextField(25);
        campoHost.setText(DEFAULT_HOST);
        campoPort.setText(DEFAULT_PORT);
        campoDataPort.setText(DEFAULT_DATA_PORT);
        campoNombre.setText("Nombre_Usuario");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4,2));
        panel.add(new JLabel("IP del Servidor: "));
        panel.add(campoHost);
        panel.add(new JLabel("Puerto de conexion: "));
        panel.add(campoPort);
        panel.add(new JLabel("Puerto de transmision: "));
        panel.add(campoDataPort);
        panel.add(new JLabel("Nombre de Usuario"));
        panel.add(campoNombre);
        
        int config = JOptionPane.showConfirmDialog(null, panel, "Configurar comunicación", JOptionPane.OK_CANCEL_OPTION);

        if (config == JOptionPane.OK_OPTION) {
            data[0] = campoHost.getText();
            data[1] = campoPort.getText();
            data[2] = campoDataPort.getText();
            data[3] = campoNombre.getText();
        } else {
            System.exit(0);
        }

        return data;
    }
    
    private void crearComponentes() {
        this.scrollPanel = new JScrollPane();
        this.txtHistorial = new JTextArea();
        this.etiqueta = new JLabel();
        this.contactos = new JComboBox<String>();
        this.txtMensaje = new JTextField();
        this.txtArchivo = new JTextField();
        this.btnArchivo = new JButton();
        this.btnEnviar = new JButton();

        /* Boton Cerrar (X) */
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                formWindowClosed(e);
            }
            public void windowClosing(WindowEvent e) {
                formWindowClosing(e);
            }
        });

        txtHistorial.setEditable(false);
        txtHistorial.setColumns(20);
        txtHistorial.setRows(5);
        txtHistorial.setWrapStyleWord(true);
        txtHistorial.setLineWrap(true);
        
        scrollPanel.setViewportView(txtHistorial);
        
        /* Botones */
        btnEnviar.setText("Enviar");
        btnEnviar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnEnviarActionPerformed(e);
            }
        });

        btnArchivo.setText("Examinar...");
        btnArchivo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnArchivoActionPerformed(e);
            }
        });

        etiqueta.setText("Destinatario: ");

        
        /** DESIGN MANAGER - GroupLayout **/

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtArchivo)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnArchivo)
                    )
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtMensaje)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnEnviar))
                    .addComponent(scrollPanel)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(etiqueta)
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(this.contactos, GroupLayout.PREFERRED_SIZE, 198, GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(this.scrollPanel, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(this.contactos, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(etiqueta)
                )
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(txtArchivo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnArchivo)
                )
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(txtMensaje, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEnviar)
                )
                .addContainerGap())
        );

        pack();
    }

    /* Acciones al cerrar la ventana ppal */
    private void formWindowClosed(WindowEvent e) {
    }
    private void formWindowClosing(WindowEvent e) {
        cliente.confirmarDesconexion();
    }

    /* Acciones del boton Enviar */
    private void btnEnviarActionPerformed(ActionEvent e) {
        if (this.contactos.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un destinatario valido.\n Si no hay uno, aguarde a que se conecte algun usuario.\n", DEFAULT_HOST, ABORT);
            return;
        }
        String cliente_destino = this.contactos.getSelectedItem().toString();
        String mensaje = this.txtMensaje.getText();
        cliente.enviarMensaje(cliente_destino, mensaje);

        String emojiSndMsg = new String(new int[] {0x1F4E8}, 0, 1);
        this.txtHistorial.append("## YO  " + emojiSndMsg + "  '" + cliente_destino + "' ## : \n" + mensaje + "\n");
        
        String emojiSndFile = new String(new int[] {0x1F4E4}, 0, 1);
        String emojiRightArrow = new String(new int[] {0x27A1}, 0, 1);
        if ( !(this.txtArchivo.getText().equals("")) ) {
            this.txtHistorial.append("## YO  " + emojiSndFile + "  '" + cliente_destino + "' ## : \nArchivo enviado  " + emojiRightArrow + "  " + this.txtArchivo.getText() + "\n");
        }
        // Reseteamos valores
        this.txtMensaje.setText("");
        this.txtArchivo.setText("");
        this.cliente.setArchivoAdjunto("");
    }

    /* Acciones del boton Examinar */
    private void btnArchivoActionPerformed(ActionEvent e) {
        JFileChooser seleccionArchivo = new JFileChooser();
        seleccionArchivo.setFileSelectionMode(JFileChooser.FILES_ONLY);
        seleccionArchivo.showOpenDialog(null);
        if ( !(seleccionArchivo.getSelectedFile().equals(null)) ) {
            File archivoSeleccionado = seleccionArchivo.getSelectedFile();
            this.cliente.setArchivoAdjunto(archivoSeleccionado.getAbsolutePath());
            this.txtArchivo.setText(archivoSeleccionado.getName());
            System.out.println("Archivo adjuntado al Cliente: " + this.txtArchivo.getText());
        }
    }

    void agregarContacto(String contacto) {
        this.contactos.addItem(contacto);
    }

    void agregarMensaje(String emisor, String mensaje) {
        txtHistorial.append("#### " + emisor + " #### : \n" + mensaje + "\n");
    }

    void agregarArchivoRecibido(String emisor, String nombreArchivo) {
        String emojiRcvFile = new String(new int[] {0x1F4E5}, 0, 1);
        txtHistorial.append("#### " + emisor + " #### ha enviado el archivo  " + emojiRcvFile + "  " + nombreArchivo + "\n");
    }

    void tituloUsuario(String id) {
        this.setTitle(" --- " + id + " --- ");
    }

    void eliminarContacto(String id) {
        for (int i = 0; i < this.contactos.getItemCount(); i++) {
            if (this.contactos.getItemAt(i).toString().equals(id)) {
                this.contactos.removeItemAt(i);
                return;
            }
        }
    }
}
