package com.giropro.feature.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.giropro.core.engine.Analise
import com.giropro.core.engine.Classificacao

/**
 * Serviço de janela flutuante (overlay) sobre os apps de transporte.
 *
 * Mostra o resultado de uma análise numa janela arrastável, por cima de qualquer app,
 * usando SYSTEM_ALERT_WINDOW. Exige que o usuário conceda "Exibir sobre outros apps".
 *
 * Observação honesta: este serviço é código real e funciona em aparelho, mas só foi
 * escrito aqui — precisa ser testado em um dispositivo Android (não há emulador nem SDK
 * no ambiente em que o projeto foi gerado). A alimentação com ofertas reais depende do
 * módulo de captura (feature/capture), ainda pendente de autorização das plataformas.
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var params: WindowManager.LayoutParams

    private lateinit var tTitulo: TextView
    private lateinit var tValor: TextView
    private lateinit var tDetalhe: TextView
    private lateinit var tClassificacao: TextView
    private lateinit var container: LinearLayout

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        iniciarForeground()
        criarJanela()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Quando houver análise anexada, atualiza a janela.
        intent?.let { atualizarDe(it) }
        return START_STICKY
    }

    private fun iniciarForeground() {
        val canalId = "giropro_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Monitoramento GiroPro",
                NotificationManager.IMPORTANCE_LOW,
            )
            (getSystemService(NotificationManager::class.java)).createNotificationChannel(canal)
        }
        val n: Notification = Notification.Builder(this, canalId)
            .setContentTitle("GiroPro monitorando")
            .setContentText("Analisando ofertas de corrida")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(1, n)
    }

    private fun criarJanela() {
        val tipo = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            tipo,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 160
        }

        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#101010"))
            setPadding(36, 28, 36, 28)
        }
        tTitulo = TextView(this).apply {
            text = "GIROPRO"
            setTextColor(Color.parseColor("#F4C847"))
            textSize = 13f
        }
        tValor = TextView(this).apply {
            text = "OFERTA: --"
            setTextColor(Color.WHITE)
            textSize = 22f
        }
        tDetalhe = TextView(this).apply {
            text = "Aguardando oferta"
            setTextColor(Color.parseColor("#B4B4B4"))
            textSize = 13f
        }
        tClassificacao = TextView(this).apply {
            text = "--"
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(0, 16, 0, 0)
        }
        container.addView(tTitulo)
        container.addView(tValor)
        container.addView(tDetalhe)
        container.addView(tClassificacao)

        tornarArrastavel(container)

        floatingView = container
        windowManager.addView(floatingView, params)
    }

    private fun tornarArrastavel(view: View) {
        var inicialX = 0
        var inicialY = 0
        var toqueX = 0f
        var toqueY = 0f
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    inicialX = params.x
                    inicialY = params.y
                    toqueX = event.rawX
                    toqueY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = inicialX + (event.rawX - toqueX).toInt()
                    params.y = inicialY + (event.rawY - toqueY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun atualizarDe(intent: Intent) {
        if (!intent.getBooleanExtra(EXTRA_TEM_ANALISE, false)) return
        val valor = intent.getDoubleExtra(EXTRA_VALOR, 0.0)
        val dist = intent.getDoubleExtra(EXTRA_DIST, 0.0)
        val tempo = intent.getDoubleExtra(EXTRA_TEMPO, 0.0)
        val vkm = intent.getDoubleExtra(EXTRA_VKM, 0.0)
        val vhora = intent.getDoubleExtra(EXTRA_VHORA, 0.0)
        val custo = intent.getDoubleExtra(EXTRA_CUSTO, 0.0)
        val lucro = intent.getDoubleExtra(EXTRA_LUCRO, 0.0)
        val classe = intent.getStringExtra(EXTRA_CLASSE) ?: Classificacao.AMARELO.name

        tValor.text = "OFERTA: " + brl(valor)
        tDetalhe.text = "${fmt(dist)} km · ${tempo.toInt()} min\n" +
            "R$/km ${brl(vkm)} · R$/h ${brl(vhora)}\n" +
            "Custo ${brl(custo)} · Lucro ${brl(lucro)}"
        val (texto, cor) = when (Classificacao.valueOf(classe)) {
            Classificacao.VERDE -> "BOA CORRIDA" to "#21B66F"
            Classificacao.AMARELO -> "RENTABILIDADE MÉDIA" to "#F4C847"
            Classificacao.VERMELHO -> "ABAIXO DA META" to "#E54848"
        }
        tClassificacao.text = texto
        tClassificacao.setTextColor(Color.parseColor(cor))
        container.setBackgroundColor(Color.parseColor("#101010"))
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager.removeView(it) }
        floatingView = null
    }

    companion object {
        const val EXTRA_TEM_ANALISE = "tem_analise"
        const val EXTRA_VALOR = "valor"
        const val EXTRA_DIST = "dist"
        const val EXTRA_TEMPO = "tempo"
        const val EXTRA_VKM = "vkm"
        const val EXTRA_VHORA = "vhora"
        const val EXTRA_CUSTO = "custo"
        const val EXTRA_LUCRO = "lucro"
        const val EXTRA_CLASSE = "classe"

        fun temPermissao(context: Context): Boolean = Settings.canDrawOverlays(context)

        fun intentComAnalise(context: Context, a: Analise, plataforma: String): Intent =
            Intent(context, OverlayService::class.java).apply {
                putExtra(EXTRA_TEM_ANALISE, true)
                putExtra(EXTRA_VALOR, a.valor)
                putExtra(EXTRA_DIST, a.distanciaTotalKm)
                putExtra(EXTRA_TEMPO, a.tempoTotalMin)
                putExtra(EXTRA_VKM, a.valorPorKm)
                putExtra(EXTRA_VHORA, a.valorPorHora)
                putExtra(EXTRA_CUSTO, a.custoOperacional)
                putExtra(EXTRA_LUCRO, a.lucroEstimado)
                putExtra(EXTRA_CLASSE, a.classificacao.name)
            }

        private fun brl(v: Double): String = "R$ %.2f".format(v)
        private fun fmt(v: Double): String = "%.1f".format(v)
    }
}
