package com.example.qrcodesfornoobs.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.qrcodesfornoobs.Models.Creature;
import com.example.qrcodesfornoobs.Models.Player;
import com.example.qrcodesfornoobs.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * A fragment for editing profile information.
 */
public class ProfileEditInfoFragment extends DialogFragment {
    Button confirmButton;
    Button cancelButton;
    EditText editEmail;
    AlertDialog dialog;
    String initialEmail;
    private int position; // The position of the listview item

    /**
     * Returns a new instance of the fragment with the given contact info.
     * @param contactInfo the contact info to edit
     * @return a new instance of the fragment with the given contact info
     */

    public static ProfileEditInfoFragment newInstance(String contactInfo) {
        ProfileEditInfoFragment fragment = new ProfileEditInfoFragment();
        Bundle args = new Bundle();
        args.putString("contactInfo", contactInfo);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to create the dialog shown by this fragment.
     * @param savedInstanceState the previously saved instance state
     * @return a new dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.edit_profile_fragment, null);


        // Builder for the delete item dialog popup
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        dialog = builder.create();

        // Initialize buttons and their click listeners
        confirmButton = view.findViewById(R.id.confirm_edit_button);
        cancelButton = view.findViewById(R.id.cancel_edit_button);
        editEmail = view.findViewById(R.id.profile_email_edit_text);
        // Use a bundle to pass in player data
        Bundle args = getArguments();
        if (args != null) {
            String email = args.getString("contactInfo");
            editEmail.setText(email);
            initialEmail = email;
        }
        addListenerOnButtons();

        // Show the dialog
        dialog.show();
        return dialog;
    }

    /**
     * Adds click listeners to the confirm and cancel buttons.
     */
    private void addListenerOnButtons(){
        // Buttons do nothing right now
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInput = editEmail.getText().toString().trim();
                if (initialEmail != userInput) {
                    FirebaseFirestore.getInstance().collection("Players")
                            .document(Player.LOCAL_USERNAME).update("contact", userInput);
                }
                Toast.makeText(getContext(), "Confirm", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Cancel", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }
}
