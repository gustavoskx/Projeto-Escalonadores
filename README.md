# Simulador de Escalonamento de Processos

Este projeto implementa um simulador de algoritmos de escalonamento de processos com interface gráfica, permitindo visualizar e comparar o desempenho de diferentes algoritmos em ambientes com múltiplas threads.

## Objetivo do Projeto

O objetivo deste simulador é demonstrar o funcionamento de algoritmos de escalonamento de processos em sistemas operacionais, permitindo a visualização do comportamento de cada algoritmo e a comparação de métricas de desempenho como tempo de espera, tempo de turnaround e utilização de recursos.

## Algoritmos Implementados

### Shortest Job First (SJF)
- **Descrição**: Seleciona o processo com o menor tempo de execução entre os processos disponíveis.
- **Características**: Não-preemptivo, minimiza o tempo médio de espera.
- **Implementação**: Uma vez que um processo começa a executar, ele continua até terminar (não preemptivo), o que geralmente resulta em um alto uso de CPU (tipicamente acima de 90%).

### Round Robin (RR)
- **Descrição**: Alterna entre os processos, dando a cada um uma fatia de tempo fixa (quantum).
- **Características**: Preemptivo, distribui o tempo de CPU igualmente entre os processos.
- **Quantum**: 4 unidades de tempo.

## Como Executar a Simulação

1. Inicie a aplicação executando a classe `Principal`.
2. Na interface gráfica, selecione o algoritmo desejado (SJF ou RR).
3. Escolha o número de threads para a simulação (1, 2, 4 ou 6).
4. Clique no botão "Iniciar Simulação" para começar.
5. Observe a execução dos processos na tabela e no gráfico de Gantt.
6. Ao final da simulação, os resultados serão exibidos no painel inferior.

## Exemplos de Saída

Após a execução da simulação, o sistema exibirá:

- **Tabela de Processos**: Mostra os detalhes de cada processo, incluindo ID, tempo de chegada, tempo de execução, tempo de início, tempo de término, tempo de espera e tempo de turnaround.
- **Gráfico de Gantt**: Visualização da execução dos processos em cada thread ao longo do tempo.
- **Métricas de Desempenho**:
  - Tempo médio de espera
  - Tempo médio de turnaround
  - Uso médio de CPU
  - Uso de memória
  - Tempo total de execução da simulação

## Instruções para Rodar no IntelliJ IDEA (Java 17+)

1. Certifique-se de ter o JDK 17 ou superior instalado.
2. Abra o IntelliJ IDEA e selecione "Open" ou "Import Project".
3. Navegue até o diretório do projeto e selecione-o.
4. Aguarde o IntelliJ configurar o projeto.
5. Navegue até a classe `src/Principal.java`.
6. Clique com o botão direito e selecione "Run 'Principal.main()'".

## Requisitos Mínimos

- **Java**: JDK 17 ou superior
- **Memória**: 256MB de RAM
- **Processador**: Qualquer processador moderno (1GHz+)
- **Espaço em Disco**: 10MB para o projeto
- **Sistema Operacional**: Windows, macOS ou Linux com suporte a Java

## Estrutura do Projeto

O projeto está organizado nos seguintes pacotes:

- **model**: Contém a classe `Processo` que representa um processo a ser escalonado.
- **scheduler**: Contém a interface `Escalonador` e suas implementações (`EscalonadorSJF` e `EscalonadorRoundRobin`).
- **simulation**: Contém as classes responsáveis pela simulação (`GeradorDeProcessos` e `MotorDeSimulacao`).
- **gui**: Contém a interface gráfica da aplicação (`JanelaPrincipal`).

## Notas Adicionais

- Os processos são gerados aleatoriamente com tempos de chegada entre 0 e 10 e tempos de execução entre 1 e 10.
- A simulação utiliza threads reais do Java para simular a execução paralela dos processos.
- O código foi desenvolvido para ser simples e direto, priorizando a clareza e a legibilidade.

## Cálculo de Métricas

### Uso de CPU
O uso de CPU é calculado como a proporção entre o tempo total de execução dos processos e o tempo total disponível:

```
usoCPU = (tempoTotalExecucao / tempoTotalDisponivel) * 100.0
```

Onde:
- `tempoTotalExecucao` é a soma dos tempos de execução de todos os processos
- `tempoTotalDisponivel` é o número de threads multiplicado pelo tempo total da simulação

Um uso de CPU de aproximadamente 90-95% é considerado normal e eficiente para o algoritmo SJF, indicando:
1. Baixa sobrecarga de escalonamento
2. Poucos períodos de ociosidade da CPU
3. Eficiência na seleção dos processos mais curtos primeiro
