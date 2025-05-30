package model;

/**
 * Classe que representa um processo a ser escalonado.
 * Contém todos os atributos necessários para o cálculo de métricas de desempenho.
 */
public class Processo {
    private String id;              // Identificador do processo (P1, P2, etc.)
    private int tempoChegada;       // Tempo de chegada do processo
    private int tempoExecucao;      // Tempo de execução total necessário
    private int tempoRestante;      // Tempo de execução restante
    private int tempoInicio;        // Tempo em que o processo começou a executar
    private int tempoFim;           // Tempo em que o processo terminou a execução
    private int tempoEspera;        // Tempo total que o processo esperou
    private int tempoRetorno;       // Tempo total desde a chegada até a conclusão
    private boolean iniciado;       // Indica se o processo já começou a executar
    private boolean finalizado;     // Indica se o processo já terminou

    /**
     * Construtor para criar um novo processo.
     * 
     * @param id Identificador do processo
     * @param tempoChegada Tempo de chegada
     * @param tempoExecucao Tempo de execução necessário
     */
    public Processo(String id, int tempoChegada, int tempoExecucao) {
        this.id = id;
        this.tempoChegada = tempoChegada;
        this.tempoExecucao = tempoExecucao;
        this.tempoRestante = tempoExecucao;
        this.iniciado = false;
        this.finalizado = false;
    }

    /**
     * Executa o processo por uma unidade de tempo.
     * 
     * @param tempoAtual Tempo atual da simulação
     * @return true se o processo terminou após esta execução, false caso contrário
     */
    public boolean executar(int tempoAtual) {
        if (!iniciado) {
            iniciado = true;
            tempoInicio = tempoAtual;
        }

        if (tempoRestante > 0) {
            tempoRestante--;
        }

        if (tempoRestante == 0 && !finalizado) {
            finalizado = true;
            tempoFim = tempoAtual + 1; // +1 porque estamos no final desta unidade de tempo
            calcularMetricas();
            return true;
        }

        return false;
    }

    /**
     * Calcula as métricas de desempenho do processo.
     * Garante que os valores calculados nunca sejam negativos.
     */
    private void calcularMetricas() {
        // Calcula o tempo de turnaround (tempo total desde a chegada até a conclusão)
        tempoRetorno = Math.max(0, tempoFim - tempoChegada);

        // Calcula o tempo de espera (tempo total que o processo esperou)
        // Tempo de espera = Tempo de retorno - Tempo de execução
        // Isso contabiliza corretamente o tempo de espera em algoritmos preemptivos
        tempoEspera = Math.max(0, tempoRetorno - tempoExecucao);
    }

    /**
     * Obtém o tempo atual da simulação.
     * Este método retorna o tempo atual baseado no estado do processo.
     */
    private int obterTempoAtual() {
        // Se o processo já terminou, retorna o tempo de término
        if (finalizado) {
            return tempoFim;
        }
        // Se o processo já começou, retorna o tempo de início + tempo já executado
        else if (iniciado) {
            return tempoInicio + (tempoExecucao - tempoRestante);
        }
        // Se o processo ainda não começou, retorna o tempo de chegada
        else {
            return tempoChegada;
        }
    }

    // Getters e setters

    public String getId() {
        return id;
    }

    public int getTempoChegada() {
        return tempoChegada;
    }

    public int getTempoExecucao() {
        return tempoExecucao;
    }

    public int getTempoRestante() {
        return tempoRestante;
    }

    public int getTempoInicio() {
        return tempoInicio;
    }

    public void setTempoInicio(int tempoInicio) {
        this.tempoInicio = tempoInicio;
        this.iniciado = true;
    }

    public int getTempoFim() {
        return tempoFim;
    }

    public void setTempoFim(int tempoFim) {
        this.tempoFim = tempoFim;
        this.finalizado = true;
        calcularMetricas();
    }

    public int getTempoEspera() {
        return tempoEspera;
    }

    public int getTempoRetorno() {
        return tempoRetorno;
    }

    public boolean isIniciado() {
        return iniciado;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    @Override
    public String toString() {
        return "Processo{" +
                "id='" + id + '\'' +
                ", tempoChegada=" + tempoChegada +
                ", tempoExecucao=" + tempoExecucao +
                ", tempoRestante=" + tempoRestante +
                ", tempoInicio=" + tempoInicio +
                ", tempoFim=" + tempoFim +
                ", tempoEspera=" + tempoEspera +
                ", tempoRetorno=" + tempoRetorno +
                '}';
    }
}
