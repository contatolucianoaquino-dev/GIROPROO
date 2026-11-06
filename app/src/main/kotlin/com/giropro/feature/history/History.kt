package com.giropro.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.giropro.GiroProApp
import com.giropro.core.engine.Classificacao
import com.giropro.core.ui.theme.GiroAmarelo
import com.giropro.core.ui.theme.GiroVerde
import com.giropro.core.ui.theme.GiroVermelho
import com.giropro.data.local.OfertaHistoricoEntity
import com.giropro.feature.GiroVmFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel(private val container: GiroProApp.Container) : ViewModel() {
    val ofertas = container.historico.todos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun remover(id: Long) { viewModelScope.launch { container.historico.remover(id) } }
}

@Composable
fun HistoryScreen() {
    val vm: HistoryViewModel = viewModel(factory = GiroVmFactory)
    val lista by vm.ofertas.collectAsState()
    val fmt = remember { SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Histórico", style = MaterialTheme.typography.titleLarge, color = GiroAmarelo, fontWeight = FontWeight.Bold)
        if (lista.isEmpty()) {
            Text(
                "Nenhuma oferta registrada ainda. Analise uma corrida e toque em Salvar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                items(lista, key = { it.id }) { o ->
                    ItemHistorico(o, fmt.format(Date(o.timestamp))) { vm.remover(o.id) }
                }
            }
        }
    }
}

@Composable
private fun ItemHistorico(o: OfertaHistoricoEntity, data: String, onRemover: () -> Unit) {
    val cor = when (runCatching { Classificacao.valueOf(o.classificacao) }.getOrDefault(Classificacao.AMARELO)) {
        Classificacao.VERDE -> GiroVerde
        Classificacao.AMARELO -> GiroAmarelo
        Classificacao.VERMELHO -> GiroVermelho
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("R$ %.2f".format(o.valor), color = cor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(o.plataforma, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "%.1f km · %d min · lucro R$ %.2f".format(o.distanciaTotalKm, o.tempoMin.toInt(), o.lucroEstimado),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(data, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = onRemover) { Text("Excluir", color = GiroVermelho) }
            }
        }
    }
}
