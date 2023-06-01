package woowacourse.shopping.ui

import woowacourse.shopping.domain.Price
import woowacourse.shopping.domain.Product

object ProductFixture {

    fun createProduct() = Product(
        id = 1,
        name = "더미입니다만",
        price = Price(1000),
        imageUrl = "url"
    )
}