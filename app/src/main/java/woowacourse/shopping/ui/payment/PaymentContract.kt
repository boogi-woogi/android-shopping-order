package woowacourse.shopping.ui.payment

import woowacourse.shopping.ui.model.BasketProductUiModel
import woowacourse.shopping.ui.model.UserUiModel

interface PaymentContract {

    interface View {

        fun initView(
            user: UserUiModel,
            basketProducts: List<BasketProductUiModel>,
            totalPrice: Int,
        )

        fun showOrderDetail(orderId: Int)
    }

    interface Presenter {

        fun getUser()

        fun addOrder(usingPoint: Int)
    }
}
