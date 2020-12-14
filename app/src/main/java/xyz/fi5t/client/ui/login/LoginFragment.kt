package xyz.fi5t.client.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_login.*
import xyz.fi5t.client.R
import xyz.fi5t.client.ui.user.UserFragment


@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loading.visibility = if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.showUser.observe(viewLifecycleOwner) {
            if (it) {
                activity?.supportFragmentManager?.commit {
                    replace(R.id.container, UserFragment())
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, "$message", Toast.LENGTH_LONG).show()
        }

        sign_in.setOnClickListener {
            viewModel.signIn(username.text.toString(), password.text.toString())
        }
    }
}
