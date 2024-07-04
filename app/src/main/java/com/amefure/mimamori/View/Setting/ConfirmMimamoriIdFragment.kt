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
import com.amefure.mimamori.View.Dialog.CustomNotifyDialogFragment
import com.amefure.mimamori.ViewModel.RootEnvironment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.launch

class ConfirmMimamoriIdFragment : Fragment() {

    private val rootEnvironment: RootEnvironment by viewModels()
    private var compositeDisposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_confirm_mimamori_id, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHeaderAction(view)

        var id: TextView = view.findViewById(R.id.miamori_id_label)
        var copyButton: Button = view.findViewById(R.id.copy_button)
        var shareButton: Button = view.findViewById(R.id.share_button)

        AppEnvironmentStore.instance.myAppUser.subscribeBy { user ->
            id.text = user.id
        }.addTo(compositeDisposable)

        copyButton.setOnClickListener {
            copyIdToClipboard(this.requireContext(), id.text.toString())
            showSuccessCopyIdDialog()
        }

        shareButton.setOnClickListener {
            shareUserId(id.text.toString())
        }
    }


    /**
     *  IDコピー成功ダイアログ表示
     */
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

        val leftButton: ImageButton = headerView.findViewById(R.id.left_button)
        leftButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rightButton: ImageButton = headerView.findViewById(R.id.right_button)
        rightButton.visibility = View.GONE
    }


    /** クリップボードにIDをコピーする */
    private fun copyIdToClipboard(context: Context, id: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Mimamori ID", id)
        clipboard.setPrimaryClip(clip)
    }

    /** 共有する */
    private fun shareUserId(id: String) {
        var msg = getString(R.string.onboarding3_mimamori_share_text, id)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, msg)
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }
}