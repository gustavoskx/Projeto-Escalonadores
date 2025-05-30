package gui;

import model.Processo;
import scheduler.EscalonadorRoundRobin;
import scheduler.EscalonadorSJF;
import scheduler.Escalonador;
import simulation.GeradorDeProcessos;
import simulation.MotorDeSimulacao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Janela principal da aplicação que contém a interface gráfica para a simulação.
 */
public class JanelaPrincipal extends JFrame implements MotorDeSimulacao.OuvinteSimulacao {
    private static final int QUANTIDADE_PROCESSOS_PADRAO = 10;

    // Componentes da GUI
    private JComboBox<String> comboBoxAlgoritmo;
    private JComboBox<Integer> comboBoxNumeroThreads;
    private JButton botaoIniciar;
    private JButton botaoParar;
    private JTable tabelaProcessos;
    private DefaultTableModel modeloTabela;
    private JPanel painelGantt;
    private JTextArea areaResultados;

    // Componentes da simulação
    private GeradorDeProcessos geradorDeProcessos;
    private Escalonador escalonador;
    private MotorDeSimulacao motorDeSimulacao;
    private Map<String, Color> coresProcessos;
    private Map<Integer, java.util.List<EventoExecucao>> eventosExecucaoPorThread;

    /**
     * Classe para armazenar eventos de execução para o gráfico de Gantt.
     */
    private static class EventoExecucao {
        private final String idProcesso;
        private final int tempoInicio;
        private final int tempoFim;

        public EventoExecucao(String idProcesso, int tempoInicio, int tempoFim) {
            this.idProcesso = idProcesso;
            this.tempoInicio = tempoInicio;
            this.tempoFim = tempoFim;
        }

        public String getIdProcesso() {
            return idProcesso;
        }

        public int getTempoInicio() {
            return tempoInicio;
        }

        public int getTempoFim() {
            return tempoFim;
        }
    }

    /**
     * Construtor da janela principal.
     */
    public JanelaPrincipal() {
        setTitle("Simulador de Escalonamento de Processos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Inicializa componentes da simulação
        geradorDeProcessos = new GeradorDeProcessos();
        escalonador = new EscalonadorRoundRobin(); // Algoritmo padrão
        motorDeSimulacao = new MotorDeSimulacao(escalonador, 2); // 2 threads por padrão
        motorDeSimulacao.adicionarOuvinte(this);
        coresProcessos = new HashMap<>();
        eventosExecucaoPorThread = new HashMap<>();

        // Inicializa a interface gráfica
        inicializarUI();
    }

    /**
     * Inicializa os componentes da interface gráfica.
     */
    private void inicializarUI() {
        // Painel principal com layout de borda
        JPanel painelPrincipal = new JPanel(new BorderLayout());

        // Painel de controles no topo
        JPanel painelControles = criarPainelControles();
        painelPrincipal.add(painelControles, BorderLayout.NORTH);

        // Painel central com tabela de processos e gráfico de Gantt
        JPanel painelCentral = new JPanel(new GridLayout(2, 1));

        // Tabela de processos
        JPanel painelTabela = criarPainelTabela();
        painelCentral.add(painelTabela);

        // Gráfico de Gantt
        painelGantt = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharGraficoGantt(g);
            }
        };
        painelGantt.setBackground(Color.WHITE);
        painelGantt.setBorder(BorderFactory.createTitledBorder("Gráfico de Gantt"));
        painelCentral.add(new JScrollPane(painelGantt));

        painelPrincipal.add(painelCentral, BorderLayout.CENTER);

        // Painel de resultados na parte inferior
        JPanel painelResultados = criarPainelResultados();
        painelPrincipal.add(painelResultados, BorderLayout.SOUTH);

