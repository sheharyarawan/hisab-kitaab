package com.example.hisabkitaab.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hisabkitaab.data.entity.Customer
import com.example.hisabkitaab.data.entity.Transaction
import com.example.hisabkitaab.data.entity.UserProfile

@Database(
    entities = [Customer::class, Transaction::class,UserProfile::class],
    version = 2
)
abstract class KitaabDatabase : RoomDatabase() {

    abstract fun getCustomerDao(): CustomerDao
    abstract fun getTransactionDao(): TransactionDao
    abstract fun profileDao(): ProfileDao

    companion object {

        @Volatile
        private var instance: KitaabDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context): KitaabDatabase {
            return instance ?: synchronized(LOCK) {
                instance ?: createDatabase(context).also {
                    instance = it
                }
            }
        }

        private fun createDatabase(context: Context): KitaabDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                KitaabDatabase::class.java,
                "kitaabDB"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}