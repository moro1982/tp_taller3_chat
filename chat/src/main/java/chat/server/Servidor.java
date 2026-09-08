package chat.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Servidor extends Thread {
    private ServerSocket serverSocketMensajes;
    private ServerSocket serverSocketArchivos;
    LinkedList<HiloCliente> clientes;   // Lista enlazada de HiloClientes
    private VentanaServer ventanaS;
    private String puertoMensajes;
    private String puertoDatos;
    private final String FILES_RCV_SND = "./chat/src/main/java/chat/server/archivosRecibidos/";
    static int indexCliente;

    public Servidor(String puertoMensajes, String puertoDatos, VentanaServer ventServ) {
        this.puertoMensajes = puertoMensajes;
        this.puertoDatos = puertoDatos;
        this.clientes = new LinkedList<>();
        this.ventanaS = ventServ;
        this.start();
    }

    @Override
    public void run() {
        try {
            this.serverSocketMensajes = new ServerSocket(Integer.valueOf(puertoMensajes));
            this.serverSocketArchivos = new ServerSocket(Integer.valueOf(puertoDatos));
            this.ventanaS.addRunningServer();
            while (true) {
                HiloCliente hCliente;
                Socket cMsgSocket;
                Socket cDataSocket;

                cMsgSocket = this.serverSocketMensajes.accept();
                System.out.println("Ingresando nueva conexion: " + cMsgSocket.getPort());
                cDataSocket = this.serverSocketArchivos.accept();
                System.out.println("Abriendo nuevo canal de transmision: " + cDataSocket.getPort());
                
                hCliente = new HiloCliente(this, cMsgSocket, cDataSocket);
                hCliente.start();
            }
        } catch (Exception e) {
            System.out.println("El servidor no ha podido iniciarse.\n La aplicacion se cerrara.");
            System.exit(0);
        }
    }

    LinkedList<String> getUsuariosConectados() {
        LinkedList<String> usuariosConectados = new LinkedList<>();
        clientes.stream()
                .forEach(c -> usuariosConectados.add(c.getIdentificador()));
        return usuariosConectados;
    }

    public String getFILES_RCV_SND() {
        return FILES_RCV_SND;
    }

    void agregarLog(String text) {
        ventanaS.addLog(text);
    }
}
