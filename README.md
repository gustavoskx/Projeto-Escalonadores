
# Explicação Detalhada do Simulador de Escalonamento de Processos

## Visão Geral do Sistema

Este simulador implementa um sistema de escalonamento de processos que demonstra como diferentes algoritmos de escalonamento funcionam em um ambiente de múltiplas CPUs (threads). O sistema é composto por vários componentes principais:

1. **Processos**: Representações de tarefas que precisam ser executadas pelo sistema
2. **Escalonadores**: Implementações de algoritmos que decidem qual processo deve ser executado
3. **Motor de Simulação**: Gerencia a execução dos processos usando múltiplas threads
4. **Gerador de Processos**: Cria conjuntos de processos com características aleatórias
5. **Interface Gráfica**: Permite visualizar e controlar a simulação

## Representação de Processos

A classe `Processo` (em `model/Processo.java`) é o bloco fundamental do sistema. Cada processo contém:

```java
private String id;              // Identificador do processo (P1, P2, etc.)
private int tempoChegada;       // Quando o processo chega ao sistema
private int tempoExecucao;      // Tempo total necessário para completar o processo
private int tempoRestante;      // Tempo que ainda falta para terminar
private int tempoInicio;        // Quando o processo começou a executar
private int tempoFim;           // Quando o processo terminou
private int tempoEspera;        // Quanto tempo o processo esperou na fila
private int tempoRetorno;       // Tempo total desde a chegada até a conclusão
private boolean iniciado;       // Se o processo já começou a executar
private boolean finalizado;     // Se o processo já terminou
```

A lógica principal de um processo está no método `executar()`:

```java
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
```

Este método:
1. Marca o processo como iniciado se for a primeira vez que está sendo executado
2. Reduz o tempo restante em uma unidade
3. Se o tempo restante chegar a zero, marca o processo como finalizado e calcula métricas
4. Retorna `true` se o processo terminou nesta execução, ou `false` caso contrário

As métricas são calculadas no método `calcularMetricas()`:

```java
private void calcularMetricas() {
    // Calcula o tempo de turnaround (tempo total desde a chegada até a conclusão)
    tempoRetorno = Math.max(0, tempoFim - tempoChegada);

    // Calcula o tempo de espera (tempo total que o processo esperou)
    // Tempo de espera = Tempo de retorno - Tempo de execução
    tempoEspera = Math.max(0, tempoRetorno - tempoExecucao);
}
```

## Algoritmos de Escalonamento

O sistema implementa dois algoritmos de escalonamento clássicos:

### 1. Shortest Job First (SJF)

Implementado na classe `EscalonadorSJF`, este algoritmo seleciona sempre o processo com o menor tempo de execução total. É um algoritmo não preemptivo, o que significa que uma vez que um processo começa a executar, ele continua até terminar.

A lógica principal está no método `obterProximoProcesso()`:

```java
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
```

Este método:
1. Verifica se já existe um processo em execução e, se não terminou, continua com ele
2. Se o processo atual terminou, limpa a referência
3. Atualiza a fila de processos prontos com os que chegaram até o momento atual
4. Seleciona o processo com menor tempo de execução usando `stream().min()`
5. Remove o processo selecionado da fila e o marca como em execução

### 2. Round Robin (RR)

Implementado na classe `EscalonadorRoundRobin`, este algoritmo alterna entre os processos, dando a cada um uma fatia de tempo fixa chamada "quantum". Se um processo não termina dentro do seu quantum, ele é colocado de volta na fila para continuar mais tarde.

A lógica principal está no método `obterProximoProcesso()`:

```java
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
```

Este método:
1. Atualiza a fila de processos prontos
2. Se o processo atual ainda tem tempo de quantum e não terminou, continua com ele
3. Se o processo atual esgotou seu quantum mas não terminou, coloca-o de volta na fila
4. Pega o próximo processo da fila
5. Reinicia o contador de quantum para o novo processo

## Motor de Simulação

O `MotorDeSimulacao` é o coração do sistema. Ele gerencia a execução dos processos usando múltiplas threads para simular CPUs paralelas.

A lógica principal está no método `executarThreadSimulacao()`:

```java
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
```

