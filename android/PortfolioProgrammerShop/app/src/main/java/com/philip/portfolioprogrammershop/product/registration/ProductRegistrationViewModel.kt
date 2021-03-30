package com.philip.portfolioprogrammershop.product.registration

import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.philip.portfolioprogrammershop.product.category.categoryList

class ProductRegistrationViewModel : ViewModel() {

    val productName = MutableLiveData("")
    val description = MutableLiveData("")
    val price = MutableLiveData("")
    val categories = MutableLiveData(categoryList.map { it.name })
    var categoryIdSelected: Int? = categoryList[0].id

    val descriptionLimit = 500
    val productNameLimit = 40
    val productNameLength = MutableLiveData("0/$productNameLimit")
    val descriptionLength = MutableLiveData("0/$descriptionLimit")

    fun onNameTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        checkProductNameLength()
    }
    fun onDescriptionTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        checkDescriptionLength()
    }
    val clickCategoryListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onCategorySelect(position)
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {
            //
        }
    }

    fun checkProductNameLength() {
        productName.value?.let {
            if (it.length > productNameLimit) {
                productName.value = it.take(productNameLimit)
            }
            productNameLength.value =
                    "${productName.value?.length}/$productNameLimit"
        }
    }

    fun checkDescriptionLength() {
        description.value?.let {
            if (it.length > descriptionLimit) {
                description.value = it.take(descriptionLimit)
            }
            descriptionLength.value =
                    "${description.value?.length}/$descriptionLimit"
        }
    }

    fun onCategorySelect(position: Int) {
        categoryIdSelected = categoryList[position].id
    }
}