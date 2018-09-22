package quickcarpet;

import java.util.LinkedHashMap;
import java.util.Map;

public class EvictingQueue<K,V> extends LinkedHashMap<K,V>
{
    private static final long serialVersionUID = 1L;

    @Override
     protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
     {
        return this.size() > 10; 
     }
}