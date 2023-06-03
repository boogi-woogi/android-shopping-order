package woowacourse.shopping.data.datasource.basket

import okhttp3.ResponseBody
import woowacourse.shopping.data.NetworkModule
import woowacourse.shopping.data.model.BasketProductEntity
import woowacourse.shopping.data.model.ProductEntity
import woowacourse.shopping.data.remote.OkHttpModule

class BasketRemoteDataSourceImpl : BasketRemoteDataSource {

    private val basketProductService: BasketProductService = NetworkModule.getService()

    override fun getAll(
        onReceived: (List<BasketProductEntity>) -> Unit,
        onFailed: (errorMessage: String) -> Unit,
    ) {
        basketProductService.requestBasketProducts(
            authorization = OkHttpModule.AUTHORIZATION_FORMAT.format(OkHttpModule.encodedUserInfo)
        ).enqueue(object : retrofit2.Callback<List<BasketProductEntity>> {

            override fun onResponse(
                call: retrofit2.Call<List<BasketProductEntity>>,
                response: retrofit2.Response<List<BasketProductEntity>>,
            ) {
                response.body()?.let {
                    onReceived(it)
                } ?: onFailed(BASKET_PRODUCTS_ERROR)
            }

            override fun onFailure(call: retrofit2.Call<List<BasketProductEntity>>, t: Throwable) {
                onFailed(BASKET_PRODUCTS_ERROR)
            }
        })
    }

    override fun add(
        product: ProductEntity,
        onAdded: (Int) -> Unit,
        onFailed: (errorMessage: String) -> Unit,
    ) {
        basketProductService.addBasketProduct(
            authorization = OkHttpModule.AUTHORIZATION_FORMAT.format(OkHttpModule.encodedUserInfo),
            productId = product.id
        ).enqueue(object : retrofit2.Callback<retrofit2.Response<ResponseBody>> {

            override fun onResponse(
                call: retrofit2.Call<retrofit2.Response<ResponseBody>>,
                response: retrofit2.Response<retrofit2.Response<ResponseBody>>,
            ) {
                response.headers()[LOCATION]?.let {
                    val productId = it.split("/").last().toInt()

                    onAdded(productId)
                } ?: onFailed(FAILED_TO_ADD_BASKET)
            }

            override fun onFailure(
                call: retrofit2.Call<retrofit2.Response<ResponseBody>>,
                t: Throwable,
            ) {
                onFailed(FAILED_TO_ADD_BASKET)
            }
        })
    }

    override fun update(
        basketProduct: BasketProductEntity,
        onUpdated: () -> Unit,
        onFailed: (errorMessage: String) -> Unit,
    ) {
        basketProductService.updateBasketProduct(
            authorization = OkHttpModule.AUTHORIZATION_FORMAT.format(OkHttpModule.encodedUserInfo),
            cartItemId = basketProduct.id.toString(),
            quantity = basketProduct.count
        ).enqueue(object : retrofit2.Callback<retrofit2.Response<ResponseBody>> {

            override fun onResponse(
                call: retrofit2.Call<retrofit2.Response<ResponseBody>>,
                response: retrofit2.Response<retrofit2.Response<ResponseBody>>,
            ) {
                if (response.isSuccessful) {
                    onUpdated()
                } else {
                    onFailed(FAILED_TO_UPDATE_COUNT)
                }
            }

            override fun onFailure(
                call: retrofit2.Call<retrofit2.Response<ResponseBody>>,
                t: Throwable,
            ) {
                onFailed(FAILED_TO_UPDATE_COUNT)
            }
        })
    }

    override fun remove(
        basketProduct: BasketProductEntity,
        onRemoved: () -> Unit,
        onFailed: (errorMessage: String) -> Unit,
    ) {
        basketProductService.removeBasketProduct(
            authorization = OkHttpModule.AUTHORIZATION_FORMAT.format(OkHttpModule.encodedUserInfo),
            cartItemId = basketProduct.id.toString(),
        ).enqueue(object : retrofit2.Callback<retrofit2.Response<ResponseBody>> {

            override fun onResponse(
                call: retrofit2.Call<retrofit2.Response<ResponseBody>>,
                response: retrofit2.Response<retrofit2.Response<ResponseBody>>,
            ) {
                if (response.isSuccessful) {
                    onRemoved()
                } else {
                    onFailed(FAILED_TO_REMOVE_PRODUCT)
                }
            }

            override fun onFailure(
                call: retrofit2.Call<retrofit2.Response<ResponseBody>>,
                t: Throwable,
            ) {
                onFailed(FAILED_TO_REMOVE_PRODUCT)
            }
        })
    }

    companion object {
        private const val LOCATION = "Location"
        private const val BASKET_PRODUCTS_ERROR = "장바구니 상품을 불러올 수 없습니다."
        private const val FAILED_TO_ADD_BASKET = "장바구니 상품을 불러올 수 없습니다."
        private const val FAILED_TO_UPDATE_COUNT = "장바구니 상품의 수량을 변경에 실패했습니다."
        private const val FAILED_TO_REMOVE_PRODUCT = "장바구니 상품 삭제에 실패했습니다."
    }
}
