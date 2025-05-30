package scheduler;

import model.Processo;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Implementação do algoritmo de escalonamento Round Robin (RR).
 * Alterna entre os processos, dando a cada um uma fatia de tempo fixa (quantum).
 */
public class EscalonadorRoundRobin implements Escalonador {
    private List<Processo> processos;
    private Queue<Processo> filaDeProcessos;
    private final int quantum;
    private Processo processoAtual;
    private int tempoRestanteQuantum;

    /**
     * Construtor para o escalonador Round Robin com quantum padrão de 4.
     */
    public EscalonadorRoundRobin() {
        this(4); // Quantum padrão de 4 unidades de tempo
    }

    /**
     * Construtor para o escalonador Round Robin com quantum personalizado.
     * 
     * @param quantum Quantum (fatia de tempo) para cada processo
     */
    public EscalonadorRoundRobin(int quantum) {
        this.processos = new ArrayList<>();
        this.filaDeProcessos = new LinkedList<>();
        this.quantum = quantum;
        this.processoAtual = null;
        this.tempoRestanteQuantum = 0;
    }

    @Override
    public void adicionarProcesso(Processo processo) {
        processos.add(processo);
    }

    @Override
    public void adicionarProcessos(List<Processo> processos) {
        this.processos.addAll(processos);
    }

    @Override
    public Processo obterProximoProcesso(int tempoAtual) {
        // Atualiza a fila de prontos com processos que chegaram até o momento atual
        atualizarFilaDeProcessos(tempoAtual);

        // Se o processo atual ainda tem tempo de quantum e não terminou, continua com ele
        if (processoAtual != null && tempoRestanteQuantum > 0 && !processoAtual.isFinalizado()) {
            tempoRestanteQuantum--;
            return processoAtual;
        }

        // Se o processo atual terminou seu quantum mas não terminou a execução, coloca de volta na fila
        if (processoAtual != null && !processoAtual.isFinalizado()) {
            filaDeProcessos.add(processoAtual);
        }

        // Pega o próximo processo da fila
        processoAtual = filaDeProcessos.poll();

        // Se temos um novo processo, reinicia o contador de quantum
        if (processoAtual != null) {
            tempoRestanteQuantum = quantum - 1; // -1 porque vamos executar uma unidade agora
        }

        return processoAtual;
    }

    /**
     * Atualiza a fila de processos prontos com base no tempo atual.
     * 
     * @param tempoAtual Tempo atual da simulação
     */
    private void atualizarFilaDeProcessos(int tempoAtual) {
        // Adiciona à fila de prontos os processos que chegaram e ainda não foram concluídos
        List<Processo> novosProcessosProntos = processos.stream()
                .filter(p -> p.getTempoChegada() <= tempoAtual && !p.isFinalizado() && 
                       !filaDeProcessos.contains(p) && p != processoAtual)
                .collect(Collectors.toList());

        filaDeProcessos.addAll(novosProcessosProntos);
    }

    @Override
    public boolean estaFinalizado() {
        // Verifica se todos os processos foram concluídos
        return processos.stream().allMatch(Processo::isFinalizado);
    }

    @Override
    public String obterNome() {
        return "Round Robin (RR) - Quantum: " + quantum;
    }

    @Override
    public List<Processo> obterTodosProcessos() {
        return new ArrayList<>(processos);
    }

    @Override
    public void reiniciar() {
        filaDeProcessos.clear();
        processos.clear();
        processoAtual = null;
        tempoRestanteQuantum = 0;
    }

    /**
     * Retorna o valor do quantum utilizado por este escalonador.
     * 
     * @return Valor do quantum
     */
    public int obterQuantum() {
        return quantum;
    }
}
