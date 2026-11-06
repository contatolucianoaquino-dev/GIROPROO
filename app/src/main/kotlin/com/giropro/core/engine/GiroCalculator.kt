package com.giropro.core.engine

/**
 * Motor de cálculo do GiroPro — Kotlin puro, sem dependência de Android.
 *
 * Toda a regra de rentabilidade vive aqui, isolada da interface, para poder ser
 * testada por completo em JVM. A UI e os conectores apenas consomem [analisar].
 */

enum class TipoCombustivel { GASOLINA, ETANOL, DIESEL }

enum class Classificacao { VERDE, AMARELO, VERMELHO }

/** Veículo cadastrado pelo motorista. Custos de desgaste são por km. */
data class Veiculo(
    val id: Long = 0,
    val nome: String,
    val combustivel: TipoCombustivel,
    val consumoKmPorLitro: Double,
    val manutencaoPorKm: Double,
    val pneusPorKm: Double,
)

/** Preços de combustível e custos fixos periódicos. */
data class CustosOperacionais(
    val precoGasolina: Double,
    val precoEtanol: Double,
    val precoDiesel: Double,
    val seguroMensal: Double,
    val ipvaAnual: Double,
    val depreciacaoMensal: Double,
    val outrosMensal: Double,
    /** km rodados por mês, usado para diluir os custos fixos em R$/km. */
    val kmPorMes: Double,
)

/** Metas do semáforo, definidas pelo motorista. */
data class Metas(
    val minimoPorKm: Double,
    val minimoPorHora: Double,
    val bomPorKm: Double,
    val bomPorHora: Double,
)

/** Uma oferta de corrida a avaliar. Distâncias em km, tempo em minutos. */
data class Oferta(
    val plataforma: String,
    val valor: Double,
    val distanciaAtePassageiroKm: Double,
    val distanciaDestinoKm: Double,
    val tempoEstimadoMin: Double,
) {
    val distanciaTotalKm: Double get() = distanciaAtePassageiroKm + distanciaDestinoKm
}

/** Resultado completo da análise de uma oferta. */
data class Analise(
    val valor: Double,
    val distanciaTotalKm: Double,
    val tempoTotalMin: Double,
    val valorPorKm: Double,
    val valorPorHora: Double,
    val custoCombustivel: Double,
    val custoOperacional: Double,
    val lucroEstimado: Double,
    val lucroPorKm: Double,
    val lucroPorHora: Double,
    val classificacao: Classificacao,
)

object GiroCalculator {

    fun precoCombustivel(custos: CustosOperacionais, tipo: TipoCombustivel): Double = when (tipo) {
        TipoCombustivel.GASOLINA -> custos.precoGasolina
        TipoCombustivel.ETANOL -> custos.precoEtanol
        TipoCombustivel.DIESEL -> custos.precoDiesel
    }

    /** Custo de combustível da corrida inteira (inclui deslocamento até o passageiro). */
    fun custoCombustivel(oferta: Oferta, veiculo: Veiculo, custos: CustosOperacionais): Double {
        if (veiculo.consumoKmPorLitro <= 0.0) return 0.0
        return oferta.distanciaTotalKm / veiculo.consumoKmPorLitro *
            precoCombustivel(custos, veiculo.combustivel)
    }

    /** Custos fixos (seguro, IPVA, depreciação, outros) convertidos para R$/km. */
    fun custoFixoPorKm(custos: CustosOperacionais): Double {
        if (custos.kmPorMes <= 0.0) return 0.0
        val fixoMensal = custos.seguroMensal + custos.depreciacaoMensal +
            custos.outrosMensal + custos.ipvaAnual / 12.0
        return fixoMensal / custos.kmPorMes
    }

    /**
     * Custo operacional total da corrida.
     * Combustível entra uma única vez (calculado pelo consumo); manutenção, pneus e
     * custos fixos entram por km. Não há dupla contagem de nenhum custo.
     */
    fun custoOperacional(oferta: Oferta, veiculo: Veiculo, custos: CustosOperacionais): Double {
        val combustivel = custoCombustivel(oferta, veiculo, custos)
        val desgastePorKm = veiculo.manutencaoPorKm + veiculo.pneusPorKm + custoFixoPorKm(custos)
        return combustivel + desgastePorKm * oferta.distanciaTotalKm
    }

    fun classificar(
        lucro: Double,
        lucroPorKm: Double,
        lucroPorHora: Double,
        metas: Metas,
    ): Classificacao {
        if (lucro < 0.0) return Classificacao.VERMELHO
        if (lucroPorKm < metas.minimoPorKm || lucroPorHora < metas.minimoPorHora) {
            return Classificacao.VERMELHO
        }
        if (lucroPorKm >= metas.bomPorKm && lucroPorHora >= metas.bomPorHora) {
            return Classificacao.VERDE
        }
        return Classificacao.AMARELO
    }

    fun analisar(
        oferta: Oferta,
        veiculo: Veiculo,
        custos: CustosOperacionais,
        metas: Metas,
    ): Analise {
        val dist = oferta.distanciaTotalKm
        val horas = oferta.tempoEstimadoMin / 60.0
        val valorPorKm = if (dist > 0.0) oferta.valor / dist else 0.0
        val valorPorHora = if (horas > 0.0) oferta.valor / horas else 0.0

        val combustivel = custoCombustivel(oferta, veiculo, custos)
        val operacional = custoOperacional(oferta, veiculo, custos)
        val lucro = oferta.valor - operacional
        val lucroPorKm = if (dist > 0.0) lucro / dist else 0.0
        val lucroPorHora = if (horas > 0.0) lucro / horas else 0.0

        return Analise(
            valor = oferta.valor,
            distanciaTotalKm = dist,
            tempoTotalMin = oferta.tempoEstimadoMin,
            valorPorKm = valorPorKm,
            valorPorHora = valorPorHora,
            custoCombustivel = combustivel,
            custoOperacional = operacional,
            lucroEstimado = lucro,
            lucroPorKm = lucroPorKm,
            lucroPorHora = lucroPorHora,
            classificacao = classificar(lucro, lucroPorKm, lucroPorHora, metas),
        )
    }
}
