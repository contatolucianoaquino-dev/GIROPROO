package com.giropro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VeiculoDao {
    @Query("SELECT * FROM veiculos ORDER BY nome")
    fun observarTodos(): Flow<List<VeiculoEntity>>

    @Query("SELECT * FROM veiculos WHERE ativo = 1 LIMIT 1")
    fun observarAtivo(): Flow<VeiculoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(v: VeiculoEntity): Long

    @Update
    suspend fun atualizar(v: VeiculoEntity)

    @Delete
    suspend fun remover(v: VeiculoEntity)

    @Query("UPDATE veiculos SET ativo = 0")
    suspend fun desativarTodos()

    @Query("UPDATE veiculos SET ativo = 1 WHERE id = :id")
    suspend fun ativar(id: Long)
}

@Dao
interface HistoricoDao {
    @Query("SELECT * FROM historico ORDER BY timestamp DESC")
    fun observarTodos(): Flow<List<OfertaHistoricoEntity>>

    @Query("SELECT * FROM historico WHERE timestamp >= :desde ORDER BY timestamp DESC")
    fun observarDesde(desde: Long): Flow<List<OfertaHistoricoEntity>>

    @Insert
    suspend fun inserir(o: OfertaHistoricoEntity): Long

    @Query("DELETE FROM historico WHERE id = :id")
    suspend fun remover(id: Long)
}
