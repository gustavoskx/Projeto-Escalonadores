package scheduler;

import model.Processo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação do algoritmo de escalonamento Shortest Job First (SJF) não preemptivo.
 * Seleciona o processo com o menor tempo de execução entre os processos disponíveis.
 * Uma vez que um processo começa a executar, ele continua até terminar (não preemptivo).
 */
public class EscalonadorSJF implements Escalonador {
    private List<Processo> processos;
    private List<Processo> filaDeProcessos;
    private Processo processoEmExecucao; // Processo atualmente em execução

    public EscalonadorSJF() {
        this.processos = new ArrayList<>();
        this.filaDeProcessos = new ArrayList<>();
        this.processoEmExecucao = null;
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
        // Se há um processo em execução e ele não terminou, continua com ele (não preemptivo)
        if (processoEmExecucao != null && !processoEmExecucao.isFinalizado()) {
            return processoEmExecucao;
        }

        // Se o processo em execução terminou, limpa a referência
        if (processoEmExecucao != null && processoEmExecucao.isFinalizado()) {
            processoEmExecucao = null;
        }

        // Atualiza a fila de prontos com processos que chegaram até o momento atual
        atualizarFilaDeProcessos(tempoAtual);

        if (filaDeProcessos.isEmpty()) {
            return null;
        }

        // Seleciona o processo com menor tempo de execução (SJF)
        Processo proximoProcesso = filaDeProcessos.stream()
                .min(Comparator.comparingInt(Processo::getTempoExecucao))
                .orElse(null);

        if (proximoProcesso != null) {
            filaDeProcessos.remove(proximoProcesso);
            processoEmExecucao = proximoProcesso; // Marca o processo como em execução
        }

        return proximoProcesso;
    }

    /**
     * Atualiza a fila de processos prontos com base no tempo atual.
     * Valida os processos antes de adicioná-los à fila de prontos.
     * 
     * @param tempoAtual Tempo atual da simulação
     */
    private void atualizarFilaDeProcessos(int tempoAtual) {
        // Adiciona à fila de prontos os processos que chegaram, são válidos e ainda não foram concluídos
        List<Processo> novosProcessosProntos = processos.stream()
                .filter(p -> eProcessoValido(p, tempoAtual))
                .collect(Collectors.toList());

        filaDeProcessos.addAll(novosProcessosProntos);
    }

    /**
     * Verifica se um processo é válido para ser adicionado à fila de prontos.
     * 
     * @param processo Processo a ser validado
     * @param tempoAtual Tempo atual da simulação
     * @return true se o processo for válido, false caso contrário
     */
    private boolean eProcessoValido(Processo processo, int tempoAtual) {
        // Verifica se o processo chegou, não está finalizado, não está na fila e tem tempo de execução positivo
        return processo.getTempoChegada() <= tempoAtual && 
               !processo.isFinalizado() && 
               !filaDeProcessos.contains(processo) && 
               processo.getTempoExecucao() > 0 &&
               processo.getTempoRestante() > 0;
    }

    @Override
    public boolean estaFinalizado() {
        // Verifica se todos os processos foram concluídos
        return processos.stream().allMatch(Processo::isFinalizado);
    }

    @Override
    public String obterNome() {
        return "Shortest Job First (SJF)";
    }

    @Override
    public List<Processo> obterTodosProcessos() {
        return new ArrayList<>(processos);
    }

    @Override
    public void reiniciar() {
        filaDeProcessos.clear();
        processos.clear();
        processoEmExecucao = null;
    }
}
