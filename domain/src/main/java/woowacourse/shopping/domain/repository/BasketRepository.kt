package woowacourse.shopping.domain.repository

import woowacourse.shopping.domain.BasketProduct
import woowacourse.shopping.domain.Product

interface BasketRepository {

    fun getAll(
        onReceived: (List<BasketProduct>) -> Unit,
        onFailed: (errorMessage: String) -> Unit,
    )

    fun add(
        product: Product,
        onAdded: (Int) -> Unit,
        onFailed: (errorMessage: String) -> Unit
    )

    fun update(basketProduct: BasketProduct)

    fun remove(basketProduct: BasketProduct)
}
