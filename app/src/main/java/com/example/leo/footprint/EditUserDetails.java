package com.example.leo.footprint;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class EditUserDetails extends Fragment {

Button update_bt;

   static EditUserDetails newInEditUserDetails()
   {
       EditUserDetails var = new EditUserDetails();
       return var;
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_user_details,container, false);

        update_bt = (Button)v.findViewById(R.id.Update);
        update_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"still Working ...",Toast.LENGTH_LONG).show();
            }
        });
        return  v;
    }

}
