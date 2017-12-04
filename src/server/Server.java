/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import battleship.FilaJogadores;
import battleship.Tabuleiro;
import battleship.TiroEnum;
import util.Mensagem;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import util.Estados;
import util.Status;

/**
 *
 * @author elder
 */
public class Server {
    /* 1 - Criar o servidor de conexões
     2 -Esperar o um pedido de conexão;
     Outro processo
     2.1 e criar uma nova conexão;
     3 - Criar streams de enechar socket de comunicação entre servidor/cliente
     4.2 - Fechar streams de entrada e saída
     trada e saída;
     4 - Tratar a conversação entre cliente e 
     servidor (tratar protocolo);
     4.1 - Fechar socket de comunicação entre servidor/cliente
     4.2 - Fechar streams de entrada e saída
           
     5 - voltar para o passo 2, até que finalize o programa;
     6 - fechar serverSocket
     */

    private ServerSocket serverSocket;
    private int cont;
    private List<Thread> threads;
    private List<TrataConexao> clientes;
    private Tabuleiro tabuleiro;
    private FilaJogadores<TrataConexao> fila;
    /*- Criar o servidor de conexões*/

    private void criarServerSocket(int porta) throws IOException {
        serverSocket = new ServerSocket(porta);
        cont = 0;
        threads = new ArrayList<>();
        clientes = new ArrayList<>();
        tabuleiro = new Tabuleiro();
        fila = new FilaJogadores<>();
    }
    

    /*2 -Esperar o um pedido de conexão;
     Outro processo*/
    private Socket esperaConexao() throws IOException {
        Socket socket = serverSocket.accept();
        return socket;
    }

    private void fechaSocket(Socket s) throws IOException {
        s.close();
    }

    private void enviaMsg(Object o, ObjectOutputStream out) throws IOException {
        out.writeObject(o);
        out.flush();
    }
    public synchronized void avisa()
    {
        System.out.println("Contador: " + ++cont);
    }
   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException {
        try {

            Server server = new Server();
            server.iniciaServidor();
            
        } catch (IOException e) {
            //trata exceção
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }

    private void iniciaServidor() throws IOException{
    
        this.criarServerSocket(5555);
            while (true) {
                System.out.println("Aguardando conexão...");
                Socket socket = this.esperaConexao();//protocolo
                System.out.println("Cliente conectado.");
                //Outro processo
                TrataConexao jogador = new TrataConexao( socket, this  );
                Thread th = new Thread( jogador );
                threads.add(th);
                clientes.add(jogador);
                
                th.start();
                
               
                System.out.println("Tratando cliente conectado...");
            }
    
    }

    public String getRanking() {
       String ranking = "Ranking da partida \n\n";
       
       for( TrataConexao j: clientes)
       {
           ranking +=  j.getNome() + " -----> " + j.getPontuação()+"\n";
       }
       
       return ranking;
    
    }
    
    
     public synchronized void  addJogadorFila(TrataConexao jogador)
    {
        fila.enfilera(jogador);
        
    }

    void sorteiaProximo() throws Exception {
        //pega o proximo da fila
        TrataConexao proximo = fila.proximo();
        //envia a ordem de vez de jogar
        proximo.proximoJogador();
        
    }

    TiroEnum fazJogada(int x, int y,TrataConexao quemJogou) throws Exception {
        
        TiroEnum tiro = tabuleiro.atirar(x, y);
        System.out.println("Resultado do tiro: " + tiro);
        System.out.println(tabuleiro.desenhaTabuleiro());
      
         //parte de mandar msg pra todos Jogadorres
        Mensagem m = new Mensagem("STATUS");
        m.setParam("tabuleiro","\n"+ tabuleiro.desenhaTabuleiro());
        m.setParam("fimDoJogo", tabuleiro.fimDeJogo());
        m.setParam("mensagem","O jogador: "+ quemJogou.getNome() + " Jogou: X: " +x+ " Y: "+ y + " Resultado: " +tiro );
        
        //Lista de Jogadores
        for (TrataConexao cliente : clientes) {
            //pega um por um JOGANDO ou que estão com a VEZ de JOGAR 
            if (cliente.getEstado().equals(Estados.JOGANDO) || cliente.getEstado().equals(Estados.VEZDEJOGAR)){
             //Envia o m (Mensagem)
               cliente.enviaMensagem(m);
            }
            
        }
        
        //teste de ´r o fim do jogo
        if(tabuleiro.fimDeJogo()){
           tabuleiro = new Tabuleiro();
        }
        
 
        return tiro;
    }

//    public String enviaStatus() {
//                
//      String DesenhoTabuleiro = tabuleiro.desenhaTabuleiro();
//      return DesenhoTabuleiro;
//       cada trataConexao 
//       envia o tabuleiro
//       fim do jogo
//       ultima jogada
//    
//    }

    boolean eprimeiro(TrataConexao jogador) {
        
        if (fila.proximo() == jogador)
            return true;
        else
            return false; 
        
    }

    synchronized void removerFila() throws Exception {
        fila.desenfilera();
    
    }

    void tirarJogadorFila(TrataConexao aThis) {
    fila.removeElemento(aThis);
    }


}
