package com.example.hisabkitaab.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hisabkitaab.data.entity.Customer
import com.example.hisabkitaab.data.repository.CustomerRepository
import kotlinx.coroutines.launch

class CustomerViewModel(private val repository: CustomerRepository) : ViewModel() {

    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.insertCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    suspend fun getAllCustomers(): List<Customer> {
        return repository.getAllCustomers()
    }
}