package com.philip.portfolioprogrammershop.product.update

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProductUpdateViewModel : ViewModel() {

    val productName = MutableLiveData("")
    val productDescription = MutableLiveData("")
    val productPrice = MutableLiveData("")

}