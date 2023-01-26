package uk.ac.ed.inf;

import java.util.List;

public class Menu{
    String name;
    String location;

    List<MenuItem> menu;
    public static class MenuItem {
        String item;
        int pence;
    }
}