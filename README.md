# GiroPro — Inteligência em Corridas

App Android nativo para motoristas de **Uber, 99 e inDrive** avaliarem a
rentabilidade de uma oferta **antes de aceitar**: valor por km, valor por hora,
custo estimado, lucro líquido e um semáforo (verde / amarelo / vermelho).

Código, identidade visual e arquitetura próprios. Não contém código, marca nem
elementos proprietários de nenhum concorrente.

---

## Estado atual (MVP em construção)

Este repositório foi iniciado num ambiente **sem Android SDK e sem aparelho**, então
as partes de UI Android ainda **não foram compiladas aqui**. O que foi feito:

| Área | Situação |
|------|----------|
| **Motor de cálculo** (`core/engine`) | ✅ Pronto e **testado** — 10 testes unitários passando em JVM |
| Identidade visual (paleta, logotipo, tema Material 3) | ✅ Implementada |
| Dashboard (Etapa 1) | ✅ Código escrito — compilar no Android Studio |
| Cadastro de veículo + Room/DataStore (Etapa 2) | ✅ Código escrito |
| Semáforo inteligente (Etapa 3) | ✅ No motor + telas |
| Histórico e painel do dia (Etapa 4) | ✅ Básico escrito |
| Janela flutuante / overlay (Etapa 5) | ⚙️ Código real escrito, **não testado em aparelho** |
| Captura automática das ofertas (Etapa 6) | ⛔ **Não implementada** — ver abaixo |
| Assistente de voz | ⏳ Previsto (TextToSpeech) |
| Testes de instrumentação (Etapas 7-8) | ⏳ Dependem de aparelho real |

> **Importante — o motor de cálculo foi validado de verdade.** Os testes em
> `app/src/test/kotlin/.../GiroCalculatorTest.kt` rodaram e passaram. As telas e o
> serviço de overlay são código real, mas precisam ser abertos e compilados no
> Android Studio (requer o Android SDK).

---

## Sobre a captura automática (leia antes de investir)

A leitura automática das ofertas exibidas pelo Uber/99/inDrive **não está
implementada** e está isolada em `feature/capture` retornando `NaoDisponivel`
(nunca inventa valores). Motivos:

- Ler a tela de um app de terceiros exige **serviço de acessibilidade** e/ou
  **MediaProjection + OCR**, recursos nativos que só funcionam em aparelho real.
- Essa leitura **pode violar os Termos de Uso** dessas plataformas e as políticas
  da Google Play para uso da API de acessibilidade. Há risco de bloqueio da conta
  do motorista e de rejeição na loja.

Enquanto essa decisão de produto e a verificação de conformidade não forem
resolvidas, o app funciona pela **tela "Analisar" (entrada/conferência manual)**,
que é o caminho permitido, e pela **janela flutuante** mostrando o resultado.

---

## Requisitos para compilar

- Android Studio (versão recente, Koala ou mais nova)
- JDK 17
- Android SDK com `compileSdk 34`

```bash
# no Android Studio: abra a pasta GiroPro e deixe o Gradle sincronizar
# ou via linha de comando, com o Android SDK instalado:
./gradlew :app:assembleDebug      # gera o APK de debug
./gradlew :app:testDebugUnitTest  # roda os testes do motor
```

O APK sai em `app/build/outputs/apk/debug/app-debug.apk`.

---

## Arquitetura

Clean Architecture leve, em um único módulo `:app` com pacotes separados
(fácil de dividir em módulos Gradle depois):

```
com.giropro
├── core/engine        # motor de cálculo — Kotlin puro, sem Android (testável)
├── core/ui/theme      # paleta, tipografia, tema Material 3
├── data/local         # Room: entidades, DAOs, banco
├── data/datastore     # DataStore: custos e metas
├── data/repo          # repositórios
├── feature/dashboard  # tela inicial
├── feature/calculator # análise manual de oferta
├── feature/vehicle    # cadastro de veículos
├── feature/config     # custos e metas
├── feature/history    # histórico
├── feature/overlay    # janela flutuante (SYSTEM_ALERT_WINDOW)
└── feature/capture    # conectores de captura (pendente de autorização)
```

As regras de negócio (`core/engine`) não dependem de Android, por isso podem ser
testadas por completo em JVM — é o que garante que a matemática está correta.

## Paleta

Preto `#101010` · Amarelo `#F4C847` (destaque) · Branco `#FFFFFF` ·
Cinza `#292929` · Verde `#21B66F` · Vermelho `#E54848`.

## Próximas etapas

1. Compilar no Android Studio e ajustar o que o compilador apontar.
2. Testar a janela flutuante em aparelho real.
3. Assistente de voz (TextToSpeech) e relatórios semanais/mensais.
4. Decisão de produto + conformidade sobre a captura automática.
5. Preparar assinatura e publicação na Google Play.
