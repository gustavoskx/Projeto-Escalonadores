package simulation;

import model.Processo;
import scheduler.Escalonador;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Motor de simulação que executa processos usando múltiplas threads.
 * Mede métricas de desempenho como tempo de espera, tempo de turnaround e uso de CPU.
 */
public class MotorDeSimulacao {
    private Escalonador escalonador;
    private int numeroThreads;
    private int tempoAtual;
    private boolean emExecucao;
    private List<OuvinteSimulacao> ouvintes;
    private ExecutorService executorService;
    private CountDownLatch simulationLatch;
    private AtomicInteger processosCompletados;
    private long tempoInicio;
    private long tempoFim;
    private double usoCPU;
    private long usoMemoria;

    /**
     * Interface para notificar eventos da simulação.
     */
    public interface OuvinteSimulacao {
        void aoIniciarProcesso(Processo processo, int idThread, int tempo);
        void aoFinalizarProcesso(Processo processo, int idThread, int tempo);
        void aoCompletarSimulacao(ResultadoSimulacao resultado);
    }

    /**
     * Classe que contém os resultados da simulação.
     */
    public static class ResultadoSimulacao {
        private final List<Processo> processos;
        private final double tempoMedioEspera;
        private final double tempoMedioRetorno;
        private final double usoCPU;
        private final long usoMemoria;
        private final long tempoExecucao;

        public ResultadoSimulacao(List<Processo> processos, double tempoMedioEspera, 
                               double tempoMedioRetorno, double usoCPU, 
                               long usoMemoria, long tempoExecucao) {
            this.processos = new ArrayList<>(processos);
            this.tempoMedioEspera = tempoMedioEspera;
            this.tempoMedioRetorno = tempoMedioRetorno;
            this.usoCPU = usoCPU;
            this.usoMemoria = usoMemoria;
            this.tempoExecucao = tempoExecucao;
        }

        public List<Processo> getProcessos() {
            return processos;
        }

        public double getTempoMedioEspera() {
            return tempoMedioEspera;
        }

        public double getTempoMedioRetorno() {
            return tempoMedioRetorno;
        }

        public double getUsoCPU() {
            return usoCPU;
        }

        public long getUsoMemoria() {
            return usoMemoria;
        }

        public long getTempoExecucao() {
            return tempoExecucao;
        }
    }

    /**
     * Construtor para o motor de simulação.
     * 
     * @param escalonador Escalonador a ser utilizado
     * @param numeroThreads Número de threads para simular execução paralela
     */
    public MotorDeSimulacao(Escalonador escalonador, int numeroThreads) {
        this.escalonador = escalonador;
        this.numeroThreads = numeroThreads;
        this.tempoAtual = 0;
        this.emExecucao = false;
        this.ouvintes = new ArrayList<>();
        this.processosCompletados = new AtomicInteger(0);
    }

    /**
     * Adiciona um ouvinte para eventos da simulação.
     * 
     * @param ouvinte Ouvinte a ser adicionado
     */
    public void adicionarOuvinte(OuvinteSimulacao ouvinte) {
        ouvintes.add(ouvinte);
    }

    /**
     * Remove um ouvinte de eventos da simulação.
     * 
     * @param ouvinte Ouvinte a ser removido
     */
    public void removerOuvinte(OuvinteSimulacao ouvinte) {
        ouvintes.remove(ouvinte);
    }

