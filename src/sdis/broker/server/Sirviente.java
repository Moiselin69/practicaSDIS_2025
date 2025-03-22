package sdis.broker.server;

import sdis.broker.common.MensajeProtocolo;
import sdis.broker.common.Primitiva;
import sdis.utils.MultiMap;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


class Sirviente implements Runnable {
    private Socket socket; // Es el socket con el que se haya aceptado la conexion al servidor
    private ConcurrentHashMap<String, String> usuariosHashMap;  // Aqui se guardan los usuarios con las contraseñas
    private ConcurrentHashMap<Integer, Sirviente> mapaSirvientes; // Hay que pasar un mapa de los sirvientes que ha lanzado el servidor, sirve para depurar
    private ConcurrentHashMap<String, ContadorAddRead> mapaMensajesAddRead; // Aqui van los datos de depuracion de las diferentes colas
    private BlackListManager listaIps; // Aqui se guarda las direcciones Ip conectadas al servidor
    private BlackListManager listaLoginsIncorrectos; // Aqui se guarda las direcciones Ip que han fallado el login
    private MultiMap multiMapa; // Aqui van guardados las diferentes colas
    private ObjectOutputStream oos; // La salida del socket
    private ObjectInputStream ois; // la entrada del socket
    private static MensajeProtocolo mensajeProtocolo; // sirve para toda la gestion de los mensajes protocolos, tanto los que envias, como los que recibes
    private final Integer id; // sirve para tener una depuracion de los diferentes hilos lanzados
    private final Integer numHilosTotales; // sirve para saber el numero de hilos totales que tiene el servidor
    private ThreadPoolExecutor executor; // sirve para saber datos de depuracion del pool de hilos

    /**
     * Este es el constructor de los hilos que se van a lanzar, para cuando haya que atender a clientes
     * @param socketRecibido Es el socket con el que se haya aceptado la conexion al servidor
     * @param id Es el id proporcionado al hilo sirviente
     * @param usuariosHashMap Aqui se guardan los usuarios con las contraseñas
     * @param listaIps Aqui se guarda las direcciones Ip conectadas al servidor
     * @param listaLoginsIncorrectos Aqui se guarda las direcciones Ip que han fallado el login
     * @param numHilosTotales El numero de hilos que se pueden lanzar
     * @param mapaSirvientes Hay que pasar un mapa de los sirvientes que ha lanzado el servidor, sirve para depurar
     * @param mapaMensajesAddRead Aqui van los datos de depuracion de las diferentes colas
     * @param multiMapa Aqui van guardados las diferentes colas
     * @param executor Es con lo que se ha lanzado el pool de hilos, sirve para depurar
     * @throws java.io.IOException
     */
    public Sirviente(Socket socketRecibido,
                     Integer id,
                     ConcurrentHashMap<String, String> usuariosHashMap,
                     BlackListManager listaIps,
                     BlackListManager listaLoginsIncorrectos,
                     int numHilosTotales,
                     ConcurrentHashMap<Integer, Sirviente> mapaSirvientes,
                     ConcurrentHashMap<String, ContadorAddRead> mapaMensajesAddRead,
                     MultiMap multiMapa,
                     ThreadPoolExecutor executor) throws java.io.IOException {
        this.socket = socketRecibido;
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
        this.id = id;
        this.usuariosHashMap = usuariosHashMap;
        this.listaIps = listaIps;
        this.listaLoginsIncorrectos = listaLoginsIncorrectos;
        this.numHilosTotales = numHilosTotales;
        this.mapaSirvientes = mapaSirvientes;
        this.mapaMensajesAddRead = mapaMensajesAddRead;
        this.multiMapa = multiMapa;
        this.executor = executor;
        // las explicaciones del motivo de cada variable, se dan arriba junto a las variables declaradas
    }

