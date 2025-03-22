package sdis.broker.common;

public class MensajeProtocolo implements java.io.Serializable {
    private final Primitiva primitiva;
    private final String mensaje; /* HELLO, PUSH, PULL_OK */
    private final String idCola;  /* PUSH, PULL_WAIT, PULL_NOWAIT */

    /* Constructor para PUSH_OK, EMPTY, NOTUNDERSTAND, EXIT, STATE */
    public MensajeProtocolo(Primitiva p) throws MalMensajeProtocoloException {
        if (p == Primitiva.ADDED || p == Primitiva.EMPTY
                || p == Primitiva.EXIT || p == Primitiva.DELETED || p == Primitiva.BADCODE) {
            this.primitiva = p;
            this.mensaje = this.idCola = null;
        } else
            throw new MalMensajeProtocoloException();
    }

    /* Constructor para INFO, ADDED, NOTAUTH, XAUTH (SERVIDOR), ERROR, EXIT, MSG, STATE, DELETEQ, READQ*/
    public MensajeProtocolo(Primitiva p, String mensaje) throws MalMensajeProtocoloException {
        if (p == Primitiva.INFO || p == Primitiva.ADDED || p == Primitiva.NOTAUTH
                || p == Primitiva.XAUTH || p == Primitiva.ERROR || p == Primitiva.EXIT
                || p == Primitiva.MSG || p == Primitiva.STATE || p == Primitiva.DELETEQ
                || p == Primitiva.EMPTY) {
            this.mensaje = mensaje;
            this.idCola  = null;
        } else if (p == Primitiva.READQ) {
            this.idCola  = mensaje;
            this.mensaje = null;
        } else
            throw new MalMensajeProtocoloException();
        this.primitiva = p;
    }

    /* Constructor para ADDMSG XAUTH (CLIENTE) */
    public MensajeProtocolo(Primitiva p, String idCola, String mensaje) throws MalMensajeProtocoloException {
        if ( (p == Primitiva.ADDMSG) || (p == Primitiva.XAUTH) ){
            this.primitiva = p;
            this.mensaje = mensaje;
            this.idCola = idCola;
        }
        else
            throw new MalMensajeProtocoloException();
    }

    public Primitiva getPrimitiva() { return this.primitiva; }
    public String getMensaje() { return this.mensaje; }
    public String getIdCola() { return this.idCola; }

    public String toString() { /* prettyPrinter de la clase */
        switch (this.primitiva) {
            case INFO:
            case MSG:
                return this.primitiva+":"+this.mensaje ;
            case READQ:
                return this.primitiva+":"+this.idCola ;
            case ADDMSG:
                return this.primitiva+":"+this.idCola+":"+this.mensaje ;
            default :
                return this.primitiva.toString() ;
        }
    }
}
