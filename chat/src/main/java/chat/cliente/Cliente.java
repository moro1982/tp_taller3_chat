package chat.cliente;

import java.net.Socket;
import java.util.LinkedList;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

public class Cliente extends Thread {
    
    private String id;
    private final String host;
    private final int puerto;
    private final int puertoDatos;
    // Ruta local de carpeta de archivos recibidos por el Cliente
    private final String FILES_RCV = "./chat/src/main/java/chat/cliente/archivosRecibidos/";
    
    private Socket socket;
    private Socket socketDatos;
    private VentanaCliente ventanaC;

    private String rutaArchivoAdjunto;
    private String rutaArchivoRecibido;
    private byte[] buffer;
    
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private boolean escuchando;

    public Cliente(String host, Integer puerto, Integer puertoDatos, String idCliente, VentanaCliente ventCli) {
        this.host = host;
        this.puerto = puerto;
        this.puertoDatos = puertoDatos;
        this.id = idCliente;
        this.ventanaC = ventCli;
        this.rutaArchivoAdjunto = "";
        escuchando = true;
        this.start();
    }

    @Override
    public void run() {
        try {
            this.socket = new Socket(host, puerto);
            this.socketDatos = new Socket(host, puertoDatos);
            this.os = new ObjectOutputStream(this.socket.getOutputStream());
            this.is = new ObjectInputStream(this.socket.getInputStream());
            
            System.out.println("Conexion exitosa!");
            this.enviarSolicitudConexion(id);
            this.escuchar();
        } catch (UnknownHostException e) {
            System.out.println("Conexion rechazada, servidor desconocido,\n" + 
                                "IP incorrecta, o el servidor no esta corriendo.\n" + 
                                "La aplicacion se cerrara.\n");
            System.exit(0);
        } catch (IOException ex) {
            System.out.println("Conexion rechazada, error de Entrada/Salida,\n" + 
                               "IP o puerto incorrecto, o el servidor no esta corriendo.\n" + 
                               "La aplicacion se cerrara.\n");
            System.exit(0);
        }
    }

    public void desconectar() {
        try {
            os.close();
            is.close();
            socket.close();
            socketDatos.close();
            escuchando = false;
            System.out.println("El cliente " + this.id + " se ha desconectado.\n");
        } catch (Exception e) {
            System.out.println("Error en cierre de comunicacion del Cliente");
        }
    }

    // Escucha el InputStream por listas de objetos con diversos mensajes
    public void escuchar() {
        try {
            while (escuchando) {
                Object auxIS = is.readObject();
                if (auxIS != null) {
                    if (auxIS instanceof LinkedList) {
                        ejecutar((LinkedList<String>) auxIS);
                    } else {
                        System.err.println("Objeto desconocido recibido a traves del socket.");
                    }
                } else {
                    System.err.println("Objeto nulo recibido a traves del socket.");
                }
            }
        } catch (Exception e) {
            System.out.println("Se ha perdido la comunicacion con el servidor.\n" + "El chat y la aplicacion se cerraran.");
            System.exit(0);
        }
    }

    /* Ejecuta una accion de acuerdo al mensaje recibido desde el Servidor */

    // lista[0] --> Tipo
    // lista[1] --> id emisor
    // lista[2] --> id destinatario
    // lista[3] --> mensaje
    // lista[4] --> nombre archivo
    
    public void ejecutar(LinkedList<String> lista) {
        String tipo = lista.get(0);
        switch (tipo) {
            case "CONNECTION_ACCEPTED":
                this.id = lista.get(1);
                this.ventanaC.tituloUsuario(id);
                for (int i = 2; i < lista.size(); i++) {
                    ventanaC.agregarContacto( lista.get(i) );
                }
                break;
            case "NEW_USER_CONNECTED":
                this.ventanaC.agregarContacto( lista.get(1) );
                System.out.println( "Contacto agregado: " + lista.get(1) );
                break;
            case "USER_DISCONNECTED":
                this.ventanaC.eliminarContacto( lista.get(1) );
                System.out.println( "Contacto eliminado: " + lista.get(1) );
                break;
            case "MESSAGE":
                this.ventanaC.agregarMensaje( lista.get(1), lista.get(3) );
                System.out.println( lista.get(1) + " dice: " + lista.get(3) );
                // Si hay archivo, lo recibo
                if ( lista.size() > 4 ) {
                    System.out.println("Archivo a recibir: " + lista.get(4));
                    String recibido = this.recibirArchivo();
                    if (recibido == null) {
                        System.out.println(" */* Error en la recepcion del archivo. */*");
                    } else {
                        System.out.println("Archivo recibido exitosamente por el Cliente.");
                        this.ventanaC.agregarArchivoRecibido(lista.get(1), recibido);
                    }
                }
                break;

            default:
                break;
        }
    }


    /* METODOS DE SALIDA */

