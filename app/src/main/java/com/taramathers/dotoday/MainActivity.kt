package com.taramathers.dotoday

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.content.Context
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import kotlin.math.roundToInt

/**
 * MainActivity
 */
class MainActivity : AppCompatActivity() {
    val listItems: MutableMap<Long, String> = HashMap()
    var lastId = 0

    var percentDone = 0

    private var mDb: ItemDatabase? = null
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    //val editText by lazy { TextView(this) }
    //val editText by lazy { TextView(this) }

    /**
     * onCreate function
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()

        mDb = ItemDatabase.getInstance(this)

        fetchDataFromDb()

        fab.setOnClickListener {

            val editText = EditText(this)
            mainLayout.addView(editText)

            editText.apply {
                hint = "Enter something"
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.setMargins(8, 8, 8, 8)
                layoutParams = lp
                setBackgroundColor(ContextCompat.getColor(this@MainActivity,
                        android.R.color.holo_blue_light))
                imeOptions = EditorInfo.IME_ACTION_DONE
                textSize = 20f
                inputType = InputType.TYPE_CLASS_TEXT

            }

            editText.setOnEditorActionListener { v, actionId, event ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        println("%%%%%%%%%%%%%%%%%%%%%%%%")
                        createItem(editText)
                        true
                    }
                    else -> false
                }
            }

        }
    }

    /**
     * onCreateOptionsMenu
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * onOptionsItemSelected
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Create a toast
     */
    fun Context.toast(context: Context = applicationContext, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    /**
     * TextWatcher
     */
    fun EditText.smartTextWatcher(on: (String) -> Unit, after: (String) -> Unit, before: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                after.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                before.invoke(s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                on.invoke(s.toString())
            }
        })
    }

    /**
     * Create a new checkbox item
     *
     * @param itemText Checkbox item text
     */
    fun createItem(item: ItemData) {
        println("done_")


        listItems.put(item.id!!, item.text)
        print(listItems)

        var checkBox:CheckBox = CheckBox(this)
        checkBox.setText(item.text)
        checkBox.setTextSize(20f)
        checkBox.isChecked = item.checked

        //val linearLayout = findViewById(R.id.mainLayout) as LinearLayout
        itemLayout.addView(checkBox)

        checkBox.setOnClickListener( View.OnClickListener {
            updatePercentDone()
        })
    }

    /**
     * Update the percentage done
     */
    fun updatePercentDone() {

        val linearLayout = findViewById(R.id.itemLayout) as LinearLayout

        var numChecked : Float = 0f
        val childCount = itemLayout.childCount

        println("CHILDREN " + childCount.toString())
        for (i in 0 until childCount) {
            val view = itemLayout.getChildAt(i)
            if (view is CheckBox) {
                val checkBox = view as CheckBox
                if (checkBox.isChecked) {
                    numChecked++
                }
            }
        }

        print( numChecked.toString() + " / " + listItems.size.toString())
        var percent : Float = numChecked / listItems.size.toFloat()
        percent *= 100
        var percentInt = percent.roundToInt()

        totalText.text = percentInt.toString() + "% Complete"

    }

    private fun bindDataWithUi(items: List<ItemData>) {

        itemLayout.removeAllViews()

        for (item in items) {
            createItem(item)
        }
    }

    private fun fetchDataFromDb() {
        val task = Runnable {
            val items =
                    mDb?.itemDataDao()?.getAll()
            mUiHandler.post({
                if (items == null || items?.size == 0) {
                    println("No data in cache..!!")
                } else {

                    println("GOT " + items.size + " items from DB")
                    bindDataWithUi(items = items)
                }
            })
        }
        mDbWorkerThread.postTask(task)
    }

    private fun insertToDb(itemData: ItemData) : Long? {
        var id : Long? = 0
        val task = Runnable {
            id = mDb?.itemDataDao()?.insert(itemData)
        }
        mDbWorkerThread.postTask(task)

        return id
    }

    override fun onDestroy() {
        ItemDatabase.destroyInstance()
        mDbWorkerThread.quit()
        super.onDestroy()
    }

    fun createItem(editText: EditText) {
        var text = editText.text.toString().trim()

        mainLayout.removeView(editText)

        var itemData = ItemData(null, text, false)
        val id = insertToDb(itemData)
        itemData.id = id

        createItem(itemData)

        editText.setId(id?.toInt()!!)

        updatePercentDone()
    }


}
