package com.giropro

import android.app.Application
import com.giropro.data.datastore.ConfigStore
import com.giropro.data.local.GiroDatabase
import com.giropro.data.repo.HistoricoRepository
import com.giropro.data.repo.VeiculoRepository

/**
 * Injeção de dependências simples e manual (service locator), suficiente para o MVP.
 * Pode migrar para Hilt mais adiante sem mudar as camadas de domínio e dados.
 */
class GiroProApp : Application() {
    lateinit var container: Container
        private set

    override fun onCreate() {
        super.onCreate()
        container = Container(this)
    }

    class Container(app: Application) {
        private val db = GiroDatabase.obter(app)
        val config = ConfigStore(app)
        val veiculos = VeiculoRepository(db.veiculoDao())
        val historico = HistoricoRepository(db.historicoDao())
    }
}
