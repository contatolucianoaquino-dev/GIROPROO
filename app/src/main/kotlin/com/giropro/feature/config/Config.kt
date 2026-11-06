package com.giropro.feature.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.giropro.GiroProApp
import com.giropro.core.engine.CustosOperacionais
import com.giropro.core.engine.Metas
import com.giropro.core.ui.theme.GiroAmarelo
import com.giropro.feature.GiroVmFactory
import com.giropro.feature.calculator.toBrDouble
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ConfigViewModel(private val container: GiroProApp.Container) : ViewModel() {
    suspend fun carregar(): Triple<CustosOperacionais, Metas, Double> =
        Triple(container.config.custos.first(), container.config.metas.first(), container.config.metaLucroMes.first())

    fun salvar(c: CustosOperacionais, m: Metas, metaMes: Double) {
        viewModelScope.launch {
            container.config.salvarCustos(c)
            container.config.salvarMetas(m, metaMes)
        }
    }
}

@Composable
fun ConfigScreen() {
    val vm: ConfigViewModel = viewModel(factory = GiroVmFactory)

    var gasolina by remember { mutableStateOf("") }
    var etanol by remember { mutableStateOf("") }
    var diesel by remember { mutableStateOf("") }
    var seguro by remember { mutableStateOf("") }
    var ipva by remember { mutableStateOf("") }
    var depreciacao by remember { mutableStateOf("") }
    var outros by remember { mutableStateOf("") }
    var kmMes by remember { mutableStateOf("") }
    var minKm by remember { mutableStateOf("") }
    var minHora by remember { mutableStateOf("") }
    var bomKm by remember { mutableStateOf("") }
    var bomHora by remember { mutableStateOf("") }
    var metaMes by remember { mutableStateOf("") }
    var salvo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val (c, m, meta) = vm.carregar()
        gasolina = fmt(c.precoGasolina); etanol = fmt(c.precoEtanol); diesel = fmt(c.precoDiesel)
        seguro = fmt(c.seguroMensal); ipva = fmt(c.ipvaAnual); depreciacao = fmt(c.depreciacaoMensal)
        outros = fmt(c.outrosMensal); kmMes = fmt(c.kmPorMes)
        minKm = fmt(m.minimoPorKm); minHora = fmt(m.minimoPorHora); bomKm = fmt(m.bomPorKm); bomHora = fmt(m.bomPorHora)
        metaMes = fmt(meta)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Secao("Combustível")
        C("Gasolina (R$/L)", gasolina) { gasolina = it }
        C("Etanol (R$/L)", etanol) { etanol = it }
        C("Diesel (R$/L)", diesel) { diesel = it }

        Secao("Custos fixos")
        C("Seguro (R$/mês)", seguro) { seguro = it }
        C("IPVA (R$/ano)", ipva) { ipva = it }
        C("Depreciação (R$/mês)", depreciacao) { depreciacao = it }
        C("Outros (R$/mês)", outros) { outros = it }
        C("Km rodados por mês", kmMes) { kmMes = it }

        Secao("Semáforo e metas")
        C("Verde a partir de (R$/km)", bomKm) { bomKm = it }
        C("Verde a partir de (R$/hora)", bomHora) { bomHora = it }
        C("Vermelho abaixo de (R$/km)", minKm) { minKm = it }
        C("Vermelho abaixo de (R$/hora)", minHora) { minHora = it }
        C("Meta de lucro no mês (R$)", metaMes) { metaMes = it }

        Button(
            onClick = {
                vm.salvar(
                    CustosOperacionais(
                        precoGasolina = gasolina.toBrDouble(), precoEtanol = etanol.toBrDouble(), precoDiesel = diesel.toBrDouble(),
                        seguroMensal = seguro.toBrDouble(), ipvaAnual = ipva.toBrDouble(),
                        depreciacaoMensal = depreciacao.toBrDouble(), outrosMensal = outros.toBrDouble(),
                        kmPorMes = kmMes.toBrDouble(),
                    ),
                    Metas(minKm.toBrDouble(), minHora.toBrDouble(), bomKm.toBrDouble(), bomHora.toBrDouble()),
                    metaMes.toBrDouble(),
                )
                salvo = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GiroAmarelo),
        ) { Text("SALVAR", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }

        if (salvo) Text("Salvo neste aparelho.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Secao(t: String) =
    Text(t, color = GiroAmarelo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

@Composable
private fun C(rotulo: String, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor, onValueChange = onChange, label = { Text(rotulo) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun fmt(v: Double): String =
    if (v == v.toLong().toDouble()) v.toLong().toString() else "%.2f".format(v).replace(".", ",")
