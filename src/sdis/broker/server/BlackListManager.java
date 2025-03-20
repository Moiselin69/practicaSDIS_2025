package sdis.broker.server;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BlackListManager {
    private ConcurrentHashMap<InetAddress, AtomicInteger> mapaFallos = new ConcurrentHashMap<>();
    private int numFallosPermitidos;

    /**
     * Constructor de la clase BlackListManager
     * @param numFallosPermitidos Como parametro tendras que pasr el numero de fallos maximos que se pueden lograr
     */
    public BlackListManager(int numFallosPermitidos){
        this.numFallosPermitidos = numFallosPermitidos;
    }

    /**
     * Metodo para introducir a la BlackList una nueva ip
     * @param ipCliente La ip del nuevo cliente
     * @param numFallos El numero de fallos que lleva cometido
     */
    public void put(InetAddress ipCliente, AtomicInteger numFallos){
        if (mapaFallos.containsKey(ipCliente))
            sumarUna(ipCliente);
        else
            mapaFallos.put(ipCliente, numFallos);
    }

    /**
     * Metodo para saber el numero de fallos de un cliente
     * @param ipCliente Como parametro la ip del cliente
     * @return Devuelve el numero de fallos
     */
    public  AtomicInteger getNumFallos(InetAddress ipCliente){
        return mapaFallos.get(ipCliente);
    }

    /**
     * Metodo que sirve para sumar en uno el numero de fallos del cliente
     * @param ipCliente Como parametro hay que pasar la ip del cliente
     */
    public void sumarUna(InetAddress ipCliente){
        mapaFallos.compute(ipCliente, (key,valor) ->{
           if (valor == null)
               return new AtomicInteger(1);
           else{
                valor.incrementAndGet();
                return valor;
           }
        });
    }

    /**
     * Metodo que sirve para restar en uno, el numero de fallos del cliente
     * @param ipCliente Como parametro tienes que pasar el ip del cliente
     */
    public void restarUna(InetAddress ipCliente){
        mapaFallos.compute(ipCliente, (key,valor) ->{
            if (valor == null)
                return new AtomicInteger(1);
            else{
                valor.decrementAndGet();
                return valor;
            }
        });
    }

    /**
     * Metodo para saber si se han superado los fallos permitidos
     * @param ipCliente Como parametro tienes que pasar la ip del cliente
     * @return Devuelve true si se ha superado el numero de fallos, false si no se ha superado
     */
    public boolean superarFallosPermitidos(InetAddress ipCliente){
        if (mapaFallos.get(ipCliente) == null)
            return false;
        if (mapaFallos.get(ipCliente).get() > numFallosPermitidos)
            return true;
        else
            return false;
    }
}
