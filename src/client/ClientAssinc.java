
package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Mensagem;
import util.Status;

/**
 *
 * @author Elder
 */
public class ClientAssinc {

    Socket socket;
    Thread thread;
    ObjectInputStream input;
    ObjectOutputStream output;
    Boolean ligado;

    public ClientAssinc() {
    }

    public void conectaServidor(String host, int porta) throws IOException {
        socket = new Socket(host, porta);
        input = new ObjectInputStream(socket.getInputStream());
        output = new ObjectOutputStream(socket.getOutputStream());
        ligado = true;

    }

    public void disparaThread() {
        thread = new Thread() {
            @Override
            public void run() {
                while (ligado) {
                    try {
                        Mensagem m = (Mensagem) input.readObject();
                        
                        System.out.println("Mensagem recebida:\n" + m);
                        
                        //deve-se tratar a mensagem chegada aqui...
                        
                        if(m.getOperacao().equals("SAIRREPLY") && m.getStatus() == Status.OK )
                            ligado = false;
                    } catch (IOException ex) {
                        System.out.println("Erro ao receber mensagem do servidor: " + ex.getMessage());
                        ligado = false;
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ClientAssinc.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        thread.start();
    }

    public void controlaJogo() throws IOException {
        while (ligado) {
            
            for( int i=0; i<100;i++){
            Mensagem m = new Mensagem("HELLO");
            m.setParam("nome", "Elder");
            m.setParam("sobrenome", "Bernardi");
            
            //enviando a mensagem
            output.writeObject(m);
            }
            Mensagem m = new Mensagem("SAIR");
            m.setParam("nome", "Elder");
            m.setParam("sobrenome", "Bernardi");
            
            //enviando a mensagem
            output.writeObject(m);
            
        }
    }
    
    public  void desliga()
    {
        ligado = false;
    }
    
    public static void main(String[] args) {
            ClientAssinc cliente = new ClientAssinc();
        
        try {
            cliente.conectaServidor("localhost", 5555);
            cliente.disparaThread();
            cliente.controlaJogo();
            
            
            
            
        } catch (IOException ex) {
            Logger.getLogger(ClientAssinc.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro no cliente: " + ex.getMessage());
            cliente.desliga();
        }
        
        
    }

}
