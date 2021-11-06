package dev.forcecodes.truckme.core.data.admin

import com.google.firebase.firestore.FirebaseFirestore
import dev.forcecodes.truckme.core.util.Result
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

interface AdminDataSource {

  suspend fun getAdminContactNumber(assignedAdmin: String?): Flow<Result<String>>
}

class AdminDataSourceImpl @Inject constructor(
  private val firestore: FirebaseFirestore
) : AdminDataSource {

  companion object {
    private const val EMPTY_ADMIN_ID = "Assigned admin ID does not exists."
  }

  override suspend fun getAdminContactNumber(assignedAdmin: String?): Flow<Result<String>> {
    return flow {
      var state: Result<String> = Result.Loading
      emit(state)

      if (assignedAdmin.isNullOrEmpty()) {
        emit(Result.Error(Exception(EMPTY_ADMIN_ID)))
        currentCoroutineContext()
          .cancel(CancellationException(EMPTY_ADMIN_ID))
      } else {
        firestore.collection("admin")
          .document(assignedAdmin)
          .get()
          .addOnSuccessListener {
            state = Result.Success(it["phoneNumber"] as String)
          }.await()
        emit(state)
      }
    }
  }
}