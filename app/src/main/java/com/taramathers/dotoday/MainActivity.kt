package com.taramathers.dotoday

import android.os.Bundle


import kotlinx.android.synthetic.main.activity_main.*
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Context
import android.os.Handler
import androidx.core.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.item_view.*
import kotlinx.android.synthetic.main.item_view.view.*
import kotlin.math.roundToInt

/**
 * MainActivity
 */
class MainActivity : AppCompatActivity() {
    val listItems: MutableMap<Int, String> = HashMap()
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

            updatePercentDone()
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
     * Create a new checkbox item
     *
     * @param itemText Checkbox item text
     */
    private fun createItem(item: ItemData) {
        println("done_")


        listItems.put(item.id!!.toInt(), item.text)
        println(listItems)

       // var checkBox:CheckBox = CheckBox(this)
       // checkBox.setText(item.text)
       // checkBox.setTextSize(20f)
        //checkBox.isChecked = item.checked

        //val itemView = findViewById(R.id.itemView) as LinearLayout?
        val inflater:LayoutInflater = LayoutInflater.from(applicationContext)

        val itemView: View = inflater.inflate(R.layout.item_view, mainLayout, false)
        itemView!!.checkBox.text = item.text
        itemView!!.checkBox.isChecked = item.checked
        itemView.id = item.id!!.toInt()
        itemLayout.addView(itemView)


        itemView.checkBox.setOnClickListener( View.OnClickListener {
            updatePercentDone()
            var item =  ItemData(
                    itemView.id.toLong(),
                    itemView.checkBox.text.toString(),
                    itemView.checkBox.isChecked)
            updateItem(item);
        })

        updatePercentDone()
    }

    /**
     * Update the percentage done
     */
    fun updatePercentDone() {

        val linearLayout = findViewById(R.id.itemLayout) as LinearLayout

        var numChecked : Float = 0f
        val childCount = itemLayout.childCount

        for (i in 0 until childCount) {
            val view = itemLayout.getChildAt(i)
            val checkBox = view.checkBox as CheckBox
            if (checkBox.isChecked) {
                numChecked++
            }
        }

        //print( numChecked.toString() + " / " + listItems.size.toString())
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

            val items = mDb?.itemDataDao()?.getAll()
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

    private fun insertToDb(itemData: ItemData) : Int? {

        println("Inserting ${itemData.text}")

        var id: Long? = 0
        val task = Runnable {
             id = mDb?.itemDataDao()?.insert(itemData)
            //println("inserted $id")
        }
        mDbWorkerThread.postTask(task)

        return id!!.toInt()
    }

    override fun onDestroy() {
        ItemDatabase.destroyInstance()
        mDbWorkerThread.quit()
        super.onDestroy()
    }

    fun createItem(editText: EditText) {
        var text = editText.text.toString().trim()

        mainLayout.removeView(editText)

        var itemData = ItemData()
        itemData.text = text
        itemData.checked = false
        val id = insertToDb(itemData)
        itemData.id = id!!.toLong()

        createItem(itemData)

        editText.setId(id!!)

        updatePercentDone()
    }

    fun updateItem(item: ItemData) {
        val task = Runnable {
            val items = mDb?.itemDataDao()?.update(item)
        }
        mDbWorkerThread.postTask(task)
    }

}