        // Adiciona o painel principal à janela
        add(painelPrincipal);
    }

    /**
     * Cria o painel de controles.
     */
    private JPanel criarPainelControles() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painel.setBorder(BorderFactory.createTitledBorder("Controles"));

        // Seleção de algoritmo
        painel.add(new JLabel("Algoritmo:"));
        comboBoxAlgoritmo = new JComboBox<>(new String[]{"Round Robin (RR)", "Shortest Job First (SJF)"});
        comboBoxAlgoritmo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarEscalonador();
            }
        });
        painel.add(comboBoxAlgoritmo);

        // Seleção de número de threads
        painel.add(new JLabel("Threads:"));
        comboBoxNumeroThreads = new JComboBox<>(new Integer[]{1, 2, 4, 6});
        comboBoxNumeroThreads.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                atualizarNumeroThreads();
            }
        });
        painel.add(comboBoxNumeroThreads);

        // Botões de controle
        botaoIniciar = new JButton("Iniciar Simulação");
        botaoIniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSimulacao();
            }
        });
        painel.add(botaoIniciar);

        botaoParar = new JButton("Parar Simulação");
        botaoParar.setEnabled(false);
        botaoParar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pararSimulacao();
            }
        });
        painel.add(botaoParar);

        return painel;
    }

    /**
     * Cria o painel com a tabela de processos.
     */
    private JPanel criarPainelTabela() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Processos"));

        // Cria o modelo da tabela
        String[] nomesColunas = {"ID", "Chegada", "Execução", "Início", "Término", "Espera (até início)", "Turnaround (total)"};
        modeloTabela = new DefaultTableModel(nomesColunas, 0);
        tabelaProcessos = new JTable(modeloTabela);

        // Adiciona a tabela a um painel com scroll
        JScrollPane painelRolagem = new JScrollPane(tabelaProcessos);
        painel.add(painelRolagem, BorderLayout.CENTER);

        return painel;
    }

    /**
     * Cria o painel de resultados.
     */
    private JPanel criarPainelResultados() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Resultados"));

        areaResultados = new JTextArea(5, 40);
        areaResultados.setEditable(false);

        JScrollPane painelRolagem = new JScrollPane(areaResultados);
        painel.add(painelRolagem, BorderLayout.CENTER);

        return painel;
    }

    /**
     * Atualiza o escalonador com base na seleção do usuário.
     */
    private void atualizarEscalonador() {
        if (motorDeSimulacao.isEmExecucao()) {
            return;
        }

        int indiceSelecao = comboBoxAlgoritmo.getSelectedIndex();
        if (indiceSelecao == 0) {
            escalonador = new EscalonadorRoundRobin();
        } else {
            escalonador = new EscalonadorSJF();
        }

        motorDeSimulacao.setEscalonador(escalonador);
    }

    /**
     * Atualiza o número de threads com base na seleção do usuário.
     */
    private void atualizarNumeroThreads() {
        if (motorDeSimulacao.isEmExecucao()) {
            return;
        }

        int numeroThreads = (Integer) comboBoxNumeroThreads.getSelectedItem();
        motorDeSimulacao.setNumeroThreads(numeroThreads);
    }

    /**
     * Inicia a simulação.
     */
    private void iniciarSimulacao() {
        // Limpa dados anteriores
        modeloTabela.setRowCount(0);
        eventosExecucaoPorThread.clear();
        coresProcessos.clear();
        areaResultados.setText("");

        // Gera processos aleatórios
        List<Processo> processos = geradorDeProcessos.gerarProcessos(QUANTIDADE_PROCESSOS_PADRAO);

        // Atribui cores aleatórias para cada processo
        for (Processo processo : processos) {
            coresProcessos.put(processo.getId(), gerarCorAleatoria());
        }

        // Adiciona processos ao escalonador
        escalonador.reiniciar();
        escalonador.adicionarProcessos(processos);

        // Atualiza a tabela com os processos
        atualizarTabelaProcessos(processos);

        // Inicializa eventos de execução para cada thread
        for (int i = 0; i < motorDeSimulacao.getNumeroThreads(); i++) {
            eventosExecucaoPorThread.put(i, new java.util.ArrayList<>());
        }

        // Atualiza estado dos botões
        botaoIniciar.setEnabled(false);
        botaoParar.setEnabled(true);
        comboBoxAlgoritmo.setEnabled(false);
        comboBoxNumeroThreads.setEnabled(false);

        // Inicia a simulação
        motorDeSimulacao.iniciarSimulacao();
    }

    /**
     * Para a simulação em andamento.
     */
    private void pararSimulacao() {
        motorDeSimulacao.pararSimulacao();

        // Atualiza estado dos botões
        botaoIniciar.setEnabled(true);
        botaoParar.setEnabled(false);
        comboBoxAlgoritmo.setEnabled(true);
        comboBoxNumeroThreads.setEnabled(true);
    }

    /**
     * Atualiza a tabela de processos.
     * Garante que nenhum valor negativo seja exibido.
     */
    private void atualizarTabelaProcessos(List<Processo> processos) {
        modeloTabela.setRowCount(0);

        for (Processo processo : processos) {
            // Garante que nenhum valor negativo seja exibido
            int tempoChegada = Math.max(0, processo.getTempoChegada());
            int tempoExecucao = Math.max(0, processo.getTempoExecucao());
            Object tempoInicio = processo.isIniciado() ? Math.max(0, processo.getTempoInicio()) : "-";
            Object tempoFim = processo.isFinalizado() ? Math.max(0, processo.getTempoFim()) : "-";
            Object tempoEspera = processo.isFinalizado() ? Math.max(0, processo.getTempoEspera()) : "-";
            Object tempoRetorno = processo.isFinalizado() ? Math.max(0, processo.getTempoRetorno()) : "-";

            Object[] linha = {
                processo.getId(),
                tempoChegada,
                tempoExecucao,
                tempoInicio,
                tempoFim,
                tempoEspera,
                tempoRetorno
            };
            modeloTabela.addRow(linha);
        }
    }

    /**
     * Desenha o gráfico de Gantt com melhorias visuais.
     */
    private void desenharGraficoGantt(Graphics g) {
        int largura = painelGantt.getWidth();
        int altura = painelGantt.getHeight();
        int alturaThread = altura / (motorDeSimulacao.getNumeroThreads() + 2); // +2 para deixar espaço para legenda
        int tempoMaximo = 0;
        int tempoChegadaMinimo = Integer.MAX_VALUE;

        // Obtém todos os processos para mostrar informações de chegada
        List<Processo> todosProcessos = escalonador.obterTodosProcessos();

        // Encontra o tempo máximo para dimensionar o gráfico e o tempo mínimo de chegada
        for (Processo processo : todosProcessos) {
            tempoChegadaMinimo = Math.min(tempoChegadaMinimo, processo.getTempoChegada());
        }

        for (List<EventoExecucao> eventos : eventosExecucaoPorThread.values()) {
            for (EventoExecucao evento : eventos) {
                tempoMaximo = Math.max(tempoMaximo, evento.getTempoFim());
            }
        }

        // Se não houver eventos, não desenha nada
        if (tempoMaximo == 0) {
            return;
        }

        // Fator de escala para o eixo do tempo
        double escalaTempo = (largura - 100) / (double) tempoMaximo;

        // Desenha o eixo do tempo
        g.setColor(Color.BLACK);
        g.drawLine(50, altura - 40, largura - 50, altura - 40);

        // Desenha as marcações de tempo
        for (int t = 0; t <= tempoMaximo; t += 5) {
            int x = 50 + (int)(t * escalaTempo);
            g.drawLine(x, altura - 45, x, altura - 35);
            g.drawString(String.valueOf(t), x - 5, altura - 25);
        }

        // Desenha a linha de chegada dos processos
        int yChegada = altura - 60;
        g.drawString("Chegada:", 5, yChegada);

        // Desenha marcadores de chegada para cada processo
        for (Processo processo : todosProcessos) {
            int xChegada = 50 + (int)(processo.getTempoChegada() * escalaTempo);
            Color corProcesso = coresProcessos.getOrDefault(processo.getId(), Color.GRAY);

            // Desenha um triângulo para marcar a chegada
            g.setColor(corProcesso);
            int[] xPoints = {xChegada, xChegada - 5, xChegada + 5};
            int[] yPoints = {yChegada - 10, yChegada, yChegada};
            g.fillPolygon(xPoints, yPoints, 3);

            // Desenha o ID do processo
            g.setColor(Color.BLACK);
            g.drawString(processo.getId(), xChegada - 5, yChegada - 15);
        }

        // Desenha os eventos de execução para cada thread
        for (int idThread = 0; idThread < motorDeSimulacao.getNumeroThreads(); idThread++) {
            int y = 30 + idThread * alturaThread;

            // Desenha o rótulo da thread
            g.setColor(Color.BLACK);
            g.drawString("Thread " + idThread, 5, y + alturaThread / 2);

            // Desenha os eventos de execução
            List<EventoExecucao> eventos = eventosExecucaoPorThread.get(idThread);
            if (eventos != null) {
                for (EventoExecucao evento : eventos) {
                    int x1 = 50 + (int)(evento.getTempoInicio() * escalaTempo);
                    int x2 = 50 + (int)(evento.getTempoFim() * escalaTempo);
                    int larguraEvento = Math.max(x2 - x1, 10); // Garante uma largura mínima visível

                    // Desenha o retângulo do evento
                    Color corProcesso = coresProcessos.getOrDefault(evento.getIdProcesso(), Color.GRAY);
                    g.setColor(corProcesso);
                    g.fillRect(x1, y, larguraEvento, alturaThread - 10);

                    // Desenha a borda do retângulo
                    g.setColor(Color.BLACK);
                    g.drawRect(x1, y, larguraEvento, alturaThread - 10);

                    // Desenha o ID do processo
                    g.drawString(evento.getIdProcesso(), x1 + 5, y + alturaThread / 2);

                    // Desenha o tempo de início e fim
                    g.setFont(new Font("Arial", Font.PLAIN, 9));
                    g.drawString("" + evento.getTempoInicio(), x1, y - 2);
                    g.drawString("" + evento.getTempoFim(), x1 + larguraEvento, y - 2);
                    g.setFont(new Font("Arial", Font.PLAIN, 12)); // Restaura a fonte
                }
            }
        }

        // Desenha a legenda
        int yLegenda = altura - 15;
        g.setColor(Color.BLACK);
        g.drawString("Legenda:", 50, yLegenda);

        int xLegenda = 120;
        for (Map.Entry<String, Color> entry : coresProcessos.entrySet()) {
            String idProcesso = entry.getKey();
            Color corProcesso = entry.getValue();

            // Desenha um quadrado com a cor do processo
            g.setColor(corProcesso);
            g.fillRect(xLegenda, yLegenda - 10, 15, 10);
            g.setColor(Color.BLACK);
            g.drawRect(xLegenda, yLegenda - 10, 15, 10);

            // Desenha o ID do processo
            g.drawString(idProcesso, xLegenda + 20, yLegenda);

            xLegenda += 70; // Espaço entre itens da legenda

            // Se chegou ao final da linha, quebra para a próxima
            if (xLegenda > largura - 100) {
                xLegenda = 120;
                yLegenda += 15;
            }
        }
    }

    /**
     * Gera uma cor aleatória para um processo.
     */
    private Color gerarCorAleatoria() {
        return new Color(
            128 + (int)(Math.random() * 128),
            128 + (int)(Math.random() * 128),
            128 + (int)(Math.random() * 128)
        );
    }

    // Implementação dos métodos da interface OuvinteSimulacao

    @Override
    public void aoIniciarProcesso(Processo processo, int idThread, int tempo) {
        // Registra o início da execução para o gráfico de Gantt
        EventoExecucao evento = new EventoExecucao(processo.getId(), tempo, tempo + 1);
        eventosExecucaoPorThread.get(idThread).add(evento);

        // Atualiza a interface gráfica
        SwingUtilities.invokeLater(() -> {
            atualizarTabelaProcessos(escalonador.obterTodosProcessos());
            painelGantt.repaint();
        });
    }

    @Override
    public void aoFinalizarProcesso(Processo processo, int idThread, int tempo) {
        // Atualiza a interface gráfica
        SwingUtilities.invokeLater(() -> {
            atualizarTabelaProcessos(escalonador.obterTodosProcessos());
            painelGantt.repaint();
        });
    }

    @Override
    public void aoCompletarSimulacao(MotorDeSimulacao.ResultadoSimulacao resultado) {
        // Atualiza a interface gráfica
        SwingUtilities.invokeLater(() -> {
            // Atualiza a tabela de processos
            atualizarTabelaProcessos(resultado.getProcessos());

            // Garante que nenhum valor negativo seja exibido nos resultados
            double tempoMedioEspera = Math.max(0, resultado.getTempoMedioEspera());
            double tempoMedioRetorno = Math.max(0, resultado.getTempoMedioRetorno());
            double usoCPU = Math.max(0, resultado.getUsoCPU());
            long usoMemoria = Math.max(0, resultado.getUsoMemoria());
            long tempoExecucao = Math.max(0, resultado.getTempoExecucao());

            // Atualiza o painel de resultados
            StringBuilder sb = new StringBuilder();
            sb.append("Algoritmo: ").append(escalonador.obterNome()).append("\n");
            sb.append("Threads: ").append(motorDeSimulacao.getNumeroThreads()).append("\n");
            sb.append("Tempo médio de espera (até início): ").append(String.format("%.2f", tempoMedioEspera)).append("\n");
            sb.append("Tempo médio de turnaround (total): ").append(String.format("%.2f", tempoMedioRetorno)).append("\n");
            sb.append("Uso de CPU: ").append(String.format("%.2f%%", usoCPU)).append("\n");
            sb.append("Uso de memória: ").append(usoMemoria / 1024).append(" KB\n");
            sb.append("Tempo de execução: ").append(tempoExecucao).append(" ms\n");

            areaResultados.setText(sb.toString());

            // Atualiza estado dos botões
            botaoIniciar.setEnabled(true);
            botaoParar.setEnabled(false);
            comboBoxAlgoritmo.setEnabled(true);
            comboBoxNumeroThreads.setEnabled(true);

            // Redesenha o gráfico de Gantt
            painelGantt.repaint();
        });
    }
}
