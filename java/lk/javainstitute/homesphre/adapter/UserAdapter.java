package lk.javainstitute.homesphre.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import lk.javainstitute.homesphre.R;
import lk.javainstitute.homesphre.model.User;
import lk.javainstitute.homesphre.navigationAdmin.UserManageFragment;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder> {

    private UserManageFragment userManageFragment;

    public UserAdapter(FirestoreRecyclerOptions<User> options, UserManageFragment fragment) {
        super(options);
        this.userManageFragment = fragment;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        holder.username.setText(model.getUsername());
        holder.email.setText(model.getEmail());


        if (model.isActive()) {
            holder.btnBlockUnblock.setText("Block");
            holder.btnBlockUnblock.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.Red));
        } else {
            holder.btnBlockUnblock.setText("Unblock");
            holder.btnBlockUnblock.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.gray));
        }

        holder.btnBlockUnblock.setOnClickListener(view -> {
            showConfirmDialog(holder, model, getSnapshots().getSnapshot(position));
        });
    }

    private void showConfirmDialog(UserViewHolder holder, User user, DocumentSnapshot snapshot) {
        String action = user.isActive() ? "block" : "unblock";

        new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Confirm " + action)
                .setMessage("Are you sure you want to " + action + " " + user.getUsername() + "?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    updateUserStatus(user, snapshot);
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void updateUserStatus(User user, DocumentSnapshot snapshot) {
        boolean newStatus = !user.isActive();
        snapshot.getReference().update("active", newStatus)
                .addOnSuccessListener(aVoid -> {
                    String message = newStatus ? "User unblocked successfully!" : "User blocked successfully!";



                    if (userManageFragment != null) {
                        userManageFragment.loadUserData();
                    }
                })
                .addOnFailureListener(e -> {
                });
    }




    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
        View view = LayoutInflater.from(group.getContext()).inflate(R.layout.user_item, group, false);
        return new UserViewHolder(view);
    }

    public void updateOptions(FirestoreRecyclerOptions<User> options) {
        super.updateOptions(options);
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username, email;
        Button btnBlockUnblock;

        public UserViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvUserName);
            email = itemView.findViewById(R.id.tvUserEmail);
            btnBlockUnblock = itemView.findViewById(R.id.btnBlockUnblock);
        }
    }
}


