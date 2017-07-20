package com.example.suas;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zendesk.suas.Action;
import com.zendesk.suas.Component;
import com.zendesk.suas.Listener;
import com.zendesk.suas.LoggerMiddleWare;
import com.zendesk.suas.Notifiers;
import com.zendesk.suas.ReduxStore;
import com.zendesk.suas.Selector;
import com.zendesk.suas.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Component<TodoList, String> {

    Store store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView listView = findViewById(R.id.list);
        final TodoListAdapter todoListAdapter = new TodoListAdapter(new ArrayList<String>());
        listView.setAdapter(todoListAdapter);

        store = new ReduxStore.Builder(new TodoReducer())
                .withMiddleware(new LoggerMiddleWare("LOGGER 1"), new LoggerMiddleWare("LOGGER 2"))
                .withDefaultNotifier(Notifiers.EQUALS)
                .build();

        findViewById(R.id.add_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                store.dispatchAction(ActionFactory.addAction(UUID.randomUUID().toString()));
            }
        });

        findViewById(R.id.nothing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                store.dispatchAction(new Action("bla bla"));
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = todoListAdapter.getItem(i);
                store.dispatchAction(ActionFactory.deleteAction(item));
            }
        });

        store.addListener(TodoList.class, new Listener<TodoList>() {
            @Override
            public void update(TodoList oldState, TodoList newState) {
                todoListAdapter.update(newState.getItems());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        store.disconnect(this);
        System.out.println();
    }

    @Override
    protected void onStart() {
        store.connect(this, TodoList.class, Notifiers.DEFAULT);
        super.onStart();
    }

    @Override
    public void update(String s) {
        System.out.println("update update " + s);
    }

    @Override
    public Selector<TodoList, String> getSelector() {
        return new Selector<TodoList, String>() {
            @Override
            public String selectData(TodoList data) {
                return data.getItems().toString();
            }
        };
    }

    class TodoListAdapter extends BaseAdapter {

        final List<String> item;

        TodoListAdapter(List<String> item) {
            this.item = item;
        }

        void update(List<String> items) {
            item.clear();
            item.addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return item.size();
        }

        @Override
        public String getItem(int i) {
            return item.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            if(v == null) {
                v = LayoutInflater
                        .from(viewGroup.getContext())
                        .inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            }

            TextView txt = v.findViewById(android.R.id.text1);
            txt.setText(item.get(i));

            return v;
        }
    }

}
