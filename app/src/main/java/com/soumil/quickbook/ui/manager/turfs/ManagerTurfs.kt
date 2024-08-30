package com.soumil.quickbook.ui.manager.turfs

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.soumil.quickbook.PermissionManager
import com.soumil.quickbook.R
import com.soumil.quickbook.databinding.FragmentManagerMyTurfsBinding
import com.soumil.quickbook.managerTurf.AddTurf

class ManagerTurfs : Fragment() {

    private lateinit var binding: FragmentManagerMyTurfsBinding
    private lateinit var managerTurfViewModel: ManagerMyTurfsViewModel
    private lateinit var adapter: TurfAdapter

    private var permissionManager: PermissionManager? = null
    private val PICK_IMAGE_REQUEST_CODE = 100

    private val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
    }
    else{
        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentManagerMyTurfsBinding.inflate(inflater, container, false)
        managerTurfViewModel = ViewModelProvider(this).get(ManagerMyTurfsViewModel::class.java)
        binding.viewModel = managerTurfViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        managerTurfViewModel.managerName.observe(viewLifecycleOwner){ name ->
            binding.nameText.text = name
        }

        managerTurfViewModel.cityName.observe(viewLifecycleOwner){ cityName ->
            binding.locationText.text = cityName
        }
        managerTurfViewModel.getManagerName()
        managerTurfViewModel.getCityName()

        setupRecyclerView(binding.recyclerView)

        managerTurfViewModel.loading.observe(viewLifecycleOwner){ isLoading ->
            if (isLoading){
                binding.progressBar.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }
            else{
                binding.progressBar.visibility = View.GONE
                if (managerTurfViewModel.turfs.value.isNullOrEmpty()){
                    binding.noTurfMessage.visibility = View.VISIBLE
                    binding.noTurfMessageText.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
                else{
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        }

        managerTurfViewModel.turfs.observe(viewLifecycleOwner){ turfs ->
            adapter.submitList(turfs)
            if (turfs.isEmpty()){
                binding.noTurfMessage.visibility = View.VISIBLE
                binding.noTurfMessageText.visibility = View.VISIBLE
            }
            else{
                binding.noTurfMessage.visibility = View.GONE
                binding.noTurfMessageText.visibility = View.GONE
            }
        }

        return binding.root

    }

    private fun setupRecyclerView(recyclerView: RecyclerView){
        adapter = TurfAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val snapHelper = LinearSnapHelper() //or PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addTurfBtn: Button = view.findViewById(R.id.addTurfButton)

        addTurfBtn.setOnClickListener {
            val intent = Intent(requireContext(), AddTurf::class.java)
            startActivity(intent)
        }
    }
}