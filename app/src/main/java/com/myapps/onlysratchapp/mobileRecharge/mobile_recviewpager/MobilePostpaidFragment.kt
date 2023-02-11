package com.myapps.onlysratchapp.recharge_services.mobile_recviewpager

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.myapps.onlysratchapp.R
import com.myapps.onlysratchapp.mobileRecharge.AllRechargeReportsActivity
import com.myapps.onlysratchapp.mobileRecharge.adapter.OperatorListAdapter
import com.myapps.onlysratchapp.mobileRecharge.model.OperatorsModel
import com.myapps.onlysratchapp.network_calls.UserModel

import com.myapps.onlysratchapp.network_calls.AppApiCalls
import com.myapps.onlysratchapp.network_calls.AppApiUrl
import com.myapps.onlysratchapp.utils.*
import kotlinx.android.synthetic.main.fragment_mobile_postpaid.view.*
import kotlinx.android.synthetic.main.layout_list_bottomsheet.view.*


import org.json.JSONObject
import java.util.*

class MobilePostpaidFragment : Fragment(), AppApiCalls.OnAPICallCompleteListener,
    OperatorListAdapter.ListAdapterListener {

    lateinit var operatorAdapter: OperatorListAdapter
    var operatorsModelArrayList = ArrayList<OperatorsModel>()
    var bottomSheetDialog: BottomSheetDialog? = null

    lateinit var root: View



    var operator_code: String = ""
    lateinit var dialog: Dialog

    lateinit var cus_mobile: String
    var cus_type="retailer"
    var cus_pin=""
    var cus_pass=""
    var cus_id=""


    companion object {
        fun newInstance(message: String): MobilePostpaidFragment {

            val f = MobilePostpaidFragment()

            val bdl = Bundle(1)

            bdl.putString(AlarmClock.EXTRA_MESSAGE, message)

            f.setArguments(bdl)

            return f

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_mobile_postpaid, container, false)

        cus_mobile = Constant.getString(activity, Constant.USER_NUMBER)
        cus_id = Constant.getString(activity, Constant.USER_ID)

        initView(root)
        getBalanceApi(cus_mobile)

        return root
    }


    fun initView(root: View) {

        root.cvWalletBalancePostpaid.setBackgroundResource(R.drawable.bg_leftcurved)
        root.cvBrowsePlans.setBackgroundResource(R.drawable.bg_rightcurved)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        getOperatorApi(AppConstants.OPERATOR_POSTPAID)

        view.tvChooseOperator.setOnClickListener {
            showOperatorsBottomSheet()
        }



        view.cvRechargePostpaidBtn.setOnClickListener {

            if (!AppCommonMethods.checkForMobile(view.etMobileNumberPostpaid)) {

                view.etMobileNumberPostpaid.requestFocus()
                view.etMobileNumberPostpaid.error =
                    requireContext().getString(R.string.error_mobile_number)
                return@setOnClickListener
            } else if (view.etAmountPostpaid.text.isNullOrEmpty()) {

                view.etAmountPostpaid.requestFocus()
                view.etAmountPostpaid.error =
                    requireContext().getString(R.string.error_invalid_amount)
                return@setOnClickListener
            } else {

                checkIfSameRecharge(cus_mobile,
                    view.etMobileNumberPostpaid.text.toString(),
                    view.etAmountPostpaid.text.toString(),operator_code)

            }


        }
    }

    //API CALL FUNCTION DEFINITION
    private fun getOperatorApi(
        operator_type: String
    ) {
        root.progress_bar.visibility = View.VISIBLE
        if (AppCommonMethods(context).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                requireContext(),
                AppConstants.OPERATOR_API,
                this
            )
            mAPIcall.getOperators(operator_type)

        } else {
            requireContext().toast(getString(R.string.error_internet))
        }
    }

    private fun getBalanceApi(
        cus_id: String
    ) {
        root.progress_bar.visibility = View.VISIBLE

        if (AppCommonMethods(context).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                requireContext(),
                AppConstants.BALANCE_API,
                this
            )
            mAPIcall.getBalance(cus_id)

        } else {
            requireContext().toast(getString(R.string.error_internet))
        }
    }


    private fun checkIfSameRecharge(
        cus_id: String,
        rec_mobile: String,
        amount: String,
        operator: String
    ) {
        root.progress_bar.visibility = View.VISIBLE

        if (AppCommonMethods(requireContext()).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                requireContext(),
                AppConstants.CHECK_SAME_RECHARGE_API,
                this
            )
            mAPIcall.checkIfSameRecharge(cus_id, rec_mobile, amount, operator)

        } else {
            requireContext().toast(getString(R.string.error_internet))
        }
    }

  /*  private fun verifyPin(
        cus_mobile: String,
        pin: String
    ) {
        root.progress_bar.visibility = View.VISIBLE

        if (AppCommonMethods(requireContext()).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                requireContext(),
                AppConstants.VERFY_PIN_API,
                this
            )
            mAPIcall.verifyPin(cus_mobile, pin)

        } else {
            requireContext().toast(getString(R.string.error_internet))
        }
    }*/

    private fun rechargeApi(
        cus_id: String,
        rec_mobile: String,
        amount: String,
        operator: String,
        cus_type: String,
    ) {
        root.progress_bar.visibility = View.VISIBLE

        if (AppCommonMethods(requireContext()).isNetworkAvailable) {
            val mAPIcall = AppApiCalls(
                requireContext(),
                AppConstants.RECHARGE_API,
                this
            )
            mAPIcall.rechargeApi(cus_id, rec_mobile, amount, operator, cus_type)

        } else {
            requireContext().toast(getString(R.string.error_internet))
        }
    }

    override fun onAPICallCompleteListner(item: Any?, flag: String?, result: String) {
        if (flag.equals(AppConstants.OPERATOR_API)) {
            Log.e(AppConstants.OPERATOR_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            Log.e(AppConstants.STATUS, status)
            if (status.contains("true")) {
                root.progress_bar.visibility = View.GONE

                val cast = jsonObject.getJSONArray("result")

                for (i in 0 until cast.length()) {
                    val notifyObjJson = cast.getJSONObject(i)
                    val operatorname = notifyObjJson.getString("operatorname")
                    Log.e("operator_name ", operatorname)
                    val operatorsModel = Gson()
                        .fromJson(
                            notifyObjJson.toString(),
                            OperatorsModel::class.java
                        )

                    operatorsModelArrayList.add(operatorsModel)
                }


            } else {

                root.progress_bar.visibility = View.GONE


            }
        }
        if (flag.equals(AppConstants.BALANCE_API)) {
            root.progress_bar.visibility = View.GONE
            Log.e(AppConstants.BALANCE_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            val messageCode = jsonObject.getString(AppConstants.MESSAGE)

            //   val token = jsonObject.getString(AppConstants.TOKEN)
            Log.e(AppConstants.STATUS, status)
            Log.e(AppConstants.MESSAGE, messageCode)
            if (status.contains(AppConstants.TRUE)) {


                root.tvWalletBalance.text =
                    "${getString(R.string.Rupee)} ${jsonObject.getString(AppConstants.WALLETBALANCE)}"
                /* tvAepsBalance.text =
                     "${getString(R.string.Rupee)} ${jsonObject.getString(AEPSBALANCE)}"*/

            } else {
                root.progress_bar.visibility = View.GONE

                if (messageCode.equals(getString(R.string.error_expired_token))) {
                    AppCommonMethods.logoutOnExpiredDialog(requireContext())
                } else {
                    requireContext().toast(messageCode.trim())
                }
            }
        }
        if (flag.equals(AppConstants.CHECK_SAME_RECHARGE_API)) {
            Log.e(AppConstants.CHECK_SAME_RECHARGE_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            val message = jsonObject.getString(AppConstants.MESSAGE)
            Log.e(AppConstants.STATUS, status)
            if (status.contains("true")) {
                root.progress_bar.visibility = View.GONE
                rechargeApi(
                    cus_id, root.etMobileNumberPostpaid.text.toString(),
                    root.etAmountPostpaid.text.toString(), operator_code,cus_type
                )
              //  verifyPin(cus_mobile, AppPrefs.getStringPref("AppPassword",context).toString())
                //  confirmPinDialog()

            } else {

                root.progress_bar.visibility = View.GONE
                showMessageDialog(getString(R.string.error_attention), message)

            }
        }
        if (flag.equals(AppConstants.RECHARGE_API)) {
            Log.e(AppConstants.RECHARGE_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            Log.e(AppConstants.STATUS, status)
            if (status.contains(AppConstants.TRUE)) {
                root.progress_bar.visibility = View.GONE

                val resultObject = jsonObject.getJSONObject("result")
                val message = resultObject.getString(AppConstants.MESS)

                showSuccessOrFailedDialog(getString(R.string.status), message)


            } else {
                val resultObject = jsonObject.getJSONObject("result")
                val message = resultObject.getString(AppConstants.MESS)
                root.progress_bar.visibility = View.GONE
                showSuccessOrFailedDialog(getString(R.string.status), message)


            }
        }
        if (flag.equals(AppConstants.VERFY_PIN_API)) {
            Log.e(AppConstants.VERFY_PIN_API, result)
            val jsonObject = JSONObject(result)
            val status = jsonObject.getString(AppConstants.STATUS)
            val message = jsonObject.getString(AppConstants.MESSAGE)
            Log.e(AppConstants.STATUS, status)
            if (status.contains(AppConstants.TRUE)) {
                root.progress_bar.visibility = View.GONE
                rechargeApi(
                    cus_id, root.etMobileNumberPostpaid.text.toString(),
                    root.etAmountPostpaid.text.toString(), operator_code,cus_type
                )

            } else {

                root.progress_bar.visibility = View.GONE
                showMessageDialog(getString(R.string.error_attention), message)

            }
        }
    }


    private fun showOperatorsBottomSheet() {
        val view: View = layoutInflater.inflate(R.layout.layout_list_bottomsheet, null)
        view.rvspinner.apply {

            layoutManager = LinearLayoutManager(requireContext())
            view.rvspinner.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

            operatorAdapter = OperatorListAdapter(
                context, operatorsModelArrayList, this@MobilePostpaidFragment
            )
            view.rvspinner.adapter = operatorAdapter
        }

        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.SheetDialog)
        bottomSheetDialog!!.setContentView(view)
        val bottomSheetBehavior: BottomSheetBehavior<*> =
            BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.peekHeight = 600
        bottomSheetDialog!!.show()
    }

    override fun onClickAtOKButton(operatorsModel: OperatorsModel?) {
        if (operatorsModel != null) {
            root.tvChooseOperator.text = operatorsModel.operatorname


            operator_code = operatorsModel.opcodenew!!.trim()
//            opName = mobileRechargeModal.operatorname!!.trim()
            Glide.with(this)
                .load(AppApiUrl.IMAGE_URL + operatorsModel.operator_image)
                .into(root.ivOperatorImagePostpaid)
            bottomSheetDialog!!.dismiss()
        }
    }


    private fun clearData(root: View) {
        root.etMobileNumberPostpaid.setText("")
        root.tvChooseOperator.setText("")
        root.ivOperatorImagePostpaid.setImageDrawable(resources.getDrawable(R.drawable.icons_cellulartower))
        root.etAmountPostpaid.setText("")
    }

    override fun onResume() {
        super.onResume()
        clearData(root)
    }

    private fun showMessageDialog(title: String, message: String) {
        val builder1 =
            AlertDialog.Builder(requireContext())
        builder1.setTitle("" + title)
        builder1.setMessage("" + message)
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "OK"
        ) { dialog, id ->

            dialog.dismiss()
        }
        /*
        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });*/
        val alert11 = builder1.create()
        alert11.show()
    }

    private fun showSuccessOrFailedDialog(title: String, message: String) {
        val builder1 =
            AlertDialog.Builder(requireContext())
        builder1.setTitle("" + title)
        builder1.setMessage("" + message)
        builder1.setCancelable(false)
        builder1.setPositiveButton(
            "OK"
        ) { dialog, id ->

            clearData(root)
            val intent = Intent(requireContext(), AllRechargeReportsActivity::class.java)
            requireContext().startActivity(intent)
            dialog.dismiss()
        }
        /*
        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });*/
        val alert11 = builder1.create()
        alert11.show()
    }

}