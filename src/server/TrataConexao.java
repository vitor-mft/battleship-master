/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import battleship.TiroEnum;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Estados;
import util.Mensagem;
import util.Status;

/**
 *
 * @author elder
 */
public class TrataConexao implements Runnable {
    
    private String nome;
    private Integer pontuação;
    
    @Override
    public void run(){
        try {
            trataConexao(socket);
        } catch (IOException ex) {
            System.out.println("Erro no tratamento do cliente: " + socket.toString() + ": "+ ex.getMessage());
        } catch (ClassNotFoundException ex) {
            
        
        }
        
    }
    
    
    private void trataConexao(Socket socket) throws IOException, ClassNotFoundException {
        // * Cliente ------SOCKET-----servidor
        //protocolo da aplicação
        /*
         4 - Tratar a conversação entre cliente e 
         servidor (tratar protocolo);
         */

        try {
            /* 3 - Criar streams de entrada e saída;*/

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            /*protocolo
             HELLO
             nome : String
             sobrenome: String
            
             HELLOREPLY
             OK, ERRO, PARAMERROR
             mensagem : String
            
             */
            /*4 - Tratar a conversação entre cliente e 
             servidor (tratar protocolo);*/
            System.out.println("Tratando...");
            Estados estado = Estados.CONECTADO;
            while (estado != Estados.SAIR) {
                server.avisa();
                Mensagem m = (Mensagem) input.readObject();
                System.out.println("Mensagem do cliente:\n" + m);
              

                String operacao = m.getOperacao();
                Mensagem reply = new Mensagem(operacao + "REPLY");
                //estados conectado autenticado
                switch (estado) {
                    case CONECTADO:
                        switch (operacao) {
                            case "LOGIN":
                                try {
                                    String user = (String) m.getParam("user");
                                    String pass = (String) m.getParam("pass");

                                    if (user.equals("ALUNO") && pass.equals("ESTUDIOSO")) {
                                        reply.setStatus(Status.OK);
                                        estado = Estados.JOGANDO;
                                    } else {
                                        reply.setStatus(Status.ERROR);
                                    }

                                } catch (Exception e) {
                                    reply.setStatus(Status.PARAMERROR);
                                    reply.setParam("msg", "Erro nos parâmetros do protocolo.");
                                }
                                break;
                            case "HELLO":
                                String nome = (String) m.getParam("nome");
                                String sobrenome = (String) m.getParam("sobrenome");

                                reply = new Mensagem("HELLOREPLY");

                                if (nome == null || sobrenome == null) {
                                    reply.setStatus(Status.PARAMERROR);
                                } else {
                                    reply.setStatus(Status.OK);
                                    reply.setParam("mensagem", "Hello World, " + nome + " " + sobrenome);

                                }
                                break;
                            case "SAIR":
                                reply.setStatus(Status.OK);
                                estado = Estados.SAIR;
                                break;
                            default:
                                //responder mensagem de erro: Não autorizado/ou inválida
                                reply.setStatus(Status.ERROR);
                                reply.setParam("msg", "MENSAGEM NÃO AUTORIZADA OU INVÁLIDA!");

                                break;
                        }
                        break;
                    case JOGANDO:
                        switch (operacao) {
                            case "RANKING":
                                try {
                                    String ranking = server.getRanking();
                                    reply.setParam("ranking", ranking);
                                    reply.setStatus(Status.OK);
                                    
                                } catch (Exception e) {
                                    }
                                break;
                            case "SUB":
                                break;
                            case "MUL":
                                break;
                            case "SOMA":
                                break;
                            case "LOGOUT":
                                reply.setStatus(Status.OK);
                                estado = Estados.CONECTADO;
                                break;
                            case "SAIR":
                                //DESIGN PATTERN STATE
                                reply.setStatus(Status.OK);
                                estado = Estados.SAIR;
                                break;
                            default:
                                reply.setStatus(Status.ERROR);
                                reply.setParam("msg", "MENSAGEM NÃO AUTORIZADA OU INVÁLIDA!");
                                break;
                        }
                        break;
                    case VEZDEJOGAR:
                        switch(operacao)
                        {
                            case "JOGADA":
                                Integer x = (Integer) m.getParam( "X");
                                //pegar as coordenadas da msg
                                ///TiroEnum res = server.fazJogada(  x, y );
                                
                                //if( res == TiroEnum.AGUA )
                               // {
                                    //perde vez de jogar
                                    //sorteia o proximo
                                    //server.sorteiaProximo();
                                //}
                                break;
                        }
                        break;
                    case SAIR: //ESTADP
                        break;

                }

                output.writeObject(reply);
                output.flush();//cambio do rádio amador
            }
            //4.2 - Fechar streams de entrada e saída
            input.close();
            output.close();
        } catch (IOException e) {
            //tratamento de falhas
            System.out.println("Problema no tratamento da conexão com o cliente: " + socket.getInetAddress());
            System.out.println("Erro: " + e.getMessage());
            throw e;
        } finally {
            //final do tratamento do protocolo
            /*4.1 - Fechar socket de comunicação entre servidor/cliente*/
            fechaSocket(socket);
        }

    }

    public String getNome() {
        return nome;
    }

    public Integer getPontuação() {
        return pontuação;
    }
    
     private void fechaSocket(Socket s) throws IOException {
        s.close();
    }
    

    private Socket socket;
    private Server server;
    
    public TrataConexao(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }
    
}
