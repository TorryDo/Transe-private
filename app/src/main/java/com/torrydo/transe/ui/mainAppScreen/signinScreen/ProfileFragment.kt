package com.torrydo.transe.ui.mainAppScreen.signinScreen

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.torrydo.transe.R
import com.torrydo.transe.databinding.FragmentProfileBinding
import com.torrydo.transe.ui.base.BaseFragment
import com.torrydo.transe.ui.mainAppScreen.MainActivity
import com.torrydo.transe.utils.CONSTANT
import com.torrydo.transe.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Named

@AndroidEntryPoint
@Named(CONSTANT.fragmentModule)
class ProfileFragment : BaseFragment<ProfileViewModel, FragmentProfileBinding>() {

    private val TAG = "_TAG_SignInFragment"

    private val mProfileVM: ProfileViewModel by viewModels()
    override fun getViewModelClass() = mProfileVM
    override fun getViewBinding() = FragmentProfileBinding.inflate(layoutInflater)

    override fun configOnCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        setup()
    }

    private fun setup() {
        binding.signinButton.setOnClickListener { _ ->
            googleSignInClient?.let {
                activityResult.launch(it.signInIntent)
            }
        }

        binding.signOutButton.setOnClickListener {
            if(auth.currentUser != null){
                auth.signOut()
                googleSignInClient?.signOut()
                popToBackStack()
            }else{
                Utils.showShortToast(requireContext(), "not Signed In")
            }
        }

        googleSignInClient = GoogleSignIn.getClient(
            requireContext(), GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(requireContext().getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

    }

    // có xung đột giữa viewmodel và registerForActivityResult chua biet cach fix, nên tạm thời để ntn

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null

    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!, requireActivity())
                } catch (e: ApiException) {
                    Log.e(TAG, "Google sign in failed", e)
                }

            }
        }

    private fun firebaseAuthWithGoogle(idToken: String, activity: Activity) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
//                    val user = auth.currentUser
                    popToBackStack()
                } else {
                    Log.e(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun popToBackStack() {
        if(requireActivity() is MainActivity){
            requireActivity().findNavController(R.id.mainFragmentContainerView).popBackStack()
        }
    }


}