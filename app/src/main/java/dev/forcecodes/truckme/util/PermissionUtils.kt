package dev.forcecodes.truckme.util

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import dev.forcecodes.truckme.R

/**
 * Utility class for access to runtime permissions.
 */
object PermissionUtils {

  val ALL_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
  )

  const val LOCATION_PERMISSION = 2
  const val READ_EXTERNAL_PERMISSION = 1

  val REQUEST_CODE = arrayOf(
    LOCATION_PERMISSION,
    READ_EXTERNAL_PERMISSION
  )

  @JvmStatic
  fun AppCompatActivity.requestMultiplePermissions() {
    registerForActivityResult(
      ActivityResultContracts.RequestMultiplePermissions()
    ) {

    }.launch(
      ALL_PERMISSIONS
    )
  }

  @JvmStatic
  fun checkSelfPermission(
    context: Context,
    permission: String,
    requestCode: Int = READ_EXTERNAL_PERMISSION,
    permissionGranted: () -> Unit = {}
  ) {
    if (ContextCompat.checkSelfPermission(context, permission)
      == PackageManager.PERMISSION_GRANTED
    ) {
      permissionGranted()
    } else {
      requestPermission(
        context as AppCompatActivity, requestCode,
        permission
      )
    }
  }

  /**
   * Requests the fine location permission. If a rationale with an additional explanation should
   * be shown to the user, displays a dialog that triggers the request.
   */
  @JvmStatic
  fun requestPermission(
    activity: AppCompatActivity, requestId: Int,
    permission: String
  ) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
      // Display a dialog with rationale.
      RationaleDialog.newInstance(requestId)
        .show(activity.supportFragmentManager, "dialog")
    } else {
      // Location permission has not been granted yet, request it.
      ActivityCompat.requestPermissions(
        activity,
        arrayOf(permission),
        requestId
      )
    }
  }

  /**
   * Checks if the result contains a [PackageManager.PERMISSION_GRANTED] result for a
   * permission from a runtime permissions request.
   *
   * @see androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
   */
  @JvmStatic
  fun isPermissionGranted(
    grantPermissions: Array<String>, grantResults: IntArray,
    permission: String
  ): Boolean {
    for (i in grantPermissions.indices) {
      if (permission == grantPermissions[i]) {
        return grantResults[i] == PackageManager.PERMISSION_GRANTED
      }
    }
    return false
  }

  /**
   * A dialog that displays a permission denied message.
   */
  class PermissionDeniedDialog : DialogFragment() {
    private var finishActivity = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
      finishActivity =
        arguments?.getBoolean(ARGUMENT_FINISH_ACTIVITY) ?: false
      return AlertDialog.Builder(activity)
        .setMessage(R.string.location_permission_denied)
        .setPositiveButton(android.R.string.ok, null)
        .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
      super.onDismiss(dialog)
      if (finishActivity) {
        Toast.makeText(
          activity, R.string.permission_required_toast,
          Toast.LENGTH_SHORT
        ).show()
      }
    }

    companion object {
      private const val ARGUMENT_FINISH_ACTIVITY = "finish"

      /**
       * Creates a new instance of this dialog and optionally finishes the calling Activity
       * when the 'Ok' button is clicked.
       */
      @JvmStatic
      fun newInstance(finishActivity: Boolean): PermissionDeniedDialog {
        val arguments = Bundle().apply {
          putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
        }
        return PermissionDeniedDialog().apply {
          this.arguments = arguments
        }
      }
    }
  }

  /**
   * A dialog that explains the use of the location permission and requests the necessary
   * permission.
   *
   *
   * The activity should implement
   * [androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback]
   * to handle permit or denial of this permission request.
   */
  class RationaleDialog : DialogFragment() {
    private var finishActivity = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
      val requestCode =
        arguments?.getInt(ARGUMENT_PERMISSION_REQUEST_CODE) ?: 0
      finishActivity =
        arguments?.getBoolean(ARGUMENT_FINISH_ACTIVITY) ?: false
      return AlertDialog.Builder(activity)
        .setMessage(R.string.permission_rationale_location)
        .setPositiveButton(android.R.string.ok) { _, _ -> // After click on Ok, request the permission.
          ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            requestCode
          )
          // Do not finish the Activity while requesting permission.
          finishActivity = false
        }
        .setNegativeButton(android.R.string.cancel, null)
        .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
      super.onDismiss(dialog)
      if (finishActivity) {
        Toast.makeText(
          activity,
          R.string.permission_required_toast,
          Toast.LENGTH_SHORT
        ).show()
      }
    }

    companion object {
      private const val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"
      private const val ARGUMENT_FINISH_ACTIVITY = "finish"

      /**
       * Creates a new instance of a dialog displaying the rationale for the use of the location
       * permission.
       *
       *
       * The permission is requested after clicking 'ok'.
       *
       * @param requestCode    Id of the request that is used to request the permission. It is
       * returned to the
       * [androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback].
       * @param finishActivity Whether the calling Activity should be finished if the dialog is
       * cancelled.
       */
      @JvmStatic
      fun newInstance(requestCode: Int): RationaleDialog {
        val arguments = Bundle().apply {
          putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
        }
        return RationaleDialog().apply {
          this.arguments = arguments
        }
      }
    }
  }
}