    private void enviarSolicitudConexion(String id) {
        LinkedList<String> lista = new LinkedList<>();
        lista.add("CONNECT_REQ");
        lista.add(id);
        try {
            os.writeObject(lista);
        } catch (IOException e) {
            System.out.println("Error de lectura/escritura al enviar mensaje al Servidor.");
        }
    }

    public void confirmarDesconexion() {
        LinkedList<String> lista = new LinkedList<>();
        lista.add("DISCONNECT_REQ");
        lista.add(id);
        try {
            os.writeObject(lista);
        } catch (IOException e) {
            System.out.println("Error de lectura/escritura al enviar mensaje al Servidor.");
        }
    }

    public void enviarMensaje(String cliente_destino, String mensaje) {
        LinkedList<String> lista = new LinkedList<>();
        lista.add("MESSAGE");
        lista.add(id);
        lista.add(cliente_destino);
        lista.add(mensaje);

        // Si se ha seleccionado un archivo adjunto, se agrega a la lista de objetos a enviar
        if ( !(this.rutaArchivoAdjunto.equals("")) ) {
            /* Enviar archivo */
            lista.add(this.rutaArchivoAdjunto);
            if(lista.size() > 4) {
                System.out.println("Enviando archivo " + lista.get(4) + " al cliente " + lista.get(2));
                
                boolean enviado = this.enviarArchivo(this.rutaArchivoAdjunto);
                if (enviado) {
                    System.out.println("Archivo enviado con exito.");
                } else {
                    System.out.println(" */* Error en la transmision del archivo. */*");
                }
            }
        }

        // Enviamos el mensaje
        try {
            os.writeObject(lista);
        } catch (IOException e) {
            System.out.println("** Error de lectura/escritura al enviar mensaje al Servidor **");
        }
    }

    private boolean enviarArchivo(String rutaArchivo) {
        try {
            System.out.println("Iniciando envio de archivo desde el Cliente...");

            DataOutputStream dos = new DataOutputStream(this.socketDatos.getOutputStream());
            FileInputStream fis = new FileInputStream(rutaArchivo);
            File localFile = new File(rutaArchivo);
            
            // Enviamos el nombre del archivo
            dos.writeUTF(localFile.getName());
            
            // Enviamos el tamanyo del archivo
            dos.writeLong(localFile.length());
            
            // Enviamos el archivo
            int bytes = 0;
            this.buffer = new byte[8*1024];
            while( (bytes = fis.read(this.buffer)) != -1 ) {
                dos.write(this.buffer, 0, bytes);
            }
            dos.flush();
            fis.close();
            
            System.out.println("Finalizando envio de archivo desde el Cliente.");
            return true;
        } catch (Exception e) {
            System.err.println("Error durante el envio de archivo desde el Cliente: " + e.getMessage());
        }
        return false;
    }


    /* METODOS DE ENTRADA */
    
    private String recibirArchivo() {
        try {
            System.out.println("Iniciando recepcion del archivo del lado del Cliente...");

            DataInputStream dis = new DataInputStream(this.socketDatos.getInputStream());
            
            // Recibimos la ruta origen del archivo y creamos la ruta local
            String rutaOrigen = dis.readUTF();
            System.out.println("Ruta del archivo origen: " + rutaOrigen);
            String nombreRecibido = rutaOrigen.substring( rutaOrigen.lastIndexOf("/") + 1 );
            String rutaArchivoLocal = this.FILES_RCV + nombreRecibido;
            System.out.println("Ruta local del archivo copiado: " + rutaArchivoLocal);
            this.setNombreArchivoRecibido(rutaArchivoLocal);
            FileOutputStream fos = new FileOutputStream(rutaArchivoLocal);
            
            // Recibimos tamaño del archivo
            long fileSize = dis.readLong();
            
            // Recibimos datos del archivo 
            int bytes;
            byte[] buffer = new byte[8 * 1024];
            while (fileSize > 0 && (bytes = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fos.write(buffer, 0, bytes);
                fileSize -= bytes;
            }
            fos.flush();
            fos.close();
            
            System.out.println("Archivo recibido por el Cliente: " + this.rutaArchivoRecibido);
            return this.rutaArchivoRecibido;
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    /* GETTERS & SETTERS */

    String getIdentificador() {
        return this.id;
    }

    public String getFILES_RCV() {
        return FILES_RCV;
    }

    public String getArchivoAdjunto() {
        return this.rutaArchivoAdjunto;
    }

    public void setArchivoAdjunto(String rutaArchivo) {
        this.rutaArchivoAdjunto = rutaArchivo;
    }

    public String getNombreArchivoRecibido() {
        return this.rutaArchivoRecibido;
    }

    public void setNombreArchivoRecibido(String rutaArchivo) {
        this.rutaArchivoRecibido = rutaArchivo;
    }
}
