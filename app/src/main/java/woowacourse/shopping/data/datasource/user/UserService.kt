package woowacourse.shopping.data.datasource.user

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import woowacourse.shopping.data.model.UserRequest

interface UserService {

    @GET("users")
    fun getUser(
        @Header("Authorization")
        authorization: String,
    ): Call<UserRequest>
}
