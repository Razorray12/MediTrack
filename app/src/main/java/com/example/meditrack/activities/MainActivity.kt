package com.example.meditrack.activities

import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.meditrack.R
import com.example.meditrack.fragments.AddPatientsFragment
import com.example.meditrack.fragments.InformationFragment
import com.example.meditrack.fragments.ProfileFragment
import com.example.meditrack.fragments.SearchFragment
import com.example.meditrack.fragments.SelectedPatientsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Objects

class MainActivity : AppCompatActivity() {
    private var isDoctor = false

    private var currentFragment: Fragment? = null
    private var searchFragment: SearchFragment? = null
    private lateinit var profileFragment: ProfileFragment
    private lateinit var informationFragment: InformationFragment
    private lateinit var addPatientFragment: AddPatientsFragment
    private var menu: Menu? = null
    private lateinit var selectedPatientsFragment: SelectedPatientsFragment
    private lateinit var searchView: SearchView
    private lateinit var nameFragment: TextView
    private lateinit var saveButton: AppCompatImageButton
    private lateinit var saveButton1: AppCompatImageButton
    private lateinit var saveButton2: AppCompatImageButton
    private lateinit var addedPatientsImageButton: AppCompatImageButton
    private lateinit var backButton: AppCompatImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val user = FirebaseAuth.getInstance().currentUser
        val userId = Objects.requireNonNull(user)!!.uid
        val doctorsRef = FirebaseDatabase.getInstance().getReference("users/doctors")

        doctorsRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    isDoctor = true
                }
                if (!isDoctor) {
                    addedPatientsImageButton.visibility = View.GONE
                }
                invalidateOptionsMenu()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)!!.title = ""

        nameFragment = toolbar.findViewById(R.id.name_fragment)

        searchView = findViewById(R.id.action_search)
        saveButton = findViewById(R.id.save_button)
        saveButton1 = findViewById(R.id.save_button1)
        saveButton2 = findViewById(R.id.save_button2)
        backButton = toolbar.findViewById(R.id.back_button)

        val hintColor = ContextCompat.getColor(this, R.color.search)
        searchView.setQueryHint(
            Html.fromHtml(
                "<font color = \"$hintColor\">" + resources.getString(
                    R.string.search
                ) + "</font>"
            )
        )

        val searchEditText =
            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(resources.getColor(R.color.handles))

        setupSearchView()

        searchFragment = SearchFragment()

        informationFragment = InformationFragment()

        searchFragment!!.setOnFragmentSwitchListener(object : SearchFragment.OnFragmentSwitchListener {
            override fun onSwitchToInformationFragment() {
                val menuItem = menu?.findItem(R.id.action_add)
                menuItem?.isVisible = false
                backButton.visibility = View.VISIBLE
                searchView.visibility = View.GONE
                nameFragment.text = "Информация"
                if (!informationFragment.isAdded) {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, informationFragment).commit()
                }

                supportFragmentManager.beginTransaction().hide(currentFragment!!)
                    .show(informationFragment).commit()

                currentFragment = informationFragment

                invalidateOptionsMenu()
            }
        })

        profileFragment = ProfileFragment()
        selectedPatientsFragment = SelectedPatientsFragment()
        addPatientFragment = AddPatientsFragment()

        supportFragmentManager.beginTransaction().add(
            R.id.fragment_container,
            searchFragment!!, "search_fragment"
        ).commit()

        currentFragment = searchFragment

        val patientsImageButton = findViewById<AppCompatImageButton>(R.id.patients)
        addedPatientsImageButton = findViewById(R.id.added_patients)
        val profileImageButton = findViewById<AppCompatImageButton>(R.id.profile)
        patientsImageButton.setColorFilter(ContextCompat.getColor(this, R.color.white))


        patientsImageButton.setOnClickListener {
            saveButton.setVisibility(View.GONE)
            saveButton1.setVisibility(View.GONE)
            patientsImageButton.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            addedPatientsImageButton.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.handles
                )
            )
            profileImageButton.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.handles
                )
            )
            nameFragment.text = "Пациенты"
            searchView.visibility = View.VISIBLE
            if (currentFragment !is SearchFragment) {
                supportFragmentManager.beginTransaction().hide(currentFragment!!).show(
                    searchFragment!!
                ).commit()
                currentFragment = searchFragment
                invalidateOptionsMenu()
            }
        }

        addedPatientsImageButton.setOnClickListener {
            saveButton.setVisibility(View.GONE)
            addedPatientsImageButton.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            patientsImageButton.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.handles
                )
            )
            profileImageButton.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.handles
                )
            )
            saveButton.setVisibility(View.GONE)
            searchView.visibility = View.GONE
            nameFragment.text = "Добавленные"
            if (currentFragment !is SelectedPatientsFragment) {
                if (selectedPatientsFragment.isAdded) {
                    supportFragmentManager.beginTransaction().hide(currentFragment!!)
                        .show(selectedPatientsFragment).commit()
                } else {
                    supportFragmentManager.beginTransaction().hide(currentFragment!!).add(
                        R.id.fragment_container,
                        selectedPatientsFragment,
                        "selected_patients_fragment"
                    ).commit()
                }
                currentFragment = selectedPatientsFragment
                invalidateOptionsMenu()
            }
        }

        profileImageButton.setOnClickListener {
            saveButton.setVisibility(View.GONE)
            showEditButton()
            profileImageButton.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
            patientsImageButton.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.handles
                )
            )
            addedPatientsImageButton.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.handles
                )
            )
            closeCloseEditButton()
            nameFragment.text = "Профиль"
            searchView.visibility = View.GONE

            if (currentFragment !is ProfileFragment) {
                if (profileFragment.isAdded) {
                    supportFragmentManager.beginTransaction().hide(currentFragment!!)
                        .show(profileFragment).commit()
                } else {
                    supportFragmentManager.beginTransaction().hide(currentFragment!!)
                        .add(R.id.fragment_container, profileFragment, "profile_fragment").commit()
                }
                currentFragment = profileFragment
                invalidateOptionsMenu()
            }
            if (profileFragment.isVisible) {
                profileFragment.onHideEditText()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuItemEdit = menu.findItem(R.id.action_edit)
        val menuItemAdd = menu.findItem(R.id.action_add)
        val menuItemClose = menu.findItem(R.id.action_close_edit)
        val menuItemClose1 = menu.findItem(R.id.action_close_edit1)

        if (currentFragment is SearchFragment) {
            menuItemAdd.setVisible(isDoctor)
            menuItemEdit.setVisible(false)
            menuItemClose.setVisible(false)
            menuItemClose1.setVisible(false)
        } else if (currentFragment is ProfileFragment) {
            menuItemAdd.setVisible(false)
            menuItemEdit.setVisible(true)
        } else if (currentFragment is SelectedPatientsFragment) {
            menuItemAdd.setVisible(false)
            menuItemAdd.setVisible(false)
            menuItemEdit.setVisible(false)
        } else if (currentFragment is AddPatientsFragment) {
            menuItemAdd.setVisible(false)
            menuItemClose1.setVisible(true)
            menuItemEdit.setVisible(false)
        } else if (currentFragment is InformationFragment) {
            menuItemAdd.setVisible(false)
            menuItemEdit.setVisible(true)
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_add) {
            searchView.visibility = View.GONE
            saveButton1.visibility = View.VISIBLE
            nameFragment.text = "Добавление"
            if (currentFragment !is AddPatientsFragment) {
                if (addPatientFragment.isAdded) {
                    supportFragmentManager.beginTransaction().hide(currentFragment!!)
                        .show(addPatientFragment).commit()
                } else {
                    supportFragmentManager.beginTransaction().hide(currentFragment!!)
                        .add(R.id.fragment_container, addPatientFragment, "add_patient_fragment")
                        .commit()
                }
                currentFragment = addPatientFragment
                invalidateOptionsMenu()
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupSearchView() {
        val searchView = findViewById<SearchView>(R.id.action_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val searchFragment =
                    supportFragmentManager.findFragmentByTag("search_fragment") as SearchFragment?

                searchFragment?.onQueryTextSubmit(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                onQueryTextSubmit(newText)
                return true
            }
        })
        searchView.setOnCloseListener {
            val searchFragment =
                supportFragmentManager.findFragmentByTag("search_fragment") as SearchFragment?
            searchFragment?.showAllPatients()
            false
        }
    }

    fun setToolbarSaveButtonListener(listener: View.OnClickListener?) {
        saveButton.setOnClickListener(listener)
    }

    fun setToolbarSaveButtonListener1(listener: View.OnClickListener?) {
        saveButton1.setOnClickListener(listener)
    }

    fun setToolbarSaveButtonListener2(listener: View.OnClickListener?) {
        saveButton2.setOnClickListener(listener)
    }

    fun setToolbarBackButtonListener(listener: View.OnClickListener?) {
        backButton.setOnClickListener(listener)
    }

    fun closeFragmentInformation() {
        if (currentFragment !is SearchFragment) {
            supportFragmentManager.beginTransaction().hide(currentFragment!!).show(searchFragment!!)
                .commit()
            currentFragment = searchFragment
            invalidateOptionsMenu()
        }
    }

    fun showSaveButton() {
        saveButton.visibility = View.VISIBLE
    }

    fun showBackButton() {
        backButton.visibility = View.VISIBLE
    }

    fun closeBackButton() {
        backButton.visibility = View.GONE
    }

    fun closeSaveButton() {
        saveButton.visibility = View.GONE
    }

    fun closeSaveButton1() {
        saveButton1.visibility = View.GONE
    }

    fun closeSaveButton2() {
        saveButton2.visibility = View.GONE
    }

    fun showSaveButton2() {
        saveButton2.visibility = View.VISIBLE
    }

    fun showCloseEditButton() {
        val item = menu!!.findItem(R.id.action_close_edit)
        item.setVisible(true)
    }

    fun closeCloseEditButton() {
        val item = menu!!.findItem(R.id.action_close_edit)
        item.setVisible(false)
    }

    fun closeFragmentAdd() {
        if (currentFragment !is SearchFragment) {
            supportFragmentManager.beginTransaction().hide(currentFragment!!).show(searchFragment!!)
                .commit()
            currentFragment = searchFragment
            invalidateOptionsMenu()
        }
    }

    fun showSearchView() {
        searchView.visibility = View.VISIBLE
    }

    fun showEditButton() {
        val item = menu!!.findItem(R.id.action_edit)
        item.setVisible(true)
    }

    fun setTextForSearch() {
        nameFragment.text = "Пациенты"
    }

    fun setText(text: String?) {
        nameFragment.text = text
    }
}
