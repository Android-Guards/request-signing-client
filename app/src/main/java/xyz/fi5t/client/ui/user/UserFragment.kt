package xyz.fi5t.client.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_user.*
import xyz.fi5t.client.R
import xyz.fi5t.client.ui.login.LoginFragment

@AndroidEntryPoint
class UserFragment : Fragment() {
    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.username.observe(viewLifecycleOwner) {
            username.text = it
        }

        viewModel.showLogin.observe(viewLifecycleOwner) {
            if (it) {
                activity?.supportFragmentManager?.commit {
                    replace(R.id.container, LoginFragment())
                }
            }
        }

        logout_button.setOnClickListener {
            viewModel.logout()
        }

    }
}
