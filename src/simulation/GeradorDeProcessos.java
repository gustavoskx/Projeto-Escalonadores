package simulation;

import model.Processo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe responsável por gerar conjuntos de processos com dados aleatórios.
 */
public class GeradorDeProcessos {
    private Random aleatorio;

    /**
     * Construtor padrão que inicializa o gerador de números aleatórios.
     */
    public GeradorDeProcessos() {
        this.aleatorio = new Random();
    }

    /**
     * Construtor que permite definir uma semente para o gerador de números aleatórios.
     * Útil para testes onde queremos resultados reproduzíveis.
     * 
     * @param semente Semente para o gerador de números aleatórios
     */
    public GeradorDeProcessos(long semente) {
        this.aleatorio = new Random(semente);
    }

    /**
     * Gera um conjunto de processos com dados aleatórios.
     * 
     * @param quantidade Número de processos a serem gerados
     * @return Lista de processos gerados
     */
    public List<Processo> gerarProcessos(int quantidade) {
        List<Processo> processos = new ArrayList<>();

        for (int i = 0; i < quantidade; i++) {
            // Define o tempo de chegada
            int tempoChegada;
            if (i == 0) {
                // Garante que o primeiro processo sempre chegue no tempo 0
                tempoChegada = 0;
            } else {
                // Para os demais processos, gera tempo de chegada entre 0 e 10
                tempoChegada = aleatorio.nextInt(11);
            }

            // Gera tempo de execução entre 1 e 10
            int tempoExecucao = aleatorio.nextInt(10) + 1;

            // Cria o processo com ID sequencial (P1, P2, ...)
            Processo processo = new Processo("P" + (i + 1), tempoChegada, tempoExecucao);
            processos.add(processo);
        }

        return processos;
    }

    /**
     * Define uma nova semente para o gerador de números aleatórios.
     * 
     * @param semente Nova semente
     */
    public void definirSemente(long semente) {
        this.aleatorio = new Random(semente);
    }
}
