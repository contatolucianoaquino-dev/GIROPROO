package com.giropro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.giropro.core.engine.TipoCombustivel

class Converters {
    @TypeConverter
    fun fromCombustivel(v: TipoCombustivel): String = v.name

    @TypeConverter
    fun toCombustivel(v: String): TipoCombustivel = TipoCombustivel.valueOf(v)
}

@Database(
    entities = [VeiculoEntity::class, OfertaHistoricoEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class GiroDatabase : RoomDatabase() {
    abstract fun veiculoDao(): VeiculoDao
    abstract fun historicoDao(): HistoricoDao

    companion object {
        @Volatile private var instancia: GiroDatabase? = null

        fun obter(context: Context): GiroDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    GiroDatabase::class.java,
                    "giropro.db",
                ).build().also { instancia = it }
            }
    }
}
