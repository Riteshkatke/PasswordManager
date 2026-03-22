package com.example.passwordmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.ViewHolder> {

    private final List<PasswordModel> list;
    private final Context context;
    private final Set<Long> visiblePasswordIds = new HashSet<>();

    public PasswordAdapter(Context context, List<PasswordModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_password, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PasswordModel item = list.get(position);

        holder.name.setText(item.getName());
        holder.website.setText(
                context.getString(
                        R.string.website_value,
                        item.getWebsite().isEmpty() ? context.getString(R.string.no_website) : item.getWebsite()
                )
        );
        holder.username.setText(context.getString(R.string.username_value, item.getUsername()));
        boolean isVisible = visiblePasswordIds.contains(item.getId());
        String displayPassword = isVisible ? item.getPassword() : "••••••••";
        holder.password.setText(context.getString(R.string.password_value, displayPassword));
        holder.toggleVisibility.setText(isVisible ? R.string.hide_password : R.string.show_password);

        holder.copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clip = ClipData.newPlainText("password", item.getPassword());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
        });

        holder.toggleVisibility.setOnClickListener(v -> {
            if (visiblePasswordIds.contains(item.getId())) {
                visiblePasswordIds.remove(item.getId());
            } else {
                visiblePasswordIds.add(item.getId());
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView website;
        final TextView username;
        final TextView password;
        final Button toggleVisibility;
        final Button copy;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            website = itemView.findViewById(R.id.website);
            username = itemView.findViewById(R.id.username);
            password = itemView.findViewById(R.id.password);
            toggleVisibility = itemView.findViewById(R.id.toggleVisibilityBtn);
            copy = itemView.findViewById(R.id.copyBtn);
        }
    }
}
