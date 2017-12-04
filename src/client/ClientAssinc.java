package client;

import battleship.TiroEnum;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Mensagem;
import util.Status;

public class ClientAssinc {

    Socket socket;
    Thread thread;
    ObjectInputStream input;
    ObjectOutputStream output;
    Boolean ligado;
    Boolean ConectouJogo = false;

    //Controla a vez de jogar
    Boolean vezDeJogar;
    Scanner scanner = new Scanner(System.in);
    Mensagem m = new Mensagem(null);

    public ClientAssinc() {
        vezDeJogar = false;

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
                        if (m.getOperacao().equals("VEZDEJOGAR")) {
                            vezDeJogar = true;
                        } else if (m.getOperacao().equals("JOGADAREPLY")) {
                            TiroEnum x = (TiroEnum) m.getParam("resultado");

                            if (x.equals((TiroEnum.AGUA))) {
                                System.out.println("Você Perdeu a VEZ. ahehe");
                                vezDeJogar = false;
                            }
                        }

                        if (m.getOperacao().equals("SAIRREPLY") && m.getStatus() == Status.OK) {

                            ligado = false;
                        }
                        if (m.getOperacao().equals("SAIRDOJOGOREPLY") && m.getStatus() == Status.OK) {
                               ConectouJogo = false;

                    }

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

    public void controlaJogo() throws IOException, ClassNotFoundException {

        disparaThread();
        while (ligado) {

            System.out.println("______MENU_______\n"
                    + " 1 - ENTRAR NO JOGO: \n"
                    + " 2 - DESCONECTAR: \n"
                    + "____________________\n"
                    + " 3 - ATIRAR: \n"
                    + " 4 - RANKING: \n"
                    + " 5 - SAIR DO JOGO:    \n"
                    + "Opção:   ");
            int op = scanner.nextInt();

            switch (op) {
                case 1:

                    if (!ConectouJogo) {
                        System.out.println("entrou");
                        m = new Mensagem("ENTRARNOJOGO");
                        m.setParam("nome", "RAFA");
                        output.writeObject(m);

                      //  m = (Mensagem) input.readObject();
                        //System.out.println("TESTE" + m);
                        if (m.getOperacao().equals("ENTRARNOJOGOREPLY") && m.getStatus() == Status.OK) {
                           // disparaThread();
                            ConectouJogo = true;
                        }
                    } else {
                        System.out.println("Você já está Jogando.....Burro");
                    }

                    break;
                case 2:
                    //sair do Programa

                    break;

                case 3:
                    if (ConectouJogo) {
                        if (vezDeJogar) {
                            System.out.println("DIGITE O VALOR DE X (0 a 9) ");
                            int x = scanner.nextInt();
                            System.out.println("DIGITE O VALOR DE Y (0 a 9) ");
                            int y = scanner.nextInt();

                            m = new Mensagem("JOGADA");
                            m.setParam("x", x);
                            m.setParam("y", y);
                            output.writeObject(m);

                        } else {
                            System.out.println("Não é sua vez de jogar!");
                        }
                    } else {
                        System.out.println("Você não esta Jogando!!!");
                    }

                    break;
                case 4:
                    m = new Mensagem("RANKING");
                    output.writeObject(m);
                    output.flush();
                    break;
                case 5:
                    m = new Mensagem("SAIRDOJOGO");
                    output.writeObject(m);
                    
                    
                    //m = (Mensagem) input.readObject();
                    
                    

                    break;

            }

        }

    }

    public void desliga() {
        ligado = false;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        ClientAssinc cliente = new ClientAssinc();

        try {
            cliente.conectaServidor("localhost", 5555);
            cliente.controlaJogo();

        } catch (IOException ex) {
            Logger.getLogger(ClientAssinc.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro no cliente: " + ex.getMessage());
            cliente.desliga();
        }

    }

}
