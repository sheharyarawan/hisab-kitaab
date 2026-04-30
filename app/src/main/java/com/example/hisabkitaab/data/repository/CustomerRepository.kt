package com.example.hisabkitaab.data.repository

import com.example.hisabkitaab.data.entity.Customer
import com.example.hisabkitaab.data.room.CustomerDao

class CustomerRepository(
    private val customerDao: CustomerDao
) {

    suspend fun insertCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun getAllCustomers(): List<Customer> {
        return customerDao.getAllCustomers()
    }

    suspend fun getCustomerById(id: Int): Customer {
        return customerDao.getCustomerById(id)
    }
}