    /**
     * Inicia a simulação com os processos atualmente no escalonador.
     */
    public void iniciarSimulacao() {
        if (emExecucao) {
            return;
        }

        emExecucao = true;
        tempoAtual = 0;
        processosCompletados.set(0);

        // Inicializa o pool de threads
        executorService = Executors.newFixedThreadPool(numeroThreads);
        simulationLatch = new CountDownLatch(numeroThreads);

        // Registra o tempo de início
        tempoInicio = System.currentTimeMillis();

        // Inicia as threads de simulação
        for (int i = 0; i < numeroThreads; i++) {
            final int idThread = i;
            executorService.submit(() -> executarThreadSimulacao(idThread));
        }

        // Inicia uma thread para monitorar o término da simulação
        new Thread(() -> {
            try {
                simulationLatch.await();
                tempoFim = System.currentTimeMillis();
                calcularMetricas();
                notificarSimulacaoCompletada();
                emExecucao = false;
                executorService.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Para a simulação em andamento.
     */
    public void pararSimulacao() {
        if (!emExecucao) {
            return;
        }

        emExecucao = false;
        executorService.shutdownNow();
    }

    /**
     * Executa a simulação em uma thread.
     * 
     * @param idThread ID da thread
     */
    private void executarThreadSimulacao(int idThread) {
        try {
            while (emExecucao && !escalonador.estaFinalizado()) {
                Processo processo = null;

                // Sincroniza o acesso ao escalonador e ao tempo atual
                synchronized (escalonador) {
                    processo = escalonador.obterProximoProcesso(tempoAtual);

                    if (processo == null) {
                        // Se não há processos disponíveis, avança o tempo
                        tempoAtual++;
                        continue;
                    }

                    // Notifica que o processo começou a executar
                    notificarProcessoIniciado(processo, idThread, tempoAtual);

                    // Executa o processo por uma unidade de tempo
                    boolean finalizado = processo.executar(tempoAtual);

                    // Se o processo terminou, notifica e incrementa o contador
                    if (finalizado) {
                        notificarProcessoFinalizado(processo, idThread, tempoAtual);
                        processosCompletados.incrementAndGet();
                    }

                    // Avança o tempo
                    tempoAtual++;
                }

                // Simula o tempo de execução real
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            simulationLatch.countDown();
        }
    }

    /**
     * Calcula métricas de desempenho após a simulação.
     */
    private void calcularMetricas() {
        List<Processo> processos = escalonador.obterTodosProcessos();

        // Calcula tempo médio de espera e turnaround
        int tempoTotalEspera = 0;
        int tempoTotalRetorno = 0;
        int tempoTotalExecucao = 0;

        for (Processo processo : processos) {
            tempoTotalEspera += processo.getTempoEspera();
            tempoTotalRetorno += processo.getTempoRetorno();
            tempoTotalExecucao += processo.getTempoExecucao();
        }

        double tempoMedioEspera = processos.isEmpty() ? 0 : (double) tempoTotalEspera / processos.size();
        double tempoMedioRetorno = processos.isEmpty() ? 0 : (double) tempoTotalRetorno / processos.size();

        // Calcula uso de memória baseado nos processos simulados
        // Assumimos que cada processo consome memória proporcional ao seu tempo de execução
        // Usamos uma estimativa de 1MB por unidade de tempo de execução
        usoMemoria = 0;
        for (Processo processo : processos) {
            // Cada processo consome memória base (1MB) + memória adicional baseada no tempo de execução
            usoMemoria += 1024 * 1024; // 1MB base por processo
            usoMemoria += processo.getTempoExecucao() * 1024; // 1KB adicional por unidade de tempo
        }

        // Estimativa de uso de CPU baseada no tempo de execução
        long tempoSimulacao = tempoFim - tempoInicio;
        // Converte o tempo de execução para a mesma unidade dos tempos de turnaround (unidades de tempo da simulação)
        // Assumindo que cada unidade de tempo da simulação é aproximadamente 100ms (conforme Thread.sleep(100))
        double unidadesTempoSimulacao = tempoSimulacao / 100.0;

        // Calcula o uso de CPU como a proporção do tempo total de execução pelo tempo total disponível
        // (tempo total disponível = número de threads * unidades de tempo da simulação)
        double tempoTotalDisponivel = numeroThreads * unidadesTempoSimulacao;
        usoCPU = tempoTotalDisponivel > 0 ? (double) tempoTotalExecucao / tempoTotalDisponivel * 100.0 : 0;
    }

    /**
     * Notifica os ouvintes que um processo começou a executar.
     */
    private void notificarProcessoIniciado(Processo processo, int idThread, int tempo) {
        for (OuvinteSimulacao ouvinte : ouvintes) {
            ouvinte.aoIniciarProcesso(processo, idThread, tempo);
        }
    }

    /**
     * Notifica os ouvintes que um processo terminou de executar.
     */
    private void notificarProcessoFinalizado(Processo processo, int idThread, int tempo) {
        for (OuvinteSimulacao ouvinte : ouvintes) {
            ouvinte.aoFinalizarProcesso(processo, idThread, tempo);
        }
    }

    /**
     * Notifica os ouvintes que a simulação foi concluída.
     */
    private void notificarSimulacaoCompletada() {
        // Recalcula as métricas para garantir consistência
        calcularMetricas();

        List<Processo> processos = escalonador.obterTodosProcessos();

        // Calcula tempo médio de espera e turnaround
        int tempoTotalEspera = 0;
        int tempoTotalRetorno = 0;

        for (Processo processo : processos) {
            tempoTotalEspera += processo.getTempoEspera();
            tempoTotalRetorno += processo.getTempoRetorno();
        }

        double tempoMedioEspera = processos.isEmpty() ? 0 : (double) tempoTotalEspera / processos.size();
        double tempoMedioRetorno = processos.isEmpty() ? 0 : (double) tempoTotalRetorno / processos.size();

        // Cria o objeto de resultado
        ResultadoSimulacao resultado = new ResultadoSimulacao(
            processos, 
            tempoMedioEspera, 
            tempoMedioRetorno, 
            usoCPU, 
            usoMemoria, 
            tempoFim - tempoInicio
        );

        // Notifica os ouvintes
        for (OuvinteSimulacao ouvinte : ouvintes) {
            ouvinte.aoCompletarSimulacao(resultado);
        }
    }

    /**
     * Retorna o escalonador utilizado pela simulação.
     * 
     * @return Escalonador
     */
    public Escalonador getEscalonador() {
        return escalonador;
    }

    /**
     * Define um novo escalonador para a simulação.
     * 
     * @param escalonador Novo escalonador
     */
    public void setEscalonador(Escalonador escalonador) {
        if (!emExecucao) {
            this.escalonador = escalonador;
        }
    }

    /**
     * Retorna o número de threads utilizadas na simulação.
     * 
     * @return Número de threads
     */
    public int getNumeroThreads() {
        return numeroThreads;
    }

    /**
     * Define um novo número de threads para a simulação.
     * 
     * @param numeroThreads Novo número de threads
     */
    public void setNumeroThreads(int numeroThreads) {
        if (!emExecucao) {
            this.numeroThreads = numeroThreads;
        }
    }

    /**
     * Verifica se a simulação está em execução.
     * 
     * @return true se a simulação está em execução, false caso contrário
     */
    public boolean isEmExecucao() {
        return emExecucao;
    }
}
