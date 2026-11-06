package com.giropro.feature.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.giropro.GiroProApp
import com.giropro.core.engine.TipoCombustivel
import com.giropro.core.ui.theme.GiroAmarelo
import com.giropro.core.ui.theme.GiroVerde
import com.giropro.data.local.VeiculoEntity
import com.giropro.feature.GiroVmFactory
import com.giropro.feature.calculator.toBrDouble
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VehicleViewModel(private val container: GiroProApp.Container) : ViewModel() {
    val veiculos = container.veiculos.todos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun salvar(nome: String, combustivel: TipoCombustivel, consumo: Double, manut: Double, pneus: Double) {
        if (nome.isBlank() || consumo <= 0) return
        viewModelScope.launch {
            val id = container.veiculos.salvar(
                VeiculoEntity(
                    nome = nome, combustivel = combustivel,
                    consumoKmPorLitro = consumo, manutencaoPorKm = manut, pneusPorKm = pneus,
                ),
            )
            container.veiculos.definirAtivo(id)
        }
    }

    fun ativar(v: VeiculoEntity) { viewModelScope.launch { container.veiculos.definirAtivo(v.id) } }
    fun remover(v: VeiculoEntity) { viewModelScope.launch { container.veiculos.remover(v) } }
}

@Composable
fun VehicleScreen() {
    val vm: VehicleViewModel = viewModel(factory = GiroVmFactory)
    val lista by vm.veiculos.collectAsState()

    var nome by remember { mutableStateOf("") }
    var consumo by remember { mutableStateOf("") }
    var manut by remember { mutableStateOf("0,12") }
    var pneus by remember { mutableStateOf("0,04") }
    var combustivel by remember { mutableStateOf(TipoCombustivel.GASOLINA) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Configurar veículo", style = MaterialTheme.typography.titleLarge, color = GiroAmarelo, fontWeight = FontWeight.Bold)

        OutlinedTextField(nome, { nome = it }, label = { Text("Nome do veículo") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoCombustivel.entries.forEach { t ->
                Button(
                    onClick = { combustivel = t },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (combustivel == t) GiroAmarelo else MaterialTheme.colorScheme.surface,
                    ),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        t.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (combustivel == t) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Num("Consumo (km/L)", consumo) { consumo = it }
        Num("Manutenção (R$/km)", manut) { manut = it }
        Num("Pneus (R$/km)", pneus) { pneus = it }

        Button(
            onClick = {
                vm.salvar(nome, combustivel, consumo.toBrDouble(), manut.toBrDouble(), pneus.toBrDouble())
                nome = ""; consumo = ""
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GiroAmarelo),
        ) { Text("SALVAR VEÍCULO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }

        Text("Meus veículos", color = MaterialTheme.colorScheme.onSurfaceVariant)
        lista.forEach { v ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        v.nome + if (v.ativo) "  •  ATIVO" else "",
                        color = if (v.ativo) GiroVerde else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${v.combustivel.name.lowercase()} · ${v.consumoKmPorLitro} km/L · manut ${v.manutencaoPorKm} · pneus ${v.pneusPorKm}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!v.ativo) OutlinedButton(onClick = { vm.ativar(v) }) { Text("Usar este") }
                        OutlinedButton(onClick = { vm.remover(v) }) { Text("Remover") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Num(rotulo: String, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(rotulo) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}
