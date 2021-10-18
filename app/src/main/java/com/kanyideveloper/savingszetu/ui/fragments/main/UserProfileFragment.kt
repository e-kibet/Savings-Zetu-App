package com.kanyideveloper.savingszetu.ui.fragments.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.RequestManager
import com.google.firebase.auth.FirebaseAuth
import com.kanyideveloper.savingszetu.R
import com.kanyideveloper.savingszetu.databinding.FragmentUserProfileBinding
import com.kanyideveloper.savingszetu.ui.activities.AuthActivity
import com.kanyideveloper.savingszetu.viewmodel.MainViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var binding: FragmentUserProfileBinding

    private val viewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController

    private lateinit var cropContent: ActivityResultLauncher<String>

    private val cropActivityResultContract = object :
        ActivityResultContract<String, Uri?>() {
        override fun createIntent(context: Context, input: String?): Intent {
            return CropImage.activity()
                .setAspectRatio(16,9)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent).uri
        }

    }

    private var currentImageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cropContent = registerForActivityResult(cropActivityResultContract){
            it?.let {
                viewModel.setCurImageUri(uri = it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        navController = findNavController()

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbarProfile.setupWithNavController(navController, appBarConfiguration)


        binding.textViewLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        binding.selectImage.setOnClickListener {
            cropContent.launch("image/*")
        }

        binding.button5.setOnClickListener {
            binding.selectImage.isVisible = true
        }

        return view
    }

    private fun subscribeToObservers(){
        viewModel.curImageUri.observe(viewLifecycleOwner, Observer {
            currentImageUri = it
            glide.load(currentImageUri).into(binding.imageViewProfile)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (R.id.saveProfile == item.itemId){
            viewModel.uploadProfileImage(currentImageUri)
            true
        }else
            super.onOptionsItemSelected(item)
    }
}