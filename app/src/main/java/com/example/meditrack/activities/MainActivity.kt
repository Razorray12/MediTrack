package com.example.meditrack.activities

import android.annotation.SuppressLint
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
import com.example.meditrack.fragments.AIFragment
import com.example.meditrack.fragments.AddPatientsFragment
import com.example.meditrack.fragments.ChatFragment
import com.example.meditrack.fragments.InformationFragment
import com.example.meditrack.fragments.ProfileFragment
import com.example.meditrack.fragments.SearchFragment
import com.example.meditrack.fragments.SelectedPatientsFragment
import java.util.Objects

class MainActivity : AppCompatActivity() {
    private var isDoctor = false

    private var currentFragment: Fragment? = null

    private var searchFragment: SearchFragment? = null
    private lateinit var profileFragment: ProfileFragment
    private lateinit var informationFragment: InformationFragment
    private lateinit var addPatientFragment: AddPatientsFragment
    private lateinit var aiFragment: AIFragment
    private lateinit var selectedPatientsFragment: SelectedPatientsFragment
    private lateinit var chatFragment: ChatFragment

    private var menu: Menu? = null
    private lateinit var saveButton: AppCompatImageButton
    private lateinit var saveButton1: AppCompatImageButton
    private lateinit var saveButton2: AppCompatImageButton
    private lateinit var backButton: AppCompatImageButton

    private lateinit var patientsImageButton: AppCompatImageButton
    private lateinit var addedPatientsImageButton: AppCompatImageButton
    private lateinit var profileImageButton: AppCompatImageButton
    private lateinit var aiImageButton: AppCompatImageButton

    private lateinit var searchView: SearchView
    private lateinit var searchView1: SearchView
    private lateinit var nameFragment: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        Objects.requireNonNull(supportActionBar)!!.title = ""
        nameFragment = toolbar.findViewById(R.id.name_fragment)

        saveButton = findViewById(R.id.save_button)
        saveButton1 = findViewById(R.id.save_button1)
        saveButton2 = findViewById(R.id.save_button2)
        backButton = toolbar.findViewById(R.id.back_button)

