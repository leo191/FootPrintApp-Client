package com.example.leo.footprint;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class EditUserDetails extends Fragment {



   static EditUserDetails newInEditUserDetails()
   {
       EditUserDetails var = new EditUserDetails();
       return var;
   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_user_details,container, false);
    }

}
