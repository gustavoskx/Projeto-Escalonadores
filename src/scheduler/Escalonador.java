package scheduler;

import model.Processo;
import java.util.List;

/**
 * Interface para algoritmos de escalonamento de processos.
 * Define os métodos que todos os escalonadores devem implementar.
 */
public interface Escalonador {

    /**
     * Adiciona um novo processo à fila de processos.
     * 
     * @param processo O processo a ser adicionado
     */
    void adicionarProcesso(Processo processo);

    /**
     * Adiciona uma lista de processos à fila de processos.
     * 
     * @param processos Lista de processos a serem adicionados
     */
    void adicionarProcessos(List<Processo> processos);

    /**
     * Obtém o próximo processo a ser executado de acordo com o algoritmo de escalonamento.
     * 
     * @param tempoAtual Tempo atual da simulação
     * @return O próximo processo a ser executado ou null se não houver processos disponíveis
     */
    Processo obterProximoProcesso(int tempoAtual);

    /**
     * Verifica se todos os processos foram concluídos.
     * 
     * @return true se todos os processos foram concluídos, false caso contrário
     */
    boolean estaFinalizado();

    /**
     * Retorna o nome do algoritmo de escalonamento.
     * 
     * @return Nome do algoritmo
     */
    String obterNome();

    /**
     * Retorna a lista de todos os processos gerenciados por este escalonador.
     * 
     * @return Lista de processos
     */
    List<Processo> obterTodosProcessos();

    /**
     * Reinicia o escalonador, preparando-o para uma nova simulação.
     */
    void reiniciar();
}