        searchView = findViewById(R.id.action_search)
        searchView.maxWidth = Int.MAX_VALUE
        searchView1 = findViewById(R.id.ai_search1)
        searchView1.maxWidth = Int.MAX_VALUE
        val hintColor = ContextCompat.getColor(this, R.color.search)
        searchView.setQueryHint(Html.fromHtml("<font color = \"$hintColor\">" + getString(R.string.search) + "</font>"))
        searchView1.setQueryHint(Html.fromHtml("<font color = \"$hintColor\">" + getString(R.string.search) + "</font>"))

        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, R.color.handles))

        val searchEditText1 = searchView1.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText1.setTextColor(ContextCompat.getColor(this, R.color.handles))

        setupSearchView()
        setupSearchView1()

        patientsImageButton = findViewById(R.id.patients)
        addedPatientsImageButton = findViewById(R.id.added_patients)
        profileImageButton = findViewById(R.id.profile)
        aiImageButton = findViewById(R.id.ai_search)

        if (savedInstanceState == null) {
            searchFragment = SearchFragment()
            informationFragment = InformationFragment()
            aiFragment = AIFragment()
            profileFragment = ProfileFragment()
            selectedPatientsFragment = SelectedPatientsFragment()
            addPatientFragment = AddPatientsFragment()
            chatFragment = ChatFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, searchFragment!!, "search_fragment")
                .commit()
            currentFragment = searchFragment

        } else {
            searchFragment = supportFragmentManager.findFragmentByTag("search_fragment") as? SearchFragment
            if (searchFragment == null) {
                searchFragment = SearchFragment()
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, searchFragment!!, "search_fragment")
                    .commit()
            }
            informationFragment = supportFragmentManager.findFragmentByTag("information_fragment") as? InformationFragment ?: InformationFragment()
            aiFragment = supportFragmentManager.findFragmentByTag("ai_fragment") as? AIFragment ?: AIFragment()
            profileFragment = supportFragmentManager.findFragmentByTag("profile_fragment") as? ProfileFragment ?: ProfileFragment()
            selectedPatientsFragment = supportFragmentManager.findFragmentByTag("selected_patients_fragment") as? SelectedPatientsFragment ?: SelectedPatientsFragment()
            addPatientFragment = supportFragmentManager.findFragmentByTag("add_patient_fragment") as? AddPatientsFragment ?: AddPatientsFragment()

            val currentFragmentTag = savedInstanceState.getString("current_fragment_tag")
            currentFragment = if (currentFragmentTag != null)
                supportFragmentManager.findFragmentByTag(currentFragmentTag)
            else searchFragment
        }

        searchFragment!!.setOnFragmentSwitchListener(object : SearchFragment.OnFragmentSwitchListener {
            override fun onSwitchToInformationFragment() {
                val menuItem = menu?.findItem(R.id.action_add)
                menuItem?.isVisible = false
                backButton.visibility = View.VISIBLE
                searchView.visibility = View.GONE
                nameFragment.text = "Информация"
                if (!informationFragment.isAdded) {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, informationFragment, "information_fragment")
                        .commit()
                }
                supportFragmentManager.beginTransaction()
                    .hide(currentFragment!!)
                    .show(informationFragment)
                    .commit()
                currentFragment = informationFragment
                invalidateOptionsMenu()
            }
        })

        selectedPatientsFragment.setOnFragmentSwitchListener(object : SelectedPatientsFragment.OnFragmentSwitchListener {
            override fun onSwitchToInformationFragment() {
                val menuItem = menu?.findItem(R.id.action_add)
                menuItem?.isVisible = false
                backButton.visibility = View.VISIBLE
                searchView.visibility = View.GONE
                nameFragment.text = "Информация"
                if (!informationFragment.isAdded) {
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fragment_container, informationFragment, "information_fragment")
                        .commit()
                }
                supportFragmentManager.beginTransaction()
                    .hide(currentFragment!!)
                    .show(informationFragment)
                    .commit()
                currentFragment = informationFragment
                invalidateOptionsMenu()
            }
        })

        if (currentFragment == null) {
            currentFragment = searchFragment
        }

        patientsImageButton.setColorFilter(ContextCompat.getColor(this, R.color.white))

        patientsImageButton.setOnClickListener {
            backButton.visibility = View.GONE
            saveButton.visibility = View.GONE
            saveButton1.visibility = View.GONE
            saveButton2.visibility = View.GONE

            nameFragment.text = "Пациенты"
            searchView.visibility = View.VISIBLE
            searchView1.visibility = View.GONE

            if (currentFragment !is SearchFragment) {
                supportFragmentManager.beginTransaction()
                    .hide(currentFragment!!)
                    .show(searchFragment!!)
                    .commit()
                currentFragment = searchFragment
                invalidateOptionsMenu()
            }

            applyActiveButtonColors(currentFragment)
        }

        addedPatientsImageButton.setOnClickListener {
            backButton.visibility = View.GONE
            saveButton.visibility = View.GONE

            saveButton1.visibility = View.GONE
            saveButton2.visibility = View.GONE
            searchView.visibility = View.GONE
            searchView1.visibility = View.GONE
            nameFragment.text = "Добавленные"

            if (currentFragment !is SelectedPatientsFragment) {
                if (selectedPatientsFragment.isAdded) {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment!!)
                        .show(selectedPatientsFragment)
                        .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment!!)
                        .add(R.id.fragment_container, selectedPatientsFragment, "selected_patients_fragment")
                        .commit()
                }
                currentFragment = selectedPatientsFragment
                invalidateOptionsMenu()
            }

            applyActiveButtonColors(currentFragment)
        }

        profileImageButton.setOnClickListener {
            saveButton.visibility = View.GONE
            saveButton2.visibility = View.GONE
            backButton.visibility = View.GONE
            showEditButton()

            closeCloseEditButton()
            nameFragment.text = "Профиль"
            searchView.visibility = View.GONE
            searchView1.visibility = View.GONE

            if (currentFragment !is ProfileFragment) {
                if (profileFragment.isAdded) {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment!!)
                        .show(profileFragment)
                        .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment!!)
                        .add(R.id.fragment_container, profileFragment, "profile_fragment")
                        .commit()
                }
                currentFragment = profileFragment
                invalidateOptionsMenu()
            }
            if (profileFragment.isVisible) {
                profileFragment.onHideEditText()
            }

            applyActiveButtonColors(currentFragment)
        }

        aiImageButton.setOnClickListener {
            saveButton1.visibility = View.GONE
            saveButton2.visibility = View.GONE
            backButton.visibility = View.GONE

            saveButton.visibility = View.GONE
            searchView.visibility = View.GONE
            searchView1.visibility = View.VISIBLE
            nameFragment.text = "Справочник"

            if (currentFragment !is AIFragment) {
                if (aiFragment.isAdded) {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment!!)
                        .show(aiFragment)
                        .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment!!)
                        .add(R.id.fragment_container, aiFragment, "ai_fragment")
                        .commit()
                }
                currentFragment = aiFragment
                invalidateOptionsMenu()
            }
            applyActiveButtonColors(currentFragment)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuItemEdit = menu.findItem(R.id.action_edit)
        val menuItemAdd = menu.findItem(R.id.action_add)
        val menuItemClose = menu.findItem(R.id.action_close_edit)
        val menuItemClose1 = menu.findItem(R.id.action_close_edit1)

        when (currentFragment) {
            is SearchFragment -> {
                menuItemAdd.isVisible = isDoctor
                menuItemEdit.isVisible = false
                menuItemClose.isVisible = false
                menuItemClose1.isVisible = false
            }
            is ProfileFragment -> {
                menuItemAdd.isVisible = false
                menuItemEdit.isVisible = true
                menuItemClose.isVisible = false
                menuItemClose1.isVisible = false
            }
            is SelectedPatientsFragment -> {
                menuItemAdd.isVisible = false
                menuItemEdit.isVisible = false
                menuItemClose.isVisible = false
                menuItemClose1.isVisible = false
            }
            is AddPatientsFragment -> {
                menuItemAdd.isVisible = false
                menuItemClose1.isVisible = true
                menuItemEdit.isVisible = false
                menuItemClose.isVisible = false
            }
            is InformationFragment -> {
                menuItemAdd.isVisible = false
                menuItemEdit.isVisible = true
                menuItemClose.isVisible = false
                menuItemClose1.isVisible = false
            }
            is AIFragment -> {
                menuItemAdd.isVisible = false
                menuItemEdit.isVisible = false
                menuItemClose1.isVisible = false
                menuItemClose.isVisible = false
            }
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
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment!!)
                        .show(addPatientFragment)
                        .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                        .hide(currentFragment!!)
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
                val searchFragment = supportFragmentManager.findFragmentByTag("search_fragment") as? SearchFragment
                searchFragment?.onQueryTextSubmit(query)
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {
                onQueryTextSubmit(newText)
                return true
            }
        })
        searchView.setOnCloseListener {
            val searchFragment = supportFragmentManager.findFragmentByTag("search_fragment") as? SearchFragment
            searchFragment?.showAllPatients()
            false
        }
    }

    private fun setupSearchView1() {
        val searchView1 = findViewById<SearchView>(R.id.ai_search1)
        searchView1.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val aiFragment = supportFragmentManager.findFragmentByTag("ai_fragment") as? AIFragment
                aiFragment?.onQueryTextSubmit(query)
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
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
            supportFragmentManager.beginTransaction()
                .hide(currentFragment!!)
                .show(searchFragment!!)
                .commit()
            currentFragment = searchFragment
            invalidateOptionsMenu()
        }
    }
    fun closeFragmentAdd() {
        if (currentFragment !is SearchFragment) {
            supportFragmentManager.beginTransaction()
                .hide(currentFragment!!)
                .show(searchFragment!!)
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
        val item = menu?.findItem(R.id.action_close_edit)
        item?.isVisible = true
    }
    fun closeCloseEditButton() {
        val item = menu?.findItem(R.id.action_close_edit)
        item?.isVisible = false
    }
    fun showSearchView() {
        searchView.visibility = View.VISIBLE
    }
    fun showEditButton() {
        val item = menu?.findItem(R.id.action_edit)
        item?.isVisible = true
    }
    fun setTextForSearch() {
        nameFragment.text = "Пациенты"
    }

    private fun applyActiveButtonColors(current: Fragment?) {
        val white = ContextCompat.getColor(this, R.color.white)
        val gray = ContextCompat.getColor(this, R.color.handles)
        patientsImageButton.setColorFilter(gray)
        addedPatientsImageButton.setColorFilter(gray)
        profileImageButton.setColorFilter(gray)
        aiImageButton.setColorFilter(gray)

        when (current) {
            is SearchFragment -> patientsImageButton.setColorFilter(white)
            is SelectedPatientsFragment -> addedPatientsImageButton.setColorFilter(white)
            is ProfileFragment -> profileImageButton.setColorFilter(white)
            is AIFragment -> aiImageButton.setColorFilter(white)
            else -> patientsImageButton.setColorFilter(white)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentTag = currentFragment?.tag ?: ""
        outState.putString("current_fragment_tag", currentTag)
        outState.putString("search_query", searchView.query.toString())
        outState.putInt("search_view_visibility", searchView.visibility)
        outState.putInt("search_view1_visibility", searchView1.visibility)
        outState.putInt("back_button_visibility", backButton.visibility)
        outState.putInt("save_button_visibility", saveButton.visibility)
        outState.putInt("save_button1_visibility", saveButton1.visibility)
        outState.putInt("save_button2_visibility", saveButton2.visibility)
        outState.putInt("added_patients_visibility", addedPatientsImageButton.visibility)
        outState.putString("name_fragment_text", nameFragment.text.toString())
        outState.putInt("patients_button_visibility", patientsImageButton.visibility)
        outState.putInt("profile_button_visibility", profileImageButton.visibility)
        outState.putInt("ai_button_visibility", aiImageButton.visibility)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedQuery = savedInstanceState.getString("search_query", "")
        searchView.setQuery(savedQuery, false)
        searchView.visibility = savedInstanceState.getInt("search_view_visibility", View.VISIBLE)
        searchView1.visibility = savedInstanceState.getInt("search_view1_visibility", View.GONE)
        backButton.visibility = savedInstanceState.getInt("back_button_visibility", View.GONE)
        saveButton.visibility = savedInstanceState.getInt("save_button_visibility", View.GONE)
        saveButton1.visibility = savedInstanceState.getInt("save_button1_visibility", View.GONE)
        saveButton2.visibility = savedInstanceState.getInt("save_button2_visibility", View.GONE)
        addedPatientsImageButton.visibility = savedInstanceState.getInt("added_patients_visibility", View.VISIBLE)
        nameFragment.text = savedInstanceState.getString("name_fragment_text", "Пациенты")

        patientsImageButton.visibility = savedInstanceState.getInt("patients_button_visibility", View.VISIBLE)
        profileImageButton.visibility = savedInstanceState.getInt("profile_button_visibility", View.VISIBLE)
        aiImageButton.visibility = savedInstanceState.getInt("ai_button_visibility", View.VISIBLE)

        applyActiveButtonColors(currentFragment)
    }
}
