# PROYECTO SIMULADOR DE CHAT MULTI-CLIENTE

## Introduccion

El proyecto consiste en un simulador de chat multi-usuario, con un Servidor activo a la espera de conexiones de Clientes. Los Clientes pueden, a traves de la conexion con el Servidor, enviar mensajes y archivos a otro Cliente, todo administrado desde una interfaz grafica tanto para Servidor como para cada Cliente.

## Descripcion de la estructura del sistema

### Servidor

El Servidor se encargara fundamentalmente de establecer, mantener y finalizar la conexion con los Clientes. Las demas tareas de interaccion con los Clientes se delegan a otras entidades, que veremos enseguida.

El Servidor se constituye de una clase homonima de tipo Thread, que posee 2 ServerSockets diferentes, uno para el envio de mensajes y otro para el envio de datos de archivos. Al correr el hilo, se crean los ServerSockets y se intenta crear un HiloCliente.

### HiloCliente

Esta clase (tambien de tipo Thread) sera la encargada de administrar la conexion entre el Servidor y cada Cliente, creandose un HiloCliente por cada nueva solicitud de conexion que se reciba. Estos Hilos se iran almacenando en una lista enlazada de HiloClientes ubicada en la instancia del Servidor.

El HiloCliente, como dijimos, administra todas las acciones inherentes a la conexion con los Clientes (solicitud de conexion y desconexion, envio de mensajes, envio de archivos, etc.). Para ello, trae consigo las referencias a los Sockets del Servidor correspondientes a su Cliente.

Para administrar la relacion Cliente-Servidor, HiloCliente posee una funcion "escuchar()", que aguardara aguardara la recepcion de un Objeto (una lista enlazada) con toda la informacion de la accion a realizarse, el cual es enviado por el Cliente correspondiente.

La funcion "ejecutar()" sera la encargada de decodificar la lista enviada (el primer elemento es el tipo de accion), y ordena todas las acciones necesarias para su ejecucion (envio de un mensaje de confirmacion al Cliente, registrar nuevo Cliente en el Servidor, notificar a los Clientes ya conectados, reenviar un mensaje recibido al Cliente destino, administrar la desconexion de Clientes, etc.)

### HiloTransfer

Por ultimo, el envio de archivos se realiza en un Hilo especialmente creado para la transferencia de datos del archivo, llamado HiloTransfer. Este Hilo -que es llamado desde el HiloCliente cuando detecta la orden de envio de archivo en el Objeto recibido-, posee 2 funciones casi excluyentes: "enviarArchivo()" y "recibirArchivo()". La segunda recibira el archivo desde el Cliente y lo almacenara en una carpeta dedicada a ello. La primera, reenvia dicho archivo recibido (con su nombre modificado) al Cliente destino.

### Cliente

La clase Cliente (tambien de tipo Thread) permite realizar todas las acciones requeridas para el envio de mensajes y archivos a los demas Clientes. Posee 2 sockets (1 para mensajes y otro para archivos), datos de host y puertos para los mismos, un campo para almacenar una ruta de archivo adjunto, entre los atributos mas importantes.

Cuando corre, tambien escucha por Objetos o datos provenientes del Servidor u otros Clientes (reenviados a traves del HiloCliente o el HiloTransfer, segun se trate). Los Objetos tambien se decodificaran en una funcion "ejecutar()", a fin de realizar las acciones requeridas (agregar un nuevo contacto conectado, quitar un contacto desconectado, agregar el mensaje recibido al registro de la ventana, o recibir un archivo).

### VentanaServer y VentanaCliente

Estas clases son las encargadas de crear y administrar la interfaz grafica necesaria para la interaccion con las instancias de Servidor y Clientes. En cada una se definen los componentes requeridos por Cliente o Servidor, segun el caso, asi como las funciones que disparan los eventos de los botones y campos de texto.