Este método:
1. Executa em loop enquanto a simulação estiver ativa e houver processos para executar
2. Sincroniza o acesso ao escalonador para evitar condições de corrida
3. Obtém o próximo processo do escalonador
4. Se não houver processos disponíveis, avança o tempo
5. Executa o processo por uma unidade de tempo
6. Se o processo terminou, notifica os ouvintes
7. Avança o tempo da simulação
8. Dorme por 100ms para simular o tempo real de execução

O motor também calcula métricas importantes após a simulação:

```java
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

    // Cálculos para uso de CPU e memória...
}
```

## Gerador de Processos

A classe `GeradorDeProcessos` cria conjuntos de processos com características aleatórias:

```java
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
```

Este método:
1. Cria uma lista vazia para armazenar os processos
2. Para cada processo:
   - Define o tempo de chegada (o primeiro sempre chega no tempo 0)
   - Gera um tempo de execução aleatório entre 1 e 10
   - Cria o processo com um ID sequencial
3. Retorna a lista de processos gerados

## Fluxo de Execução da Simulação

Vamos entender o fluxo completo de uma simulação:

1. **Inicialização**:
   - O usuário seleciona o algoritmo de escalonamento (SJF ou Round Robin)
   - O usuário define o número de threads (CPUs) para a simulação
   - O sistema gera um conjunto de processos aleatórios

2. **Início da Simulação**:
   - O motor de simulação cria um pool de threads
   - Cada thread representa uma CPU que pode executar processos
   - O tempo da simulação começa em 0

3. **Ciclo de Execução**:
   - Para cada unidade de tempo:
     - Cada thread solicita um processo ao escalonador
     - O escalonador decide qual processo deve ser executado em cada thread
     - Os processos são executados por uma unidade de tempo
     - Se um processo termina, suas métricas são calculadas
     - O tempo avança

4. **Término da Simulação**:
   - A simulação termina quando todos os processos são concluídos
   - O sistema calcula métricas finais como tempo médio de espera e tempo médio de retorno
   - Os resultados são exibidos na interface gráfica

## Diferenças Entre os Algoritmos

### SJF (Shortest Job First)
- Seleciona sempre o processo com menor tempo de execução total
- É não preemptivo: uma vez que um processo começa, ele continua até terminar
- Tende a minimizar o tempo médio de espera
- Pode causar "starvation" (inanição) de processos longos se processos curtos continuarem chegando

### Round Robin
- Alterna entre os processos, dando a cada um uma fatia de tempo fixa (quantum)
- É preemptivo: interrompe processos que não terminam dentro do quantum
- Distribui o tempo de CPU de forma mais equitativa
- Bom para sistemas interativos onde o tempo de resposta é importante
- O desempenho depende muito do valor do quantum:
  - Quantum muito pequeno: muita troca de contexto (overhead)
  - Quantum muito grande: se aproxima do FCFS (First-Come, First-Served)

## Exemplo de Execução

Vamos considerar um exemplo com 3 processos:
- P1: Chega no tempo 0, precisa de 5 unidades de tempo
- P2: Chega no tempo 2, precisa de 3 unidades de tempo
- P3: Chega no tempo 4, precisa de 1 unidade de tempo

### Com SJF:
1. Tempo 0: P1 começa a executar (único processo disponível)
2. Tempo 2: P2 chega, mas P1 continua (SJF é não preemptivo)
3. Tempo 4: P3 chega, mas P1 continua
4. Tempo 5: P1 termina, P3 começa (menor tempo de execução)
5. Tempo 6: P3 termina, P2 começa
6. Tempo 9: P2 termina, todos os processos concluídos

### Com Round Robin (quantum = 2):
1. Tempo 0: P1 começa a executar
2. Tempo 2: P1 é interrompido após seu quantum, P2 chega e começa
3. Tempo 4: P2 é interrompido, P3 chega, P1 continua
4. Tempo 6: P1 é interrompido, P3 começa
5. Tempo 7: P3 termina, P2 continua
6. Tempo 9: P2 termina, P1 continua
7. Tempo 10: P1 termina, todos os processos concluídos

## Conclusão

Este simulador demonstra de forma visual e interativa como diferentes algoritmos de escalonamento afetam a execução de processos em um sistema operacional. Ele permite comparar o desempenho de algoritmos como SJF e Round Robin em termos de métricas importantes como tempo médio de espera e tempo de retorno.

A implementação usa conceitos avançados de programação Java como threads, sincronização, streams e interfaces gráficas Swing para criar uma simulação realista e educativa do escalonamento de processos.
