package woowacourse.shopping.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import woowacourse.shopping.data.datasource.basket.BasketProductService
import woowacourse.shopping.data.datasource.order.OrderService
import woowacourse.shopping.data.datasource.product.ProductService
import woowacourse.shopping.data.datasource.user.UserService

object NetworkModule {

    private lateinit var url: String
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(NullOnEmptyConvertFactory)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val basketProductService: BasketProductService by lazy { getService() }
    val orderService: OrderService by lazy { getService() }
    val productService: ProductService by lazy { getService() }
    val userService: UserService by lazy { getService() }

    fun setBaseUrl(url: String) {
        this.url = url
    }

    private inline fun <reified T> getService(): T {

        return retrofit.create(T::class.java)
    }
}
