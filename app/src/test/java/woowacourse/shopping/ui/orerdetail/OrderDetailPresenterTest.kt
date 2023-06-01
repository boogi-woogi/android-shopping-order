package woowacourse.shopping.ui.orerdetail

import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import woowacourse.shopping.data.repository.OrderRepository
import woowacourse.shopping.ui.OrderFixture
import woowacourse.shopping.ui.model.OrderUiModel
import woowacourse.shopping.ui.orderdetail.OrderDetailContract
import woowacourse.shopping.ui.orderdetail.OrderDetailPresenter

class OrderDetailPresenterTest {

    private lateinit var presenter: OrderDetailContract.Presenter
    private lateinit var view: OrderDetailContract.View
    private lateinit var repository: OrderRepository

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        view = mockk(relaxed = true)
        presenter = OrderDetailPresenter(
            view = view,
            orderRepository = repository,
            orderId = 10,
            order = null
        )
    }

    @Test
    fun `저장소로부터 주문 식별번호에 해당하는 주문 정보를 얻어온 후 뷰를 초기화한다`() {
        // given
        val order = OrderFixture.createOrder()
        val slotInitView = slot<(order: OrderUiModel) -> Unit>()
        every {
            repository.getOrder(
                orderId = 10,
                onReceived = capture(slotInitView)
            )
        }.answers {
            slotInitView.invoke(order)
        }

        // when: 저장소로부터 주문 정보룰 받아온다.
        presenter.getOrder()

        // then: 받아온 주문 정보로 뷰가 초기화된다.
        verify { view.initView(order) }
    }
}
