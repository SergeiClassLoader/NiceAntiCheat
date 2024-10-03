package pro.cyrent.anticheat.util.cache;


import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;


//https://github.com/PaperMC/Paper/blob/ff7b9b03805ae54af82135d66dea75b4e6761343/patches/api/0043-Misc-Utils.patch#L7
public class CachedSizeConcurrentLinkedQueue<E> extends ConcurrentLinkedQueue<E> {
    private final LongAdder cachedSize = new LongAdder();

    @Override
    public boolean add(E e) {
        boolean result = super.add(e);
        if (result) {
            cachedSize.increment();
        }
        return result;
    }

    @Override
    public E poll() {
        E result = super.poll();
        if (result != null) {
            cachedSize.decrement();
        }
        return result;
    }

    @Override
    public int size() {
        return cachedSize.intValue();
    }
}
