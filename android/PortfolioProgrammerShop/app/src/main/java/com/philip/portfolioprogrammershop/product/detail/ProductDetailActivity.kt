package com.philip.portfolioprogrammershop.product.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View.VISIBLE
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.philip.portfolioprogrammershop.R
import com.philip.portfolioprogrammershop.databinding.ActivityProductDetailBinding
import com.philip.portfolioprogrammershop.product.update.ProductUpdateActivity
import com.philip.portfolioprogrammershop.util.EventObserver
import kotlinx.android.synthetic.main.activity_product_detail.*
import kotlinx.android.synthetic.main.activity_product_registration.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.item_image_viewpager.*
import kotlinx.android.synthetic.main.item_product_list.view.*


class ProductDetailActivity : AppCompatActivity() {

    private var getProductId: Long = 0

    private val viewModel: ProductDetailViewModel by viewModels()

    // 수정, 삭제 기능을 위해 상품정보를 저장해두는 변수
    private var productId: Long = 0
    private var productName: String? = null
    private var productDescription: String? = null
    private var productPrice: Int = 0
    private var productPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binding = DataBindingUtil.setContentView<ActivityProductDetailBinding>(
            this,
            R.layout.activity_product_detail
        )
        binding.lifecycleOwner = this
        binding.xmlViewModel = viewModel

        setSupportActionBar(toolbar_productDetail) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_back_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(true) // 툴바에 타이틀 보이게
        supportActionBar?.title = ""

        getProductId = intent.getLongExtra(PRODUCT_ID, -1)
        viewModel.loadDetail(getProductId)

        viewModel.productImage.observe(this, Observer { imagePath ->
            Glide.with(this)
                    .load(imagePath)
                    .centerCrop()
                    .into(img_productDetail)
        })

        // "내가 올린 상품"인 경우 -> 수정,삭제 버튼 보일 것
        viewModel.checkMe.observe(this, EventObserver {
            endLine_layout.visibility = VISIBLE
        })
        // "상품 삭제 성공"인 경우 -> 상품 상세 화면 닫기(메인 화면이 보임)
        viewModel.deleteProductToast.observe(this, EventObserver {
            //
            finish()
        })

        // 수정, 삭제 기능을 위해 상품정보를 LiveData 에 저장해둔다.
        viewModel.productInfo.observe(this, Observer { productInfo ->
            productId = productInfo.product_id
            productName = productInfo.product_name
            productDescription = productInfo.product_description
            productPrice = productInfo.product_price
            productPath = productInfo.product_path
        })

        // 상품 수정 버튼
        btn_edit_productDetail.setOnClickListener {
            val sendIntent = Intent(this@ProductDetailActivity, ProductUpdateActivity::class.java).apply {
                putExtra(ProductUpdateActivity.PRODUCT_ID, productId)
                putExtra(ProductUpdateActivity.PRODUCT_NAME, productName)
                putExtra(ProductUpdateActivity.PRODUCT_DESCRIPTION, productDescription)
                putExtra(ProductUpdateActivity.PRODUCT_PRICE, productPrice)
                putExtra(ProductUpdateActivity.PRODUCT_PATH, productPath)
            }
            startActivity(sendIntent)
            finish()
        }

        // 상품 삭제 버튼
        btn_delete_productDetail.setOnClickListener {
            showDeleteProductPopup()
        }

    }

    private fun showDeleteProductPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "상품을 삭제하시겠습니까?"

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("상품 삭제")
            .setPositiveButton("확인") { dialog, which ->
                viewModel.deleteItem(getProductId)
            }
            .create()

        alertDialog.setView(view)
        alertDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item?.let {
            when(item.itemId) {
                android.R.id.home -> onBackPressed()
                else -> {}
            }
        }
        return true
    }

    companion object {
        val PRODUCT_ID = "productId"
    }

}