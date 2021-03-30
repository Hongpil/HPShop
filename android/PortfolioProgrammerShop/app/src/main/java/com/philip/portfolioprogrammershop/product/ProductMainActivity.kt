package com.philip.portfolioprogrammershop.product


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.philip.portfolioprogrammershop.R
import com.philip.portfolioprogrammershop.common.Prefs
import com.philip.portfolioprogrammershop.product.detail.ProductDetailActivity
import com.philip.portfolioprogrammershop.product.list.ProductListPagedAdapter
import com.philip.portfolioprogrammershop.product.list.ProductListViewModel
import com.philip.portfolioprogrammershop.product.registration.ProductRegistrationActivity
import com.philip.portfolioprogrammershop.product.search.ProductSearchActivity
import com.philip.portfolioprogrammershop.signin.SigninActivity
import kotlinx.android.synthetic.main.activity_product_main.*
import kotlinx.android.synthetic.main.activity_product_main_body.*
import kotlinx.android.synthetic.main.activity_product_main_body.recycler_product_list
import kotlinx.android.synthetic.main.activity_product_main_body.txt_product_list_fragment
import kotlinx.android.synthetic.main.fragment_product_list.*


class ProductMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ProductListPagedAdapter.OnItemClickListener {

    private val viewModel: ProductListViewModel by viewModels()
    var selectedCategoryId: Int? = null
    private val productListFragmentAdapter by lazy {
        ProductListPagedAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_main)

        setSupportActionBar(product_layout_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게)

        product_navigationView.setNavigationItemSelectedListener(this@ProductMainActivity)
        val header = product_navigationView.getHeaderView(0)
        val textHeader = header.findViewById<TextView>(R.id.header_nickname)
        textHeader?.run {
            text = Prefs.userName
        }

        configRecycle()

        val items = resources.getStringArray(R.array.spinner_array)
        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner_product_list.adapter = myAdapter
        spinner_product_list.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when(position) {
                    0   ->  {
                        runViewModel(0, null)
                        selectedCategoryId = 0
                    }
                    1   ->  {
                        runViewModel(1, null)
                        selectedCategoryId = 1
                    }
                    2   ->  {
                        runViewModel(2, null)
                        selectedCategoryId = 2
                    }
                    3   ->  {
                        runViewModel(3, null)
                        selectedCategoryId = 3
                    }
                    4   ->  {
                        runViewModel(4, null)
                        selectedCategoryId = 4
                    }
                    else -> {
                        runViewModel(0, null)
                        selectedCategoryId = 0
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        // 모든 상품 정보
        viewModel.allProducts.observe(this, Observer { products ->
            productListFragmentAdapter.setProducts(products)
        })

        btn_floatingAction.setOnClickListener {
            val intent = Intent(this@ProductMainActivity, ProductRegistrationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }

        product_searchView.setIconifiedByDefault(true) // true : SearchView 가 Icon 화 되어 시작됨 / false : SearchView 가 펼쳐진 상태에서 시작됨
        product_searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            // 입력 후 엔터 버튼 클릭 시 호출
            override fun onQueryTextSubmit(query: String?): Boolean {
                product_searchView.setQuery("", false)
                product_searchView.clearFocus()
                query?.let {
                    val intent = Intent(this@ProductMainActivity, ProductSearchActivity::class.java).apply {
                        putExtra(ProductSearchActivity.KEYWORD, query)
                    }
                    startActivity(intent)
                } ?: Toast.makeText(this@ProductMainActivity, "검색 키워드를 입력해주세요.", Toast.LENGTH_SHORT).show()

                return false
            }
            // 타이핑 하는 동안 출력
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }


    /**
     * 수정화면에서 수정 후 돌와왔을 때, 수정된 내용을 바로 보여준다.
     */
    override fun onResume() {
        super.onResume()
        selectedCategoryId?.let { runViewModel(it, null) }
    }

    private fun configRecycle() {
        with(recycler_product_list) {
            adapter = productListFragmentAdapter.apply {
                registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()
                        txt_product_list_fragment.isVisible = this@apply.itemCount == 0
                    }
                })
            }
            layoutManager = LinearLayoutManager(this@ProductMainActivity)
        }
    }

    private fun runViewModel(categoryId: Int, keyword:String?) {
        viewModel.setDataSource(categoryId, keyword)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {  // 메뉴 버튼
                product_drawerLayout.openDrawer(GravityCompat.START)  // 네비게이션 드로어 열기
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_signout -> {
                Prefs.token = null
                Prefs.refreshToken = null
                startActivity(Intent(this@ProductMainActivity, SigninActivity::class.java))
            }
        }

        product_drawerLayout.closeDrawer(product_navigationView)

        return true
    }

    override fun onBackPressed() {
        if (product_drawerLayout.isDrawerOpen(GravityCompat.START)) {
            product_drawerLayout.closeDrawers()
            //
        } else {
            super.onBackPressed()
        }
    }

    override fun onClickProduct(productId: Long?) {
        val intent = Intent(this@ProductMainActivity, ProductDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ProductDetailActivity.PRODUCT_ID, productId)
        }
        startActivity(intent)
    }

}