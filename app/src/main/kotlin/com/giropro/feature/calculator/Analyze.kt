package com.giropro.feature.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.giropro.GiroProApp
import com.giropro.core.engine.Analise
import com.giropro.core.engine.Classificacao
import com.giropro.core.engine.GiroCalculator
import com.giropro.core.engine.Oferta
import com.giropro.core.engine.TipoCombustivel
import com.giropro.core.engine.Veiculo
import com.giropro.core.ui.theme.GiroAmarelo
import com.giropro.core.ui.theme.GiroVerde
import com.giropro.core.ui.theme.GiroVermelho
import com.giropro.data.local.OfertaHistoricoEntity
import com.giropro.data.repo.toEngine
import com.giropro.feature.GiroVmFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AnalyzeViewModel(private val container: GiroProApp.Container) : ViewModel() {

    val veiculoAtivo = container.veiculos.ativo

    /** Veículo padrão quando nenhum está cadastrado — claramente um exemplo editável. */
    private val veiculoPadrao = Veiculo(
        nome = "Exemplo", combustivel = TipoCombustivel.GASOLINA,
        consumoKmPorLitro = 12.0, manutencaoPorKm = 0.12, pneusPorKm = 0.04,
    )

    suspend fun analisar(oferta: Oferta): Analise {
        val v = container.veiculos.ativo.first()?.toEngine() ?: veiculoPadrao
        val custos = container.config.custos.first()
        val metas = container.config.metas.first()
        return GiroCalculator.analisar(oferta, v, custos, metas)
    }

    fun salvarNoHistorico(oferta: Oferta, a: Analise, realizada: Boolean) {
        viewModelScope.launch {
            container.historico.registrar(
                OfertaHistoricoEntity(
                    timestamp = System.currentTimeMillis(),
                    plataforma = oferta.plataforma,
                    valor = a.valor,
                    distanciaTotalKm = a.distanciaTotalKm,
                    tempoMin = a.tempoTotalMin,
                    custoEstimado = a.custoOperacional,
                    lucroEstimado = a.lucroEstimado,
                    classificacao = a.classificacao.name,
                    realizada = realizada,
                ),
            )
        }
    }
}

@Composable
fun AnalyzeScreen(onMostrarNaJanela: (Analise, String) -> Unit) {
    val vm: AnalyzeViewModel = viewModel(factory = GiroVmFactory)

    var plataforma by remember { mutableStateOf("Uber") }
    var valor by remember { mutableStateOf("") }
    var distDestino by remember { mutableStateOf("") }
    var distPassageiro by remember { mutableStateOf("") }
    var tempo by remember { mutableStateOf("") }
    var analise by remember { mutableStateOf<Analise?>(null) }

    val scope = androidx.compose.runtime.rememberCoroutineScope()

    fun montarOferta() = Oferta(
        plataforma = plataforma,
        valor = valor.toBrDouble(),
        distanciaAtePassageiroKm = distPassageiro.toBrDouble(),
        distanciaDestinoKm = distDestino.toBrDouble(),
        tempoEstimadoMin = tempo.toBrDouble(),
    )

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Analisar corrida", style = MaterialTheme.typography.titleLarge, color = GiroAmarelo, fontWeight = FontWeight.Bold)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Uber", "99", "inDrive").forEach { p ->
                Button(
                    onClick = { plataforma = p },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (plataforma == p) GiroAmarelo else MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.weight(1f),
                ) { Text(p, color = if (plataforma == p) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) }
            }
        }

        Campo("Valor oferecido (R$)", valor) { valor = it }
        Campo("Distância da corrida (km)", distDestino) { distDestino = it }
        Campo("Distância até o passageiro (km)", distPassageiro) { distPassageiro = it }
        Campo("Tempo estimado total (min)", tempo) { tempo = it }

        Button(
            onClick = { scope.launch { analise = vm.analisar(montarOferta()) } },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = GiroAmarelo),
        ) { Text("CALCULAR", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }

        analise?.let { a ->
            ResultadoCard(a)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { vm.salvarNoHistorico(montarOferta(), a, realizada = false) },
                    modifier = Modifier.weight(1f),
                ) { Text("Salvar") }
                Button(
                    onClick = { onMostrarNaJanela(a, plataforma) },
                    modifier = Modifier.weight(1f),
                ) { Text("Ver na janela") }
            }
        }
    }
}

@Composable
private fun Campo(rotulo: String, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(rotulo) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResultadoCard(a: Analise) {
    val cor = when (a.classificacao) {
        Classificacao.VERDE -> GiroVerde
        Classificacao.AMARELO -> GiroAmarelo
        Classificacao.VERMELHO -> GiroVermelho
    }
    val rotulo = when (a.classificacao) {
        Classificacao.VERDE -> "BOA CORRIDA"
        Classificacao.AMARELO -> "RENTABILIDADE MÉDIA"
        Classificacao.VERMELHO -> "ABAIXO DA META"
    }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(rotulo, color = cor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Linha("Lucro estimado", brl(a.lucroEstimado), if (a.lucroEstimado < 0) GiroVermelho else GiroVerde)
            Linha("Valor por km", brl(a.valorPorKm))
            Linha("Valor por hora", brl(a.valorPorHora))
            Linha("Custo estimado", brl(a.custoOperacional))
            Linha("Rentabilidade por km", brl(a.lucroPorKm))
        }
    }
}

@Composable
private fun Linha(k: String, v: String, cor: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(v, color = if (cor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else cor, fontWeight = FontWeight.SemiBold)
    }
}

internal fun String.toBrDouble(): Double =
    trim().replace(".", "").replace(",", ".").toDoubleOrNull()
        ?: trim().toDoubleOrNull() ?: 0.0

internal fun brl(v: Double): String = "R$ %.2f".format(v)
