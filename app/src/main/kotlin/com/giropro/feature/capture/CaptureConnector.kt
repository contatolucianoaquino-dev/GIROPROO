package com.giropro.feature.capture

/**
 * Contrato de captura de ofertas. Cada plataforma tem seu conector.
 *
 * ESTADO HONESTO (leia antes de usar):
 * A captura automática das ofertas exibidas por Uber, 99 e inDrive NÃO está implementada.
 * Ela depende de mecanismos nativos (serviço de acessibilidade e/ou MediaProjection + OCR)
 * que só funcionam em aparelho real e cuja leitura da tela de apps de terceiros pode
 * violar os Termos de Uso dessas plataformas e as políticas da Google Play.
 *
 * Por isso os conectores abaixo retornam [ResultadoCaptura.NaoDisponivel] e nunca inventam
 * valores. A interface e a arquitetura ficam prontas; a ativação real exige decisão de
 * produto e verificação de conformidade. Enquanto isso, a análise funciona por
 * CONFERÊNCIA/ENTRADA MANUAL (tela Analisar), que é o caminho permitido e seguro.
 */

/** Dados que uma oferta capturada pode conter. Campos ausentes ficam nulos — nunca inventados. */
data class OfertaCapturada(
    val plataforma: String,
    val valor: Double? = null,
    val distanciaAtePassageiroKm: Double? = null,
    val distanciaDestinoKm: Double? = null,
    val distanciaTotalKm: Double? = null,
    val tempoEstimadoMin: Double? = null,
    val embarque: String? = null,
    val destino: String? = null,
) {
    /** true quando há o mínimo para calcular rentabilidade sem chute. */
    val suficienteParaAnalise: Boolean
        get() = valor != null && (distanciaTotalKm != null || distanciaDestinoKm != null) &&
            tempoEstimadoMin != null
}

sealed interface ResultadoCaptura {
    data class Capturada(val oferta: OfertaCapturada) : ResultadoCaptura
    /** Captura ainda não implementada/autorizada para esta plataforma. */
    data object NaoDisponivel : ResultadoCaptura
}

interface CaptureConnector {
    val plataforma: String
    fun capturar(): ResultadoCaptura
}

class UberConnector : CaptureConnector {
    override val plataforma = "Uber"
    // TODO: captura via mecanismo autorizado + OCR, testada em aparelho real.
    override fun capturar(): ResultadoCaptura = ResultadoCaptura.NaoDisponivel
}

class NoveNoveConnector : CaptureConnector {
    override val plataforma = "99"
    override fun capturar(): ResultadoCaptura = ResultadoCaptura.NaoDisponivel
}

class InDriveConnector : CaptureConnector {
    override val plataforma = "inDrive"
    override fun capturar(): ResultadoCaptura = ResultadoCaptura.NaoDisponivel
}

object Conectores {
    val todos: List<CaptureConnector> = listOf(UberConnector(), NoveNoveConnector(), InDriveConnector())
}
