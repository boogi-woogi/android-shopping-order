package woowacourse.shopping.ui.shopping

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import woowacourse.shopping.R
import woowacourse.shopping.data.database.ShoppingDatabase
import woowacourse.shopping.data.database.dao.recentproduct.RecentProductDaoImpl
import woowacourse.shopping.data.datasource.basket.BasketRemoteDataSourceImpl
import woowacourse.shopping.data.datasource.product.ProductRemoteDataSourceImpl
import woowacourse.shopping.data.datasource.recentproduct.local.LocalRecentProductDataSource
import woowacourse.shopping.data.repository.BasketRepositoryImpl
import woowacourse.shopping.data.repository.ProductRepositoryImpl
import woowacourse.shopping.data.repository.RecentProductRepositoryImpl
import woowacourse.shopping.databinding.ActivityShoppingBinding
import woowacourse.shopping.ui.basket.BasketActivity
import woowacourse.shopping.ui.model.UiProduct
import woowacourse.shopping.ui.model.UiRecentProduct
import woowacourse.shopping.ui.productdetail.ProductDetailActivity
import woowacourse.shopping.ui.shopping.ShoppingViewType.MORE_BUTTON
import woowacourse.shopping.ui.shopping.ShoppingViewType.PRODUCT
import woowacourse.shopping.ui.shopping.ShoppingViewType.RECENT_PRODUCTS
import woowacourse.shopping.ui.shopping.morebutton.MoreButtonAdapter
import woowacourse.shopping.ui.shopping.product.ProductAdapter
import woowacourse.shopping.ui.shopping.recentproduct.RecentProductAdapter
import woowacourse.shopping.ui.shopping.recentproduct.RecentProductWrapperAdapter
import woowacourse.shopping.ui.shopping.skeleton.SkeletonProductAdapter
import woowacourse.shopping.util.setThrottleFirstOnClickListener
import woowacourse.shopping.util.turnOffSupportChangeAnimation

class ShoppingActivity : AppCompatActivity(), ShoppingContract.View {

    private lateinit var presenter: ShoppingPresenter
    private lateinit var binding: ActivityShoppingBinding

    private lateinit var recentProductWrapperAdapter: RecentProductWrapperAdapter
    private lateinit var recentProductAdapter: RecentProductAdapter
    private lateinit var productAdapter: ProductAdapter
    private lateinit var moreButtonAdapter: MoreButtonAdapter
    private lateinit var concatAdapter: ConcatAdapter

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val returnPoint = result.data?.getBooleanExtra(FROM_ANOTHER_ACTIVITY, false) ?: false
            if (returnPoint) {
                presenter.updateBasket()
                presenter.fetchRecentProducts()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shopping)
        binding.rvShopping.turnOffSupportChangeAnimation()
        initSkeletonAdapter()
        initPresenter()
        initAdapter()
        initRecentProductsData()
        initButtonBasketClickListener()
        initShoppingRecyclerViewScrollListener()
    }

    override fun onResume() {
        super.onResume()
        initRecentProductsData()
    }

    private fun initPresenter() {
        val shoppingDatabase = ShoppingDatabase(this)
        presenter = ShoppingPresenter(
            this,
            ProductRepositoryImpl(
                ProductRemoteDataSourceImpl()
            ),
            RecentProductRepositoryImpl(
                LocalRecentProductDataSource(RecentProductDaoImpl(shoppingDatabase))
            ),
            BasketRepositoryImpl(BasketRemoteDataSourceImpl())
        )
    }

    override fun updateProducts(products: List<UiProduct>) {
        runOnUiThread {
            productAdapter.submitList(products)
        }
    }

    override fun updateRecentProducts(recentProducts: List<UiRecentProduct>) {
        recentProductAdapter.submitList(recentProducts)
        recentProductWrapperAdapter.notifyDataSetChanged()
    }

    override fun showProductDetail(
        currentProduct: UiProduct,
        currentProductBasketId: Int?,
        previousProduct: UiProduct?,
        previousProductBasketId: Int?
    ) {
        activityResultLauncher.launch(
            ProductDetailActivity.getIntent(
                this,
                currentProduct,
                currentProductBasketId,
                previousProduct,
                previousProductBasketId
            )
        )
    }

    override fun updateMoreButtonState(isVisible: Boolean) {
        moreButtonAdapter.updateItemCount(isVisible)
    }

    override fun updateTotalBasketCount(totalBasketCount: Int) {
        binding.totalBasketCount = totalBasketCount
    }

    override fun updateSkeletonState(isLoaded: Boolean) {
        binding.isLoaded = isLoaded
    }

    private fun initSkeletonAdapter() {
        binding.skeletonProduct.rvSkeletonProduct.adapter = SkeletonProductAdapter()
    }

    private fun initAdapter() {
        recentProductAdapter = RecentProductAdapter(presenter::inquiryProductDetail)
        recentProductWrapperAdapter = RecentProductWrapperAdapter(recentProductAdapter)
        productAdapter = ProductAdapter(
            presenter::inquiryProductDetail,
            presenter::minusBasketProductCount,
            presenter::plusBasketProductCount,
            presenter::addBasketProduct
        )
        moreButtonAdapter = MoreButtonAdapter(presenter::updateProducts)
        concatAdapter =
            ConcatAdapter(
                getConcatAdapterConfig(),
                recentProductWrapperAdapter,
                productAdapter,
                moreButtonAdapter
            )
        binding.rvShopping.adapter = concatAdapter
        binding.rvShopping.layoutManager = getGridLayoutManager()
    }

    private fun initRecentProductsData() {
        presenter.fetchRecentProducts()
    }

    private fun getConcatAdapterConfig(): ConcatAdapter.Config = ConcatAdapter.Config.Builder()
        .setIsolateViewTypes(false)
        .build()

    private fun getGridLayoutManager(): GridLayoutManager =
        GridLayoutManager(this, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int =
                    when (ShoppingViewType.of(concatAdapter.getItemViewType(position))) {
                        RECENT_PRODUCTS -> 2
                        PRODUCT -> 1
                        MORE_BUTTON -> 2
                    }
            }
        }

    private fun initShoppingRecyclerViewScrollListener() {
        binding.rvShopping.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = binding.rvShopping.layoutManager as GridLayoutManager
                val lastPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                if (layoutManager.itemCount <= lastPosition + LOAD_POSITION ||
                    !recyclerView.canScrollVertically(STATE_LOWEST)
                ) {
                    presenter.fetchHasNext()
                }
            }
        })
    }

    private fun initButtonBasketClickListener() {
        binding.ivBasket.setThrottleFirstOnClickListener {
            activityResultLauncher.launch(BasketActivity.getIntent(this))
        }
    }

    companion object {
        private const val LOAD_POSITION = 4
        private const val STATE_LOWEST = 1

        private const val FROM_ANOTHER_ACTIVITY = "FromAnotherActivity"

        fun getResultIntent() = Intent().putExtra(FROM_ANOTHER_ACTIVITY, true)
    }
}
