package woowacourse.shopping.data.datasource.recentproduct

import woowacourse.shopping.data.database.dao.recentproduct.RecentProductDao
import woowacourse.shopping.data.model.DataProduct
import woowacourse.shopping.data.model.DataRecentProduct

class RecentProductLocalDataSourceImpl(private val dao: RecentProductDao) :
    RecentProductLocalDataSource.Local {

    override fun getPartially(size: Int): List<DataRecentProduct> = dao.getPartially(size)

    override fun add(product: DataProduct) {
        while (dao.getSize() >= STORED_DATA_SIZE) {
            dao.removeLast()
        }
        dao.add(product)
    }

    companion object {
        private const val STORED_DATA_SIZE = 50
    }
}
