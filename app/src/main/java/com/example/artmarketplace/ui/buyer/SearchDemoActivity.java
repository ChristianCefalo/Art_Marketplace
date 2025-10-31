package com.example.artmarketplace.ui.buyer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.artmarketplace.R;
import com.google.android.material.appbar.MaterialToolbar;

public class SearchDemoActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ArtworkAdapter adapter;

    // Demo data to filter
    private final List<String> all = new ArrayList<>(Arrays.asList(
            "Sunset Vista", "Blue Silence", "Golden Field", "Neon City",
            "Ocean Breath", "Forest Light", "Crimson Night", "Emerald Path",
            "Marble Sky", "Silver Lines", "Amber Drift", "Ivory Waves",
            "Shadow Bloom", "Velvet Dawn", "Scarlet Route", "Indigo Dream",
            "Aurora Melt", "Pastel Storm", "Copper Horizon", "Prism Gate"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_demo);

        // *** This is the toolbar hookup you asked about ***
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // (You can remove the next line if you want; the toolbar shows the title)
        setTitle("Search Demo");

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArtworkAdapter();
        recycler.setAdapter(adapter);

        // initial list
        adapter.submit(new ArrayList<>(all));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_demo, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) item.getActionView();
        if (sv != null) {
            sv.setQueryHint("Search artworks...");
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String q) { filter(q); return true; }
                @Override public boolean onQueryTextChange(String q) { filter(q); return true; }
            });
        }
        return true;
    }

    private void filter(String q) {
        if (q == null || q.trim().isEmpty()) {
            adapter.submit(new ArrayList<>(all));
            return;
        }
        String s = q.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String title : all) {
            if (title.toLowerCase().contains(s)) filtered.add(title);
        }
        adapter.submit(filtered);
    }

    // ---- tiny adapter (1 row = title text) ----
    static class ArtworkAdapter extends RecyclerView.Adapter<ArtworkAdapter.VH> {
        private final List<String> data = new ArrayList<>();
        void submit(List<String> items) { data.clear(); data.addAll(items); notifyDataSetChanged(); }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_title_row, parent, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.title.setText(data.get(pos)); }
        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView title;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
            }
        }
    }
}