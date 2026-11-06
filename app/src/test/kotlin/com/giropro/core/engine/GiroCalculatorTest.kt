package com.giropro.core.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GiroCalculatorTest {

    private val veiculo = Veiculo(
        nome = "Onix",
        combustivel = TipoCombustivel.GASOLINA,
        consumoKmPorLitro = 12.0,
        manutencaoPorKm = 0.12,
        pneusPorKm = 0.04,
    )

    private val custos = CustosOperacionais(
        precoGasolina = 6.0,
        precoEtanol = 4.0,
        precoDiesel = 6.5,
        seguroMensal = 150.0,
        ipvaAnual = 1200.0,
        depreciacaoMensal = 300.0,
        outrosMensal = 100.0,
        kmPorMes = 4000.0,
    )

    private val metas = Metas(minimoPorKm = 1.30, minimoPorHora = 22.0, bomPorKm = 2.0, bomPorHora = 35.0)

    private fun perto(esperado: Double, obtido: Double, tol: Double = 0.01) {
        assertTrue(kotlin.math.abs(esperado - obtido) <= tol, "esperado ~$esperado, obtido $obtido")
    }

    @Test fun valorPorKm_eParHora_doExemploDoProjeto() {
        // Oferta do prompt: R$ 32,50 · 18,5 km · 35 min
        val oferta = Oferta("Uber", 32.50, 0.0, 18.5, 35.0)
        val a = GiroCalculator.analisar(oferta, veiculo, custos, metas)
        perto(1.76, a.valorPorKm)       // 32,50 / 18,5
        perto(55.71, a.valorPorHora)    // 32,50 / (35/60)
    }

    @Test fun custoCombustivel_usaConsumoEPreco() {
        val oferta = Oferta("99", 0.0, 0.0, 12.0, 10.0) // 12 km, consumo 12 km/L, R$6/L => 1L => R$6
        perto(6.0, GiroCalculator.custoCombustivel(oferta, veiculo, custos))
    }

    @Test fun custoFixoPorKm_diluiFixosPelosKmDoMes() {
        // (150 + 300 + 100 + 1200/12) / 4000 = 650/4000 = 0,1625
        perto(0.1625, GiroCalculator.custoFixoPorKm(custos))
    }

    @Test fun custoOperacional_naoContaCombustivelDuasVezes() {
        val oferta = Oferta("Uber", 0.0, 0.0, 10.0, 10.0)
        val combustivel = GiroCalculator.custoCombustivel(oferta, veiculo, custos) // 10/12*6 = 5,0
        val fixoKm = GiroCalculator.custoFixoPorKm(custos)
        val esperado = combustivel + (0.12 + 0.04 + fixoKm) * 10.0
        perto(esperado, GiroCalculator.custoOperacional(oferta, veiculo, custos))
        perto(5.0, combustivel)
    }

    @Test fun lucro_eRentabilidade() {
        val oferta = Oferta("Uber", 32.50, 2.0, 18.5, 35.0)
        val a = GiroCalculator.analisar(oferta, veiculo, custos, metas)
        perto(a.valor - a.custoOperacional, a.lucroEstimado)
        perto(a.lucroEstimado / a.distanciaTotalKm, a.lucroPorKm)
        assertTrue(a.lucroEstimado > 0, "corrida longa e bem paga deve dar lucro")
    }

    @Test fun semaforo_prejuizoEhVermelho() {
        val oferta = Oferta("99", 4.0, 3.0, 2.0, 12.0) // paga pouco, roda pra buscar
        val a = GiroCalculator.analisar(oferta, veiculo, custos, metas)
        assertEquals(Classificacao.VERMELHO, a.classificacao)
    }

    @Test fun semaforo_verdeQuandoBateAsDuasMetasBoas() {
        val a = GiroCalculator.classificar(lucro = 40.0, lucroPorKm = 2.5, lucroPorHora = 50.0, metas)
        assertEquals(Classificacao.VERDE, a)
    }

    @Test fun semaforo_amareloEntreMinimoEBom() {
        val a = GiroCalculator.classificar(lucro = 10.0, lucroPorKm = 1.6, lucroPorHora = 28.0, metas)
        assertEquals(Classificacao.AMARELO, a)
    }

    @Test fun semaforo_vermelhoSeUmIndicadorAbaixoDoMinimo() {
        // bom por km, mas por hora abaixo do mínimo => vermelho (não usa indicador isolado)
        val a = GiroCalculator.classificar(lucro = 5.0, lucroPorKm = 2.1, lucroPorHora = 15.0, metas)
        assertEquals(Classificacao.VERMELHO, a)
    }

    @Test fun divisaoPorZero_naoQuebra() {
        val oferta = Oferta("Uber", 10.0, 0.0, 0.0, 0.0)
        val a = GiroCalculator.analisar(oferta, veiculo, custos, metas)
        assertEquals(0.0, a.valorPorKm)
        assertEquals(0.0, a.valorPorHora)
    }
}
