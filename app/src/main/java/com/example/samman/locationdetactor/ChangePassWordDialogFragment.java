package com.example.samman.locationdetactor;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by BinodNepali on 1/25/2017.
 */

public class ChangePassWordDialogFragment extends DialogFragment{
    private FirebaseUser firebaseuser;
    private Context context;

    public void SetMapActivityContext(Context context){
        this.context=context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.changepass_dialog, null))
                // Add action buttons
                .setPositiveButton(R.string.confirm_changePassword, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText oldPassWordField = (EditText) ((AlertDialog) dialog).findViewById(R.id.oldPassWord);
                        EditText newPassWordField = (EditText) ((AlertDialog) dialog).findViewById(R.id.newPassWord);
                        String oldPassWord = oldPassWordField.getText().toString();
                        final String newPassWord = newPassWordField.getText().toString();
                        firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
                        if (!oldPassWord.equals("") && !newPassWord.equals("")) {
                            final String email = firebaseuser.getEmail();
                            AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassWord);
                            firebaseuser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        firebaseuser.updatePassword(newPassWord).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isComplete()) {
                                                    Toast.makeText(context, "Password Changed Successfully", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(context, "Something went wrong. Please try again later", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel_changePassword, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(context, "Operation is cancel", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
