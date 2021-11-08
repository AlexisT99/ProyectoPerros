package com.example.sql.Controlador;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerController extends FragmentStatePagerAdapter {
    int numoftabs;
    Acelerometro Acelerometro;
    Giroscopio Giroscopio;
    Magnetometro Magnetometro;

    public PagerController(@NonNull FragmentManager fm, int behaviors) {
        super(fm, behaviors);
        this.numoftabs = behaviors;
        Acelerometro = new Acelerometro();
        Giroscopio = new Giroscopio();
        Magnetometro = new Magnetometro();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return  Acelerometro;
            case 1:
                return Giroscopio;
            case 2:
                return Magnetometro;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numoftabs;
    }
}
