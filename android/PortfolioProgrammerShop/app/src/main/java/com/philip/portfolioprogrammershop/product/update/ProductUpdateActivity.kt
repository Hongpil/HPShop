package com.philip.portfolioprogrammershop.product.update

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.philip.portfolioprogrammershop.R
import com.philip.portfolioprogrammershop.api.ApiGenerator
import com.philip.portfolioprogrammershop.api.request.ProductUpdateRequest
import com.philip.portfolioprogrammershop.api.response.ApiResponse
import com.philip.portfolioprogrammershop.api.response.ProductListItemResponse
import com.philip.portfolioprogrammershop.common.Prefs
import com.philip.portfolioprogrammershop.databinding.ActivityProductUpdateBinding
import kotlinx.android.synthetic.main.activity_product_registration.product_registration_toolbar
import kotlinx.android.synthetic.main.activity_product_registration.progressBar
import kotlinx.android.synthetic.main.activity_product_update.*
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ProductUpdateActivity : AppCompatActivity(), View.OnClickListener {

    private val viewModel: ProductUpdateViewModel by viewModels()
    private var profileImage: File? = null
    private var isImageSelect: Boolean = false
    private var product_id: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = DataBindingUtil.setContentView<ActivityProductUpdateBinding>(
            this,
            R.layout.activity_product_update
        )
        binding.lifecycleOwner = this
        binding.xmlViewModel = viewModel


        setSupportActionBar(product_registration_toolbar) // 툴바를 액티비티의 앱바로 지정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_white_back_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(true) // 툴바에 타이틀 보이게
        supportActionBar?.title = "상품 등록"


        product_id = intent.getLongExtra(PRODUCT_ID, -1)
        val product_name: String? = intent.getStringExtra(PRODUCT_NAME)
        val product_description: String? = intent.getStringExtra(PRODUCT_DESCRIPTION)
        val product_price: Int = intent.getIntExtra(PRODUCT_PRICE, 0)
        val product_image: String? = intent.getStringExtra(PRODUCT_PATH)
        if (product_name != null && product_description != null && product_image != null) {
            setInitView(product_name, product_description, product_price, product_image)
        }

    }

    private fun setInitView(name:String, description:String, price:Int, image:String) {

        Glide.with(this)
            .load("${ApiGenerator.URL_DEFAULT}/${image}")
            .centerCrop()
            .into(img_product_update)

        edt_update_name.setText(name)
        edt_update_description.setText(description)
        edt_update_price.setText(price.toString())
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


    /**
     * 이미지 선택
     */
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.img_product_update -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                intent.resolveActivity(packageManager)?.let {
                    startActivityForResult(intent, REQUEST_PICK_IMAGES)
                }
            }
            R.id.btn_registration -> {
                lifecycleScope.launch {
                    updater()
                }
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK)
            return

        when (requestCode) {
            REQUEST_PICK_IMAGES -> data?.let { showImage(it) }
        }
    }

    private fun showImage(intent: Intent) =
            getContent(intent.data)?.let { imageFile ->
                profileImage = imageFile
                isImageSelect = true
                Glide.with(this)
                        .load(profileImage)
                        .centerCrop()
                        .into(img_product_update)
            }

    fun getContent(uri: Uri?): File? = uri?.let {
        this.contentResolver.openInputStream(uri)?.use { input ->
            val ext = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(this.contentResolver.getType(uri))
            } else {
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
            }
            val file = File.createTempFile("FILE_${System.currentTimeMillis()}", ".$ext", this.cacheDir)
            file.outputStream().use { output ->
                input.copyTo(output)
                return file
            }
        }
        return null
    }

    suspend fun updater() {
        // 상품 이미지가 선택된 경우
        if (isImageSelect) {

            profileImage.let {

                visibleProgressBar()

                val mediaType = MediaType.parse("image/*")
                var requestBody : RequestBody = RequestBody.create(mediaType, profileImage)
                var multipartBody : MultipartBody.Part = MultipartBody.Part.createFormData("upload", profileImage?.name, requestBody)

                val request = extractRequest(multipartBody, requestBody, product_id)
                val response = ProductUpdater().updater(request)
                onRegistrationResponse(response)

            }
        } else {
            Toast.makeText(this, "상품 이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractRequest(multipartBody: MultipartBody.Part, requestBody: RequestBody, productId: Long): ProductUpdateRequest =
        ProductUpdateRequest(
            Prefs.token,
            Prefs.userId,
            productId,
            viewModel.productName.value,
            viewModel.productDescription.value,
            viewModel.productPrice.value?.toIntOrNull(),
            multipartBody,
            requestBody
            )

    private fun onRegistrationResponse(response: ApiResponse<ProductListItemResponse>) {
        if (response.success && response.message == "product update success") {
            goneProgressBar()
            showPopup()
        } else {
            Toast.makeText(this, response.message ?: "알 수 없는 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPopup() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "상품이 수정되었습니다."

        val alertDialog = AlertDialog.Builder(this)
                .setTitle("수정 완료")
                .setPositiveButton("확인") { dialog, which ->
                    finish()
                }
                .create()

        alertDialog.setView(view)
        alertDialog.show()
    }


    private fun visibleProgressBar() {
        progressBar.visibility = View.VISIBLE
    }
    private fun goneProgressBar() {
        progressBar.visibility = View.GONE
    }

    companion object {
        const val REQUEST_PICK_IMAGES = 0

        const val PRODUCT_ID = "productId"
        const val PRODUCT_NAME = "productName"
        const val PRODUCT_DESCRIPTION = "productDescription"
        const val PRODUCT_PRICE = "productPrice"
        const val PRODUCT_PATH = "productPath"
    }

}