package com.example.budgy.ui.transaksi

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.budgy.R
import com.example.budgy.data.response.DeletePendapatanResponse
import com.example.budgy.data.response.DeletePengeluaranResponse
import com.example.budgy.data.response.PutDataPendapatan
import com.example.budgy.data.response.PutDataPengeluaran
import com.example.budgy.data.response.PutPendapatanResponse
import com.example.budgy.data.response.PutPengeluaranResponse
import com.example.budgy.data.retrofit.ApiConfig
import com.example.budgy.data.retrofit.ApiService
import com.example.budgy.databinding.FragmentEditTransaksiPendapatanBinding
import com.example.budgy.databinding.FragmentEditTransaksiPengeluaranBinding
import com.example.budgy.ui.home.HomeFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EditTransaksiPendapatanFragment : Fragment() {

    private var _binding: FragmentEditTransaksiPendapatanBinding? = null
    private val binding get() = _binding!!

    private val apiService: ApiService by lazy {
        ApiConfig.getApiService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTransaksiPendapatanBinding.inflate(inflater, container, false)

        binding.btnBack.setOnClickListener {
            val homeFragment = HomeFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .addToBackStack(null)
                .commit()
        }

        setupDatePicker()
        setupCategoryDropdown()
        loadExistingData()

        binding.btnSave.setOnClickListener {
            val id = arguments?.getInt("id") ?: -1
            handleSave(id)
        }

        binding.btnDelete.setOnClickListener {
            val id = arguments?.getInt("id") ?: -1
            handleDelete(id)
        }

        return binding.root
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }.time
                    binding.etDate.setText(sdf.format(selectedDate))
                }, year, month, day
            ).show()
        }
    }

    private fun setupCategoryDropdown() {
        val categories = resources.getStringArray(R.array.kategori_pendapatan)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        binding.spCategory.setAdapter(adapter)
        binding.spCategory.setOnClickListener {
            binding.spCategory.showDropDown()
        }
    }

    private fun loadExistingData() {
        val tanggal = arguments?.getString("tanggal") ?: ""
        val nominal = arguments?.getString("nominal") ?: "0"
        binding.etTotal.setText(nominal)
        binding.etDate.setText(tanggal)
    }



    private fun handleSave(id: Int) {
        Log.d("EditTransaksiPendapatan", "Memulai handleSave dengan ID: $id")

        val nominalText = binding.etTotal.text.toString()
        val kategoriText = binding.spCategory.text.toString()
        val tanggal = binding.etDate.text.toString()

        Log.d("EditTransaksiPendapatan", "Input nominalText: $nominalText")
        Log.d("EditTransaksiPendapatan", "Input kategoriText: $kategoriText")
        Log.d("EditTransaksiPendapatan", "Input tanggal: $tanggal")

        val nominal = nominalText.toIntOrNull()
        val categories = resources.getStringArray(R.array.kategori_pendapatan)
        val kategoriId = categories.indexOf(kategoriText)

        Log.d("EditTransaksiPendapatan", "Parsed nominal: $nominal")
        Log.d("EditTransaksiPendapatan", "Parsed kategoriId: $kategoriId")

        if (nominal != null && kategoriId >= 0 && tanggal.isNotEmpty()) {
            val putData = PutDataPendapatan(
                kategoriId = kategoriId,
                nominal = nominal,
                tanggal = tanggal
            )

            Log.d("EditTransaksiPendapatan", "Mengirim PutData: $putData")

            apiService.putPendapatan(id, putData).enqueue(object :
                Callback<PutPendapatanResponse> {
                override fun onResponse(
                    call: Call<PutPendapatanResponse>,
                    response: Response<PutPendapatanResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("EditTransaksiPendapatan", "Response sukses: ${response.body()}")
                        Toast.makeText(requireContext(), "Pendapatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Log.e("EditTransaksiPendapatan", "Response gagal: ${response.code()} - ${response.message()}")
                        Toast.makeText(requireContext(), "Gagal memperbarui pendapatan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PutPendapatanResponse>, t: Throwable) {
                    Log.e("EditTransaksiPendapatan", "Error API: ${t.message}")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.w("EditTransaksiPendapatan", "Input tidak valid: nominal=$nominal, kategoriId=$kategoriId, tanggal=$tanggal")
            Toast.makeText(requireContext(), "Semua field harus diisi dengan benar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDelete(id: Int) {
        apiService.deletePendapatan(id).enqueue(object : Callback<DeletePendapatanResponse> {
            override fun onResponse(
                call: Call<DeletePendapatanResponse>,
                response: Response<DeletePendapatanResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Pendapatan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus pendapatan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeletePendapatanResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}