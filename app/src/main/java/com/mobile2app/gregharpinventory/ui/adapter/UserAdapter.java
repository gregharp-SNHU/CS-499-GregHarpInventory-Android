package com.mobile2app.gregharpinventory.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile2app.gregharpinventory.R;
import com.mobile2app.gregharpinventory.model.DbKeys;
import com.mobile2app.gregharpinventory.model.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    // list of user data maps from Firestore
    private final List<Map<String, Object>> users = new ArrayList<>();

    // list of corresponding Firestore document IDs (UIDs)
    private final List<String> userIds = new ArrayList<>();

    // role options for the spinner
    private static final String[] ROLES = {Roles.USER, Roles.MANAGER, Roles.OWNER};

    // listener interfaces for role change and delete actions
    public interface OnRoleChangeListener {
        void onRoleChanged(String uid, String newRole);
    }

    public interface OnDeleteListener {
        void onDelete(String uid, String email);
    }

    private OnRoleChangeListener roleChangeListener;
    private OnDeleteListener deleteListener;

    // set the listener for role changes
    public void setOnRoleChangeListener(OnRoleChangeListener listener) {
        roleChangeListener = listener;
    }

    // set the listener for user deletion
    public void setOnDeleteListener(OnDeleteListener listener) {
        deleteListener = listener;
    }

    // update the adapter with new data
    public void setUsers(List<String> ids, List<Map<String, Object>> data) {
        userIds.clear();
        users.clear();
        userIds.addAll(ids);
        users.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, Object> user = users.get(position);
        String uid = userIds.get(position);

        // set the email and current role text
        String email = Objects.requireNonNull(user.get(DbKeys.EMAIL)).toString();
        String currentRole = Objects.requireNonNull(user.get(DbKeys.ROLE)).toString();

        // populate the email as username
        holder.emailText.setText(email);

        // set up the role spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                holder.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                ROLES
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.roleSpinner.setAdapter(spinnerAdapter);

        // set the spinner to the current role
        for (int i = 0; i < ROLES.length; i++) {
            if (ROLES[i].equals(currentRole)) {
                holder.roleSpinner.setSelection(i);
                break;
            }
        }

        // handle role change from spinner
        holder.roleSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent,
                                               View view, int pos, long id) {
                        String newRole = ROLES[pos];

                        // only fire if the role actually changed
                        if (!newRole.equals(currentRole) && roleChangeListener != null) {
                            roleChangeListener.onRoleChanged(uid, newRole);
                        }
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                        // nothing to do here
                    }
                });

        // handle delete button
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(uid, email);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // ViewHolder for user items
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView emailText;
        Spinner roleSpinner;
        ImageButton deleteButton;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.userEmailText);
            roleSpinner = itemView.findViewById(R.id.roleSpinner);
            deleteButton = itemView.findViewById(R.id.deleteUserButton);
        }
    }
}