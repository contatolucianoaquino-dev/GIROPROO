package com.giropro.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.giropro.core.engine.CustosOperacionais
import com.giropro.core.engine.Metas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "config")

/**
 * Guarda custos e metas no DataStore. Valores iniciais são padrões editáveis,
 * não dados reais de nenhum motorista — o usuário ajusta na tela de configuração.
 */
class ConfigStore(private val context: Context) {

    private object Keys {
        val gasolina = doublePreferencesKey("preco_gasolina")
        val etanol = doublePreferencesKey("preco_etanol")
        val diesel = doublePreferencesKey("preco_diesel")
        val seguro = doublePreferencesKey("seguro_mensal")
        val ipva = doublePreferencesKey("ipva_anual")
        val depreciacao = doublePreferencesKey("depreciacao_mensal")
        val outros = doublePreferencesKey("outros_mensal")
        val kmMes = doublePreferencesKey("km_por_mes")
        val minKm = doublePreferencesKey("meta_min_km")
        val minHora = doublePreferencesKey("meta_min_hora")
        val bomKm = doublePreferencesKey("meta_bom_km")
        val bomHora = doublePreferencesKey("meta_bom_hora")
        val metaMes = doublePreferencesKey("meta_lucro_mes")
    }

    val custos: Flow<CustosOperacionais> = context.dataStore.data.map { p ->
        CustosOperacionais(
            precoGasolina = p[Keys.gasolina] ?: 6.0,
            precoEtanol = p[Keys.etanol] ?: 4.0,
            precoDiesel = p[Keys.diesel] ?: 6.5,
            seguroMensal = p[Keys.seguro] ?: 150.0,
            ipvaAnual = p[Keys.ipva] ?: 1200.0,
            depreciacaoMensal = p[Keys.depreciacao] ?: 300.0,
            outrosMensal = p[Keys.outros] ?: 100.0,
            kmPorMes = p[Keys.kmMes] ?: 4000.0,
        )
    }

    val metas: Flow<Metas> = context.dataStore.data.map { p ->
        Metas(
            minimoPorKm = p[Keys.minKm] ?: 1.30,
            minimoPorHora = p[Keys.minHora] ?: 22.0,
            bomPorKm = p[Keys.bomKm] ?: 2.0,
            bomPorHora = p[Keys.bomHora] ?: 35.0,
        )
    }

    val metaLucroMes: Flow<Double> = context.dataStore.data.map { it[Keys.metaMes] ?: 4500.0 }

    suspend fun salvarCustos(c: CustosOperacionais) {
        context.dataStore.edit { p ->
            p[Keys.gasolina] = c.precoGasolina
            p[Keys.etanol] = c.precoEtanol
            p[Keys.diesel] = c.precoDiesel
            p[Keys.seguro] = c.seguroMensal
            p[Keys.ipva] = c.ipvaAnual
            p[Keys.depreciacao] = c.depreciacaoMensal
            p[Keys.outros] = c.outrosMensal
            p[Keys.kmMes] = c.kmPorMes
        }
    }

    suspend fun salvarMetas(m: Metas, metaMes: Double) {
        context.dataStore.edit { p ->
            p[Keys.minKm] = m.minimoPorKm
            p[Keys.minHora] = m.minimoPorHora
            p[Keys.bomKm] = m.bomPorKm
            p[Keys.bomHora] = m.bomPorHora
            p[Keys.metaMes] = metaMes
        }
    }
}
