package cn.charlotte.pit.data;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class EventQueue {

    public String id = "1";

    public List<String> normalEvents = new ObjectArrayList<>(16);

    public List<String> epicEvents = new ObjectArrayList<>(16);

}
