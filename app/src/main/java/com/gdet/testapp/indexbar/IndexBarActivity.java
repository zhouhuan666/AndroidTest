package com.gdet.testapp.indexbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gdet.testapp.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-03-15
 * 描述：
 */
public class IndexBarActivity extends AppCompatActivity {
    private ListView mListView;
    private IndexBar mIndexBar;
    private TextView mPreviewText;

    // 用一个简单的字符串列表替换 Cheeses
    private final List<String> dummyData = Arrays.asList(
            "Apple", "Banana", "Cherry", "Date", "Fig", "Grape",
            "Honeydew", "Indian Fig", "Jackfruit", "Kiwi", "Lemon", "Mango",
            "Nectarine", "Orange", "Papaya", "Quince", "Raspberry", "Strawberry",
            "Tomato", "Ugli Fruit", "Vanilla", "Watermelon", "Xigua", "Yellow Passion Fruit", "Zucchini"
    );
    private Map<String, Integer> mSections = new HashMap<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indexbar);

        final int length = dummyData.size();
        for (int i = 0; i < length; i++) {
            String alphabet = dummyData.get(i).substring(0, 1);
            if (!mSections.containsKey(alphabet)) {
                mSections.put(alphabet, i);
            }
        }

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, dummyData));
        mIndexBar = (IndexBar) findViewById(R.id.index_bar);
        mIndexBar.setSections(alphabets());
        mPreviewText = (TextView) findViewById(R.id.previewText);

        mIndexBar.setIndexBarFilter(new IndexBar.IIndexBarFilter() {
            @Override
            public void filterList(float sideIndex, int position, String previewText) {
                Integer selection = mSections.get(previewText);
                if (selection != null) {
                    mPreviewText.setVisibility(View.VISIBLE);
                    mPreviewText.setText(previewText);
                    mListView.setSelection(selection);
                } else {
                    mPreviewText.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String[] alphabets() {
        final int length = 26;
        final String[] alphabets = new String[length];
        char c = 'A';
        for (int i = 0; i < length; i++) {
            alphabets[i] = String.valueOf(c++);
        }
        return alphabets;
    }
}
