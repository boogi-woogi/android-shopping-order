package woowacourse.shopping.domain.repository

import woowacourse.shopping.domain.Product
import woowacourse.shopping.domain.RecentProduct

interface RecentProductRepository {
    fun add(product: Product)
    fun getPartially(size: Int): List<RecentProduct>
}
