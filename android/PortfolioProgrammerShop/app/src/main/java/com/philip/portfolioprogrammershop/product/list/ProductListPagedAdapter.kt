package com.philip.portfolioprogrammershop.product.list


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.philip.portfolioprogrammershop.R
import com.philip.portfolioprogrammershop.api.ApiGenerator
import com.philip.portfolioprogrammershop.api.response.ProductListItemResponse
import kotlinx.android.synthetic.main.item_product_list.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.NumberFormat


class ProductListPagedAdapter(private val listener: OnItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var products: List<ProductListItemResponse>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProductItemViewHolder(listener, LayoutInflater.from(parent.context).inflate(R.layout.item_product_list, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val listItem = products?.get(position)
        listItem.let {
            (holder as ProductItemViewHolder).bind(it)
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int = products?.size ?: 0

    fun setProducts(products: List<ProductListItemResponse>) {
        this.products = products
        notifyDataSetChanged()
    }

    class ProductItemViewHolder(
            private val listener: OnItemClickListener,
            private val view: View
    ) : RecyclerView.ViewHolder(view) {

        var productId: Long? = null

        init {
            itemView.setOnClickListener {
                GlobalScope.launch(Dispatchers.Main) {
                    listener.onClickProduct(productId)
                }
            }
        }

        fun bind(item: ProductListItemResponse?) = item?.let {
            this.productId = item.id
            val commaSeparatedPrice =
                    NumberFormat.getNumberInstance().format(item.price)

            view.txt_product_list_title.text = item.name.replace("\"", "")  // 쌍따옴표 제거
            view.txt_product_list_price.text = "₩$commaSeparatedPrice"

            Glide.with(view.img_product_list)
                    .load("${ApiGenerator.URL_DEFAULT}/${item.path}")
                    .centerCrop()
                    .into(view.img_product_list)
        }

    }

    interface OnItemClickListener {
        fun onClickProduct(productId: Long?)
    }


}