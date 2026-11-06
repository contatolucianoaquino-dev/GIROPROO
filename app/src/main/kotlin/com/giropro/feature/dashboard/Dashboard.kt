package com.giropro.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.giropro.GiroProApp
import com.giropro.core.ui.theme.GiroAmarelo
import com.giropro.core.ui.theme.GiroVerde
import com.giropro.feature.GiroVmFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class DashboardState(
    val veiculoAtivo: String = "Nenhum veículo cadastrado",
    val ganhoHoje: Double = 0.0,
    val lucroHoje: Double = 0.0,
    val kmHoje: Double = 0.0,
    val horasHoje: Double = 0.0,
    val analisadasHoje: Int = 0,
)

class DashboardViewModel(container: GiroProApp.Container) : ViewModel() {

    private fun inicioDoDia(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    val state = combine(
        container.veiculos.ativo,
        container.historico.desde(inicioDoDia()),
    ) { veiculo, ofertas ->
        val realizadas = ofertas.filter { it.realizada }
        DashboardState(
            veiculoAtivo = veiculo?.nome ?: "Nenhum veículo cadastrado",
            ganhoHoje = realizadas.sumOf { it.valor },
            lucroHoje = realizadas.sumOf { it.lucroEstimado },
            kmHoje = realizadas.sumOf { it.distanciaTotalKm },
            horasHoje = realizadas.sumOf { it.tempoMin } / 60.0,
            analisadasHoje = ofertas.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())
}

private data class Atalho(val titulo: String, val rota: String)

@Composable
fun DashboardScreen(
    monitorando: Boolean,
    onToggleMonitor: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val vm: DashboardViewModel = viewModel(factory = GiroVmFactory)
    val s by vm.state.collectAsState()

    val atalhos = listOf(
        Atalho("Analisar corrida", "analisar"),
        Atalho("Configurar veículo", "veiculo"),
        Atalho("Histórico", "historico"),
        Atalho("Relatórios", "historico"),
        Atalho("Configurações", "config"),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("GIRO", style = MaterialTheme.typography.headlineLarge, color = GiroAmarelo, fontWeight = FontWeight.Black)
        }
        item {
            Text("PRO · Inteligência em Corridas", color = MaterialTheme.colorScheme.onBackground)
        }
        item { Spacer(Modifier.height(4.dp)) }
        item {
            Button(
                onClick = onToggleMonitor,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (monitorando) GiroVerde else GiroAmarelo,
                ),
            ) {
                Text(
                    if (monitorando) "MONITORANDO — TOQUE PARA PARAR" else "INICIAR MONITORAMENTO",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        item {
            Text(
                "Status: " + if (monitorando) "ativo" else "parado",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { InfoCard("Veículo", s.veiculoAtivo) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard("Ganhos do dia", brl(s.ganhoHoje), Modifier.weight(1f))
                InfoCard("Lucro estimado", brl(s.lucroHoje), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard("Km hoje", "%.0f km".format(s.kmHoje), Modifier.weight(1f))
                InfoCard("Horas", "%.1f h".format(s.horasHoje), Modifier.weight(1f))
            }
        }
        item { InfoCard("Corridas analisadas hoje", s.analisadasHoje.toString()) }
        item { Text("Atalhos", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(atalhos) { a ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(a.rota) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Text(a.titulo, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun InfoCard(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(titulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            Text(valor, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

internal fun brl(v: Double): String = "R$ %.2f".format(v)