    public void run() {
        String usuarioCliente = ""; // sirve para saber si el cliente ha iniciado sesión. Cada vez que se ejecute la accion de LOGGIN, se vuelve a resetear
        String contraCliente = ""; // sirve para saber si el cliente ha iniciado sesión. Cada vez que se ejecute la acción de LOGGIN, se vuelve a resetear
        String mensajeEnviar; // sirve de apoyo, para enviar mensajes
        try {
            listaIps.put(socket.getInetAddress(), new AtomicInteger(1));
            if (listaIps.superarFallosPermitidos(socket.getInetAddress())){
                mensajeProtocolo = new MensajeProtocolo(Primitiva.ERROR, "Err Max Number of connections reached.");
                oos.writeObject(mensajeProtocolo);
                listaIps.restarUna(socket.getInetAddress());
                socket.close();
                return;
            }
            if (listaLoginsIncorrectos.superarFallosPermitidos(socket.getInetAddress())){
                mensajeProtocolo = new MensajeProtocolo(Primitiva.ERROR, "Err Max Number of login attempts reached.");
                oos.writeObject(mensajeProtocolo);
                listaIps.restarUna(socket.getInetAddress());
                socket.close();
                return;
            }
            /*
            Siempre aceptamos de primeras las conexiones, una vez aceptadas las conexiones, incrementaremos
            el contador de la ip conectada con la instruccón de la línea 76, y procederemos hacer las comprobaciones
            1ª Comprobacion: comprobamos que la ip solo tenga tres conexiones. Va desde la linea 77 a la 82
            2ª Comprobacion: comprobamos que la ip no haya fallado tres veces el LOGGIN. Va desde la linea 84 a la 89
             */
            System.out.println("[BM] (connections) for IP /" +socket.getInetAddress().toString()+" = "+listaIps.getNumFallos(socket.getInetAddress()));
            mensajeProtocolo = new MensajeProtocolo(Primitiva.INFO, "Hola, para iniciar sesion escriba 'LOGGIN', " +
                    " para añadir un mensaje a una cola escriba 'ADDMSG', para leer un mensaje de una cola escriba 'READMSG', para cerrar conexion " +
                    "escriba 'EXIT'\n");
            oos.writeObject(mensajeProtocolo);
            /*
            La linea 97, es una linea de depuracion que se pedía en el primer modulo a realizar de la practica
            En la conexion cliente-servidor, en nuestro caso, el primero que envia informacion es el servidor al cliente
            He creido conveniente marcarla como info, ya que es información que se da al cliente. No he creido conveniente
            marcar las opciones que podría tener el administrador, ya que si administrador, es porque sabe su trabajo.
            La linea 98, 99, 100, 101 es la que hace el trabajo anterior
             */
            while (true){
                /*
                El procedimiento es el siguiente: Una vez enviado el mensaje de información, el inicio del bucle
                siempre va a empezar con la espera de que un cliente envie un mensaje protocolo, esto se
                puede ver en la linea 123.
                Una vez enviado este mensaje por parte del cliente los posibles mensajes pueden ser:
                1ºEXIT: significa que el cliente quiere cerrar sesion
                2ºXAUTH: significa que el cliente quiere establecer conexion
                3ºADDMSG: significa que el cliente quiere añadir un mensaje a una cola
                4ºREADQ: significa que el cliente quiere leer un mensaje de una cola
                Casos del administrador:
                1º ADDMSG: si la cola no está creada, la crea
                2º DELETEQ: el administrador puede borrar una cola
                Casos unicos:
                1º Que el cliente no nos mande una primitiva acorde con nuestro estandar
                 */
                mensajeProtocolo = (MensajeProtocolo) ois.readObject();
                switch (mensajeProtocolo.getPrimitiva()){
                    case EXIT:
                        mensajeProtocolo = new MensajeProtocolo(Primitiva.EXIT, "Se va a cerrar conexion...Hasta pronto");
                        oos.close();
                        ois.close();
                        socket.close();
                        return;
                        /*
                        Poco que explicar, cerramos los recursos, y se termina el run de este hilo
                         */
                    case XAUTH:
                        usuarioCliente = ""; // aqui es donde reseteamos el usuario cliente, para que no haya fallos al hacer LOGGIN
                        contraCliente = "";
                        usuarioCliente = mensajeProtocolo.getIdCola(); // en la cola de la primitiva XAUTH viene el nombre del usuario
                        contraCliente = mensajeProtocolo.getMensaje(); // en el mensaje de la primitiva XAUTH viene la contraseña del usuario
                        if (usuariosHashMap.containsKey(usuarioCliente)) // verificamos que el usuario existe en el hashMap
                            if (usuariosHashMap.get(usuarioCliente).equals(contraCliente)) { // verificamos que la contraseña sea el valor en el hashMap
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.XAUTH, "User successfully logged");
                                oos.writeObject(mensajeProtocolo); // en la linea 141 preparamos el mensaje, y en la 142 lo enviamos
                            }else{ // llegamos a esta parte cuando la contraseña no coincide
                                listaLoginsIncorrectos.put(socket.getInetAddress(), new AtomicInteger(1)); // incrementamos en uno el valor de veces que se ha tratado de hacer LOGGIN desde esta ip
                                System.out.println("[BM] (login fails) for IP  /" +socket.getInetAddress().toString()+" = "+listaLoginsIncorrectos.getNumFallos(socket.getInetAddress()));
                                if (listaLoginsIncorrectos.superarFallosPermitidos(socket.getInetAddress())){
                                    mensajeProtocolo = new MensajeProtocolo(Primitiva.ERROR, "Err Max Number of login attempts reached.");
                                    oos.writeObject(mensajeProtocolo);
                                    listaIps.restarUna(socket.getInetAddress());
                                    System.out.println("[BM] (connections) for IP /" +socket.getInetAddress().toString()+" = "+listaIps.getNumFallos(socket.getInetAddress()));
                                    oos.close();
                                    ois.close();
                                    socket.close();
                                    return;
                                    /*
                                    En la linea 146 se comprueba, que no se haya fallado el LOGGIN mas de lo permitido,
                                    si ocurre cerramos recuros y se termina el run de este sirviente con este cliente
                                     */
                                }
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.ERROR, "Credentials does not match our records. Enter username again: ");
                                oos.writeObject(mensajeProtocolo);
                                usuarioCliente = "";
                                contraCliente = "";

                                /*
                                En la linea 161 y 162, volvemos a resetear no sea que se haya fallado y quede guardado alguna de las dos
                                para posteriores acciones
                                 */
                            }
                        else{
                            listaLoginsIncorrectos.put(socket.getInetAddress(), new AtomicInteger(1));
                            System.out.println("[BM] (login fails) for IP  /" +socket.getInetAddress().toString()+" = "+listaLoginsIncorrectos.getNumFallos(socket.getInetAddress()));
                            if (listaLoginsIncorrectos.superarFallosPermitidos(socket.getInetAddress())){
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.ERROR, "Err Max Number of login attempts reached.");
                                oos.writeObject(mensajeProtocolo);
                                listaIps.restarUna(socket.getInetAddress());
                                System.out.println("[BM] (connections) for IP /" +socket.getInetAddress().toString()+" = "+listaIps.getNumFallos(socket.getInetAddress()));
                                socket.close();
                                return;
                            }
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.ERROR, "Credentials does not match our records. Enter username again: ");
                            usuarioCliente = "";
                            contraCliente = "";
                            oos.writeObject(mensajeProtocolo);
                            /*
                            Es el mismo codigo que va desde la linea 144 a la 154
                             */
                        }
                        break;
                    case ADDMSG:
                        if(!usuarioCliente.isEmpty()) { // aqui comprobamos que el usuario este registrado
                            if (multiMapa.contains(mensajeProtocolo.getIdCola())){ // aqui comprobamos que la cola que solicita el cliente esté activa
                                multiMapa.push(mensajeProtocolo.getIdCola(), mensajeProtocolo.getMensaje()); // si lo esta introducimos el mensaje en a cola
                                mapaMensajesAddRead.get(mensajeProtocolo.getIdCola()).sumaUnaAdd(); // sumamos a uno el valor de depuracion de mensajes añadidos a la cola
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.ADDED); // mandamos el mensaje protocolo con la primitiva correspondiente de que se ha enviado bien
                                oos.writeObject(mensajeProtocolo);
                            }else if ((!multiMapa.contains(mensajeProtocolo.getIdCola()) && (usuarioCliente.equals("admin")))) {
                                System.out.println("El administrador ha creado una cola"); // aqui llegamos cuando, no existe la cola, pero el que envia el mensaje es el administrador
                                multiMapa.push(mensajeProtocolo.getIdCola(), mensajeProtocolo.getMensaje()); // Es el mismo proceso que va desde la linea 192 a la 195
                                mapaMensajesAddRead.put(mensajeProtocolo.getIdCola(), new ContadorAddRead());
                                mapaMensajesAddRead.get(mensajeProtocolo.getIdCola()).sumaUnaAdd();
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.ADDED);
                                oos.writeObject(mensajeProtocolo);
                            }else{// cuando no se es admin, ni está la cola presente se llega a este apartado
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.NOTAUTH, "No existe la cola de mensajes y no eres administrador para poder crear una");
                                oos.writeObject(mensajeProtocolo);
                            }
                        }else{ // cuando no se esta loggeado se llega a este apartado
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.NOTAUTH, "Usted no está loggeado");
                            oos.writeObject(mensajeProtocolo);
                        }
                        break;
                    case READQ:
                        if (!usuarioCliente.isEmpty()) { // se accede a las lineas de abajo cuando el cliente se ha loggeado
                            mensajeEnviar = multiMapa.pull(mensajeProtocolo.getMensaje()); // extraemos el mensaje del multi mapa
                            if (mensajeEnviar == null){ // si es null significa que no hay cola, o mensajes que extraer por que devolvemos un mensaje protocolo con primitiva EMPTY
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.EMPTY);
                                oos.writeObject(mensajeProtocolo);
                            }
                            else {
                                mapaMensajesAddRead.get(mensajeProtocolo.getIdCola()).sumaUnaRead();
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.MSG, mensajeEnviar);
                                oos.writeObject(mensajeProtocolo);
                                /*
                                 Este apartado es como el proceso exitoso de enviar un mensaje por parte del cliente,
                                 pero ahora en vez de sumar uno al contador de Add se los sumamos al de Read
                                 */
                            }
                        }
                        else{// aqui llegamos cuando no se está logeado
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.NOTAUTH, "Usted no está loggeado");
                            oos.writeObject(mensajeProtocolo);
                        }
                        break;
                    case STATE: // Primitiva exclusiva del administrador
                        if (usuarioCliente.equals("admin")){
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.INFO, ""+numHilosTotales+":"+executor.getActiveCount()+
                                    ":"+mapaMensajesAddRead.get(mensajeProtocolo.getMensaje()).getContadorAdd()
                                    +":"+mapaMensajesAddRead.get(mensajeProtocolo.getMensaje()).getContadorRead());
                            oos.writeObject(mensajeProtocolo);
                        }else{
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.NOTAUTH, "Usted no está loggeado como administrador para ejecutar este mensaje...");
                            oos.writeObject(mensajeProtocolo);
                        }
                        break;
                    case DELETEQ: //primitiva exclusiva del administrador
                        if (usuarioCliente.equals("admin")){
                            if (multiMapa.contains(mensajeProtocolo.getMensaje())){
                                multiMapa.delCola(mensajeProtocolo.getMensaje());
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.DELETED);
                                oos.writeObject(mensajeProtocolo);
                            }else{
                                mensajeProtocolo = new MensajeProtocolo(Primitiva.EMPTY, "Cola no existente");
                                oos.writeObject(mensajeProtocolo);
                            }
                        }else{
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.NOTAUTH, "Usted no está loggeado como administrador para ejecutar este mensaje...");
                            oos.writeObject(mensajeProtocolo);
                        }
                        break;
                    case BADCODE:
                        mensajeProtocolo = new MensajeProtocolo(Primitiva.INFO, "Perdón por la equivocación, vuelve a enviar el mensaje");
                        oos.writeObject(mensajeProtocolo);
                        break;
                    default:
                        mensajeProtocolo = new MensajeProtocolo(Primitiva.BADCODE);
                        oos.writeObject(mensajeProtocolo);

                }
            }
        }catch (SocketException e){
            listaIps.restarUna(socket.getInetAddress());
            if (!usuarioCliente.isEmpty())
                System.out.println("El usuario: "+usuarioCliente+" ha cerrado conexión");
            System.out.println("[BM] (connections) for IP /" +socket.getInetAddress().toString()+" = "+listaIps.getNumFallos(socket.getInetAddress()));
            try{
                oos.close();
                ois.close();
                socket.close();
            }catch (Exception ex){
                System.out.println("No se ha podido cerrar bien los recursos"+ex.getLocalizedMessage());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

