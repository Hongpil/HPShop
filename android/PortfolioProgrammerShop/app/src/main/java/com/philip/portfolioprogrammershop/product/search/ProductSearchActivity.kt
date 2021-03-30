package com.philip.portfolioprogrammershop.product.search



import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.philip.portfolioprogrammershop.R
import com.philip.portfolioprogrammershop.product.detail.ProductDetailActivity
import com.philip.portfolioprogrammershop.product.list.ProductListPagedAdapter
import com.philip.portfolioprogrammershop.product.list.ProductListViewModel
import kotlinx.android.synthetic.main.activity_product_main_body.*
import kotlinx.android.synthetic.main.activity_product_search.*

class ProductSearchActivity : AppCompatActivity(), ProductListPagedAdapter.OnItemClickListener {

    private val viewModel: ProductListViewModel by viewModels()
    var keyword: String? = null

    private val productSearchAdapter by lazy {
        ProductListPagedAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_search)

        keyword = intent.getStringExtra(KEYWORD)

        setSupportActionBar(toolbar_product_search) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_back_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(true) // 툴바에 타이틀 보이게
        supportActionBar?.title = keyword

        configRecycle()

        // 모든 상품 정보
        viewModel.allProducts.observe(this, Observer { products ->
            productSearchAdapter.setProducts(products)
        })

        // 카테고리 값이 될 수 없는 -1 입력
        keyword?.let { runViewModel(-1, it) }

    }

    private fun configRecycle() {
        with(recycler_product_search) {
            adapter = productSearchAdapter.apply {
                registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()
                        txt_product_search.isVisible = this@apply.itemCount == 0
                    }
                })
            }
            layoutManager = LinearLayoutManager(this@ProductSearchActivity)
        }
    }

    private fun runViewModel(categoryId: Int, keyword: String) {
        viewModel.setDataSource(categoryId, keyword)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onClickProduct(productId: Long?) {

        val intent = Intent(this@ProductSearchActivity, ProductDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ProductDetailActivity.PRODUCT_ID, productId)
        }
        startActivity(intent)
    }

    companion object {
        const val KEYWORD = "keyword"
    }
}