package com.amefure.mimamori.View.Setting

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.amefure.mimamori.R
import com.amefure.mimamori.Repository.AppEnvironmentStore
import com.amefure.mimamori.Utility.ShareTextUtility
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.ViewModel.RootEnvironment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.launch

class ConfirmMimamoriIdFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_confirm_mimamori_id, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)

        val id: TextView = view.findViewById(R.id.miamori_id_label)
        val copyButton: Button = view.findViewById(R.id.copy_button)
        val shareButton: Button = view.findViewById(R.id.share_button)

        id.text = rootEnvironment.getSignInUserId()

        copyButton.setOnClickListener {
            ShareTextUtility.copyIdToClipboard(this.requireContext(), id.text.toString())
            showSuccessCopyIdDialog()
        }

        shareButton.setOnClickListener {
            ShareTextUtility.shareUserId(id.text.toString(), this.requireContext())
        }
    }

    /** IDコピー成功ダイアログ表示 */
    private fun showSuccessCopyIdDialog() {
        val dialog = CustomNotifyDialogFragment.newInstance(
            title = getString(R.string.dialog_title_notice),
            msg = getString(R.string.dialog_success_copy),
            showPositive = true,
            showNegative = false
        )
        dialog.showNow(parentFragmentManager, "SuccessCopyIdDialog")
    }

    /**
     * ヘッダーボタンセットアップ
     * [LeftButton]：backButton
     * [RightButton]：非表示(GONE)
     */
    private fun setUpHeaderAction(view: View) {
        val headerView: ConstraintLayout = view.findViewById(R.id.include_header)

        val isMamorare = rootEnvironment.getIsMamorare()
        val headerTitle: TextView = view.findViewById(R.id.header_title)
        headerTitle.text = if (isMamorare) getString(R.string.mamorare) else getString(R.string.mimamori)

        val leftButton: ImageButton = headerView.findViewById(R.id.left_button)
        leftButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rightButton: ImageButton = headerView.findViewById(R.id.right_button)
        rightButton.visibility = View.GONE
    }

}