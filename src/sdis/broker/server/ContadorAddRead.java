package sdis.broker.server;

import java.util.concurrent.atomic.AtomicInteger;

public class ContadorAddRead {
    private AtomicInteger contadorAdd = new AtomicInteger(0);
    private AtomicInteger contadorRead = new AtomicInteger(0);

    /**
     * Metodo para obtener las veces que se ha añadido un mensaje a la cola
     * @return Devuelve la cantidad de mensajes que se han añadido a la cola
     */
    public AtomicInteger getContadorAdd() {
        return contadorAdd;
    }

    /**
     * Metodo para establecer el contador de mensajes añadidos a la cola
     * @param contadorAdd el valor que quieras establecer
     */
    public void setContadorAdd(int contadorAdd){
        this.contadorAdd = new AtomicInteger(contadorAdd);
    }

    /**
     * Metodo para suma uno al contador de mensajes añadidos a la cola
     */
    public void sumaUnaAdd(){
        contadorAdd.incrementAndGet();
    }

    /**
     * Metodo para obtener el contador de mensajes leidos de una cola
     * @return Devuelve el contador de mensajes leidos de una cola
     */
    public AtomicInteger getContadorRead(){
        return contadorRead;
    }

    /**
     * Metodo para establecer el contador de mensajes leidos de una cola
     * @param contadorRead Devuelve el contador de mensajes leidos de una cola
     */
    public void setContadorRead(int contadorRead){
        this.contadorRead = new AtomicInteger(contadorRead);
    }

    /**
     * Metodo para sumar uno al contador de mensajes leidos
     */
    public void sumaUnaRead(){
        this.contadorRead.incrementAndGet();
    }
}
