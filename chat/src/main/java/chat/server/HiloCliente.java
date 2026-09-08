package chat.server;

import java.net.Socket;
import java.util.LinkedList;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.Thread;

public class HiloCliente extends Thread {
    private final Servidor server;
    private final Socket cMsgSocket;
    private final Socket cDataSocket;
    private String id;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private boolean escuchando;
    private String rutaArchivoRecibido;
    
    public HiloCliente( Servidor server, Socket cMsgSocket, Socket cDataSocket ) {
        this.server = server;
        this.cMsgSocket = cMsgSocket;
        this.cDataSocket = cDataSocket;
    }

    @Override
    public void run() {
        try {
            this.os = new ObjectOutputStream(this.cMsgSocket.getOutputStream());
            this.is = new ObjectInputStream(this.cMsgSocket.getInputStream());
            this.escuchar();
        } catch (Exception e) {
            System.out.println("Error al realizar lectura del hilo del cliente");
        }
        desconectar();
    }
    
    public void escuchar() {
        escuchando = true;
        while (escuchando) {
            try {
                Object auxIS = is.readObject();
                if (auxIS instanceof LinkedList) {
                    ejecutar((LinkedList<String>) auxIS);
                }
            } catch (Exception e) {
                System.out.println("Error al leer envio del cliente");
                this.desconectar();
            }
        }
    }
    
    public void desconectar() {
        try {
            cMsgSocket.close();
            cDataSocket.close();
            escuchando = false;
        } catch (IOException e) {
            System.out.println("Error al cerrar comunicacion con el cliente");
        }
    }
    
    public void ejecutar(LinkedList<String> lista) {
        String tipo = lista.get(0);

        switch (tipo) {
            case "CONNECT_REQ":
                confirmarConexion( lista.get(1) );
                break;
            case "DISCONNECT_REQ":
                confirmarDesconexion();
                break;
            case "MESSAGE":
                String destinatario = lista.get(2);
                server.clientes
                    .stream()
                    .filter(h -> (destinatario.equals(h.getIdentificador())))
                    .forEach((h) -> {
                        h.enviarMensaje(lista);
                        
                        // Aqui llamamos al HiloTransfer para recibir el archivo (si lo hay)
                        if ( lista.size() > 4 ) {
                            HiloTransfer hRecepcion = new HiloTransfer(this.cDataSocket, this.server.getFILES_RCV_SND());
                            hRecepcion.start();
                            
                            // Aguardamos que finalice el HiloTransfer receptor
                            try {
                                hRecepcion.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            File archivosRecibidos = new File(server.getFILES_RCV_SND());
                            File[] archivosGuardados = archivosRecibidos.listFiles();

                            for (File archivo : archivosGuardados) {
                                if (archivo.getName().contains("_copia")) {
                                    System.out.println(archivo.getName());
                                    this.setRutaArchivoRecibido(server.getFILES_RCV_SND() + archivo.getName());
                                }
                            }
                            System.out.println(this.getRutaArchivoRecibido());

                            System.out.println("Archivo a reenviar desde hilo: " + this.rutaArchivoRecibido);
                            
                            // Ahora, enviamos el archivo recibido hacia el Cliente destino.
                            HiloTransfer hEnvio = new HiloTransfer(h.cDataSocket, this.rutaArchivoRecibido, true );
                            hEnvio.start();
                            // Aguardamos que finalice el HiloTransfer emisor
                            try {
                                hEnvio.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // Una vez enviado, borramos el archivo copiado en el servidor
                            File tempFile = new File(this.rutaArchivoRecibido);
                            tempFile.delete();
                        }
                    });
                break;
            default:
                break;
        }
    }

    public String getIdentificador() {
        return id;
    }

    private void confirmarConexion(String id) {
        Servidor.indexCliente++;
        this.id = Servidor.indexCliente + " - " + id;

        // Estructura del mensaje (tipo - id - ids usuarios conectados)
        LinkedList<String> lista = new LinkedList<>();
        lista.add("CONNECTION_ACCEPTED");
        lista.add(this.id);
        lista.addAll(server.getUsuariosConectados());

        // Enviamos mensaje de confirmacion al cliente
        this.enviarMensaje(lista);
        server.agregarLog("\nNuevo Cliente: " + this.id + "\n");

        // Estructura de mensaje broadcast (tipo - id)
        LinkedList<String> auxLista = new LinkedList<>();
        auxLista.add("NEW_USER_CONNECTED");
        auxLista.add(this.id);
        
        // Aviso de nueva conexion a c/u de los otros clientes
        server.clientes
                .stream()
                .forEach(cliente -> cliente.enviarMensaje(auxLista));
        
        // Agrego nuevo cliente a la lista 
        server.clientes.add(this);
    }

    private void confirmarDesconexion() {
        // Estructura mensaje de desconexion (tipo - id)
        LinkedList<String> auxLista = new LinkedList<>();
        auxLista.add("USER_DISCONNECTED");
        auxLista.add(this.id);
        
        this.server.agregarLog("\nEl Cliente \"" + this.id + "\" se ha desconectado.");
        this.desconectar();
        
        // Busco y elimino al HiloCliente correspondiente
        for(int i = 0; i < this.server.clientes.size(); i++) {
            if(this.server.clientes.get(i).equals(this)) {
                this.server.clientes.remove(i);
                break;
            }
        }
        
        // Aviso de desconexion a c/u de los otros clientes
        server.clientes
                .stream()
                .forEach(h -> h.enviarMensaje(auxLista));
    }

    private void enviarMensaje(LinkedList<String> lista) {
        try {
            os.writeObject(lista);
        } catch (Exception e) {
            System.out.println("Error en el envio del objeto al cliente");
        }
    }

    public String getRutaArchivoRecibido() {
        return this.rutaArchivoRecibido;
    }

    public void setRutaArchivoRecibido(String rutaArchivo) {
        this.rutaArchivoRecibido = rutaArchivo;
    }
}
