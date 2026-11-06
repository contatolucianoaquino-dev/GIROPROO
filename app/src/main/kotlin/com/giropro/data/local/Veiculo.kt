package com.giropro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.giropro.core.engine.TipoCombustivel

@Entity(tableName = "veiculos")
data class VeiculoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val combustivel: TipoCombustivel,
    val consumoKmPorLitro: Double,
    val manutencaoPorKm: Double,
    val pneusPorKm: Double,
    val ativo: Boolean = false,
)

@Entity(tableName = "historico")
data class OfertaHistoricoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val plataforma: String,
    val valor: Double,
    val distanciaTotalKm: Double,
    val tempoMin: Double,
    val custoEstimado: Double,
    val lucroEstimado: Double,
    val classificacao: String,
    /** true quando a corrida foi efetivamente realizada, não só analisada. */
    val realizada: Boolean = false,
)
