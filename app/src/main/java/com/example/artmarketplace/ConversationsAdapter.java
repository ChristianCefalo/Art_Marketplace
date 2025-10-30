package com.example.artmarketplace;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.VH> {

    interface OnOpen   { void open(@NonNull String convId); }
    interface OnRename{ void rename(@NonNull DocumentSnapshot doc, @NonNull String newTitle); }
    interface OnDelete{ void delete(@NonNull DocumentSnapshot doc); }

    private List<DocumentSnapshot> data;
    private final OnOpen onOpen;
    private final OnRename onRename;
    private final OnDelete onDelete;

    ConversationsAdapter(List<DocumentSnapshot> d,
                         OnOpen o,
                         OnRename r,
                         OnDelete del) {
        data = (d == null) ? new ArrayList<>() : d;
        onOpen = o;
        onRename = r;
        onDelete = del;
    }

    void submit(List<DocumentSnapshot> d) {
        data = (d == null) ? new ArrayList<>() : d;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, snippet;
        ImageButton overflow;
        VH(@NonNull View itemView) {
            super(itemView);
            title    = itemView.findViewById(R.id.tvTitle);
            snippet  = itemView.findViewById(R.id.tvSnippet);
            overflow = itemView.findViewById(R.id.btnOverflow);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final DocumentSnapshot doc = data.get(position);

        final String rawTitle  = doc.getString("title");
        final String safeTitle = (rawTitle == null || rawTitle.trim().isEmpty()) ? "Untitled" : rawTitle.trim();
        final String last      = doc.getString("lastMessage");
        final String safeLast  = (last == null) ? "" : last;

        h.title.setText(safeTitle);
        h.snippet.setText(safeLast);

        // Open chat on row tap
        h.itemView.setOnClickListener(v -> onOpen.open(doc.getId()));

        // Overflow → menu (Rename/Delete), anchored to the title (higher in the row)
        if (h.overflow != null) {
            h.overflow.setOnClickListener(v -> showMenu(v.getContext(), h.title, doc));
        }
    }

    private void showMenu(@NonNull Context c, @NonNull View anchor, @NonNull DocumentSnapshot doc) {
        PopupMenu m = new PopupMenu(c, anchor, Gravity.TOP | Gravity.END);
        final int ID_RENAME = 1;
        final int ID_DELETE = 2;

        m.getMenu().add(0, ID_RENAME, 0, "Rename");
        m.getMenu().add(0, ID_DELETE, 1, "Delete");

        m.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == ID_DELETE) {
                onDelete.delete(doc);
                return true;
            }
            if (id == ID_RENAME) {
                showRenamePopup(c, anchor, doc);
                return true;
            }
            return false;
        });
        m.show();
    }

    /** Rename as an anchored PopupWindow (no dialog jump/flicker). */
    private void showRenamePopup(@NonNull Context c, @NonNull View anchor, @NonNull DocumentSnapshot doc) {
        // Build content view
        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(c, 16);
        root.setPadding(pad, pad, pad, pad);
        root.setBackground(new ColorDrawable(Color.WHITE));
        root.setElevation(dp(c, 4));

        TextView title = new TextView(c);
        title.setText("Rename chat");
        title.setTextSize(16);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        root.addView(title);

        EditText input = new EditText(c);
        String current = doc.getString("title");
        input.setText(current == null ? "" : current);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ip.topMargin = dp(c, 8);
        root.addView(input, ip);

        LinearLayout actions = new LinearLayout(c);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.END);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ap.topMargin = dp(c, 12);

        TextView cancel = new TextView(c);
        cancel.setText("Cancel");
        cancel.setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 8));

        TextView save = new TextView(c);
        save.setText("Save");
        save.setPadding(dp(c, 12), dp(c, 8), dp(c, 12), dp(c, 8));
        save.setTextColor(0xFF6750A4); // purple-ish

        actions.addView(cancel);
        actions.addView(save);
        root.addView(actions, ap);

        // PRE-MEASURE to get real width (so we can position without a second pass)
        int screenW = c.getResources().getDisplayMetrics().widthPixels;
        int maxWidth = (int) (screenW * 0.9f);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        root.measure(widthSpec, heightSpec);
        int contentW = Math.min(root.getMeasuredWidth(), maxWidth);

        // Create popup with deterministic width
        final PopupWindow pw = new PopupWindow(root,
                contentW,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true /* focusable */);
        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pw.setOutsideTouchable(true);
        pw.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        pw.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Compute target position: centered, ~96dp from top (tweak if needed)
        int x = (screenW - contentW) / 2;
        int y = dp(c, 96);

        // Show immediately at desired location — no jump
        pw.showAtLocation(anchor, Gravity.TOP | Gravity.START, x, y);

        cancel.setOnClickListener(v -> pw.dismiss());
        save.setOnClickListener(v -> {
            String newTitle = input.getText().toString().trim();
            if (newTitle.isEmpty()) {
                Toast.makeText(c, "Title can't be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            pw.dismiss();
            onRename.rename(doc, newTitle);
        });
    }

    private static int dp(Context c, int v) {
        float d = c.getResources().getDisplayMetrics().density;
        return (int) (v * d + 0.5f);
    }

    @Override public int getItemCount() { return (data == null) ? 0 : data.size(); }
}
