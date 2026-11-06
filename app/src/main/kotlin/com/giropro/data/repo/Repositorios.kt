package com.giropro.data.repo

import com.giropro.core.engine.Veiculo
import com.giropro.data.local.HistoricoDao
import com.giropro.data.local.OfertaHistoricoEntity
import com.giropro.data.local.VeiculoDao
import com.giropro.data.local.VeiculoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Converte a entidade Room para o modelo puro do motor de cálculo. */
fun VeiculoEntity.toEngine(): Veiculo = Veiculo(
    id = id,
    nome = nome,
    combustivel = combustivel,
    consumoKmPorLitro = consumoKmPorLitro,
    manutencaoPorKm = manutencaoPorKm,
    pneusPorKm = pneusPorKm,
)

class VeiculoRepository(private val dao: VeiculoDao) {
    val todos: Flow<List<VeiculoEntity>> = dao.observarTodos()
    val ativo: Flow<VeiculoEntity?> = dao.observarAtivo()

    suspend fun salvar(v: VeiculoEntity): Long = dao.inserir(v)
    suspend fun atualizar(v: VeiculoEntity) = dao.atualizar(v)
    suspend fun remover(v: VeiculoEntity) = dao.remover(v)

    suspend fun definirAtivo(id: Long) {
        dao.desativarTodos()
        dao.ativar(id)
    }
}

class HistoricoRepository(private val dao: HistoricoDao) {
    val todos: Flow<List<OfertaHistoricoEntity>> = dao.observarTodos()
    fun desde(ts: Long): Flow<List<OfertaHistoricoEntity>> = dao.observarDesde(ts)

    suspend fun registrar(o: OfertaHistoricoEntity): Long = dao.inserir(o)
    suspend fun remover(id: Long) = dao.remover(id)
}
