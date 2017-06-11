package com.example.hossam.hossamraya7challenge.Network;

import com.example.hossam.hossamraya7challenge.Models.Route;

import java.util.ArrayList;

/**
 * Created by hossam on 09/06/17.
 */

public interface JSONParserInterface {
    void DirectionDrawer();
    void DirectionDrawerSuccess(ArrayList<Route> route);
    void Route(String [] StringArray);
}
